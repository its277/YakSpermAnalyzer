package com.yaksperm.analyzer.domain.model

/**
 * Complete CASA result for one analysis session.
 * All velocity values are in µm/s; linearity ratios in %.
 */
data class CasaResult(
    val sessionId: String,
    val sampleId: String,
    val technicianName: String,
    val timestamp: Long,

    // ── Cell counts ───────────────────────────────────────────────────────────
    val totalDetected: Int,
    val totalTracked: Int,
    val progressiveMotile: Int,
    val nonProgressiveMotile: Int,
    val immotile: Int,

    // ── Motility percentages ──────────────────────────────────────────────────
    val totalMotilityPct: Float,
    val progressiveMotilityPct: Float,
    val nonProgressiveMotilityPct: Float,

    // ── Velocity parameters (µm/s) ─────────────────────────────────────────────
    val vclMean: Float,   // Curvilinear Velocity
    val vapMean: Float,   // Average Path Velocity
    val vslMean: Float,   // Straight-Line Velocity

    // ── Linearity ratios (%) ───────────────────────────────────────────────────
    val linMean: Float,   // LIN = VSL/VCL × 100
    val strMean: Float,   // STR = VSL/VAP × 100
    val wobMean: Float,   // WOB = VAP/VCL × 100

    val grade: MotilityGrade,
    val videoPath: String,
    val framesProcessed: Int,
    val durationSeconds: Float
)
