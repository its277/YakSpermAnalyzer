package com.yaksperm.analyzer.domain.casa

import com.yaksperm.analyzer.Constants
import com.yaksperm.analyzer.domain.model.CasaResult
import com.yaksperm.analyzer.domain.model.MotilityGrade
import com.yaksperm.analyzer.domain.model.SpermTrack
import com.yaksperm.analyzer.domain.model.classifyGrade
import java.util.UUID
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * CASA (Computer-Assisted Sperm Analysis) parameter calculator.
 *
 * Implements Hamilton IVOS-II kinematic definitions:
 *   VCL — Curvilinear Velocity:  mean frame-to-frame speed
 *   VSL — Straight-Line Velocity: displacement / total time
 *   VAP — Average Path Velocity: mean of UKF-smoothed speed
 *   LIN — Linearity:  VSL/VCL × 100
 *   STR — Straightness: VSL/VAP × 100
 *   WOB — Wobble:      VAP/VCL × 100
 *
 * Progressive:    VAP ≥ 40 µm/s AND STR ≥ 60%
 * Non-progressive: motile (VCL ≥ 5 µm/s) but not progressive
 * Immotile:       VCL < 5 µm/s
 */
object CasaCalculator {

    fun compute(
        tracks: List<SpermTrack>,
        sessionId: String = UUID.randomUUID().toString(),
        sampleId: String = "",
        technicianName: String = "",
        timestamp: Long = System.currentTimeMillis(),
        videoPath: String = "",
        framesProcessed: Int = 0,
        totalDetected: Int = 0
    ): CasaResult {
        val valid = tracks.filter { it.positions.size >= Constants.MIN_TRACK_LENGTH }

        if (valid.isEmpty()) {
            return emptyResult(sessionId, sampleId, technicianName, timestamp, videoPath, framesProcessed, totalDetected)
        }

        val kinematics = valid.map { computeTrackKinematics(it) }

        var progressive = 0
        var nonProgressive = 0
        var immotile = 0

        kinematics.forEach { k ->
            when {
                k.vcl < Constants.IMMOTILE_VCL_THRESHOLD -> immotile++
                k.vap >= Constants.PROGRESSIVE_VAP_THRESHOLD && k.str >= Constants.PROGRESSIVE_STR_THRESHOLD -> progressive++
                else -> nonProgressive++
            }
        }

        val totalTracked = valid.size
        val totalMotile = progressive + nonProgressive
        val totalMotilityPct   = if (totalTracked > 0) (totalMotile.toFloat() / totalTracked) * 100f else 0f
        val progressivePct     = if (totalTracked > 0) (progressive.toFloat() / totalTracked) * 100f else 0f
        val nonProgressivePct  = if (totalTracked > 0) (nonProgressive.toFloat() / totalTracked) * 100f else 0f

        val vclMean = kinematics.map { it.vcl }.average().toFloat()
        val vapMean = kinematics.map { it.vap }.average().toFloat()
        val vslMean = kinematics.map { it.vsl }.average().toFloat()
        val linMean = kinematics.map { it.lin }.average().toFloat()
        val strMean = kinematics.map { it.str }.average().toFloat()
        val wobMean = kinematics.map { it.wob }.average().toFloat()

        val durationSeconds = framesProcessed / Constants.BIOLOGICAL_FPS

        return CasaResult(
            sessionId                = sessionId,
            sampleId                 = sampleId,
            technicianName           = technicianName,
            timestamp                = timestamp,
            totalDetected            = totalDetected,
            totalTracked             = totalTracked,
            progressiveMotile        = progressive,
            nonProgressiveMotile     = nonProgressive,
            immotile                 = immotile,
            totalMotilityPct         = totalMotilityPct,
            progressiveMotilityPct   = progressivePct,
            nonProgressiveMotilityPct = nonProgressivePct,
            vclMean                  = vclMean,
            vapMean                  = vapMean,
            vslMean                  = vslMean,
            linMean                  = linMean,
            strMean                  = strMean,
            wobMean                  = wobMean,
            grade                    = classifyGrade(progressivePct),
            videoPath                = videoPath,
            framesProcessed          = framesProcessed,
            durationSeconds          = durationSeconds
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Per-track kinematics
    // ─────────────────────────────────────────────────────────────────────────

    fun computeTrackKinematics(track: SpermTrack): TrackKinematics {
        val dt = 1.0f / Constants.BIOLOGICAL_FPS
        val p = Constants.PIXEL_TO_UM

        // VCL: mean of frame-to-frame speed (µm/s)
        val displacements = track.positions.zipWithNext { a, b ->
            val dx = (b.first - a.first) * p
            val dy = (b.second - a.second) * p
            sqrt(dx * dx + dy * dy)
        }
        val vcl = if (displacements.isEmpty()) 0f
                  else (displacements.average().toFloat() / dt)

        // VSL: straight-line speed (µm/s)
        val first = track.positions.first()
        val last  = track.positions.last()
        val dxTotal = (last.first  - first.first)  * p
        val dyTotal = (last.second - first.second) * p
        val totalTime = (track.positions.size - 1) * dt
        val vsl = if (totalTime > 0f) sqrt(dxTotal * dxTotal + dyTotal * dyTotal) / totalTime else 0f

        // VAP: mean of UKF-smoothed velocity magnitudes (µm/s)
        val vap = if (track.velocities.isEmpty()) 0f
                  else {
                      val magnitudes = track.velocities.map { (vx, vy) ->
                          sqrt((vx * p).pow(2) + (vy * p).pow(2))
                      }
                      (magnitudes.average().toFloat() / dt)
                  }

        // Linearity ratios (%)
        val lin = if (vcl > 0f) (vsl / vcl) * 100f else 0f
        val str = if (vap > 0f) (vsl / vap) * 100f else 0f
        val wob = if (vcl > 0f) (vap / vcl) * 100f else 0f

        return TrackKinematics(
            vcl = vcl,
            vap = vap,
            vsl = vsl,
            lin = lin.coerceIn(0f, 100f),
            str = str.coerceIn(0f, 100f),
            wob = wob.coerceIn(0f, 100f)
        )
    }

    private fun emptyResult(
        sessionId: String, sampleId: String, technicianName: String,
        timestamp: Long, videoPath: String, framesProcessed: Int, totalDetected: Int
    ) = CasaResult(
        sessionId = sessionId, sampleId = sampleId, technicianName = technicianName,
        timestamp = timestamp, totalDetected = totalDetected, totalTracked = 0,
        progressiveMotile = 0, nonProgressiveMotile = 0, immotile = 0,
        totalMotilityPct = 0f, progressiveMotilityPct = 0f, nonProgressiveMotilityPct = 0f,
        vclMean = 0f, vapMean = 0f, vslMean = 0f,
        linMean = 0f, strMean = 0f, wobMean = 0f,
        grade = MotilityGrade.POOR,
        videoPath = videoPath, framesProcessed = framesProcessed,
        durationSeconds = framesProcessed / Constants.BIOLOGICAL_FPS
    )
}

data class TrackKinematics(
    val vcl: Float,
    val vap: Float,
    val vsl: Float,
    val lin: Float,
    val str: Float,
    val wob: Float
)
