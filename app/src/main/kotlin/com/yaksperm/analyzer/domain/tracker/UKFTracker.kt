package com.yaksperm.analyzer.domain.tracker

import com.yaksperm.analyzer.Constants
import com.yaksperm.analyzer.domain.model.SpermDetection
import kotlin.math.sqrt

/**
 * Full Unscented Kalman Filter implementation for sperm cell tracking.
 *
 * State vector: [x, y, vx, vy, ax, ay]  (position, velocity, acceleration in pixels)
 * Measurement:  [x, y]                  (YOLO-detected center)
 *
 * Uses Merwe scaled sigma points with alpha=0.1, beta=2.0, kappa=0.0.
 */
class UKFTracker {

    private val tracks = mutableListOf<TrackedSperm>()
    private var nextId = 0

    private val n = Constants.UKF_STATE_DIM         // 6
    private val alpha = Constants.UKF_ALPHA          // 0.1
    private val beta = Constants.UKF_BETA            // 2.0
    private val kappa = Constants.UKF_KAPPA          // 0.0
    private val lambda = alpha * alpha * (n + kappa) - n

    // Sigma point weights
    private val Wm = DoubleArray(2 * n + 1)          // mean weights
    private val Wc = DoubleArray(2 * n + 1)          // covariance weights

    private val dt = 1.0 / Constants.BIOLOGICAL_FPS

    // Process noise Q (diagonal)
    private val Q = diag(doubleArrayOf(1.0, 1.0, 2.0, 2.0, 0.5, 0.5))

    // Measurement noise R (diagonal, 2×2)
    private val R = diag(doubleArrayOf(15.0, 15.0))

    init {
        Wm[0] = lambda / (n + lambda)
        Wc[0] = lambda / (n + lambda) + (1.0 - alpha * alpha + beta)
        for (i in 1 until 2 * n + 1) {
            Wm[i] = 1.0 / (2.0 * (n + lambda))
            Wc[i] = Wm[i]
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Public API
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Process one video frame: predict all existing tracks, match to new detections,
     * update matched tracks, create new tracks for unmatched detections, age unmatched tracks.
     *
     * @return All currently active [TrackedSperm] after the update step.
     */
    fun update(detections: List<SpermDetection>, frameIndex: Int): List<TrackedSperm> {
        // 1. Predict
        tracks.forEach { predict(it) }

        // 2. Build cost matrix
        val nT = tracks.size
        val nD = detections.size

        if (nT == 0) {
            // Bootstrap all detections as new tracks
            detections.forEach { d -> initTrack(d, frameIndex) }
            return tracks.filter { it.isActive }.toList()
        }

        val cost = Array(nT) { t ->
            DoubleArray(if (nD > 0) nD else 1) { d ->
                if (nD == 0) Double.MAX_VALUE
                else {
                    val det = detections[d]
                    val dx = tracks[t].state[0] - det.x
                    val dy = tracks[t].state[1] - det.y
                    sqrt(dx * dx + dy * dy)
                }
            }
        }

        // 3. Hungarian assignment
        val assignments = if (nD > 0) HungarianMatcher.assign(cost) else emptyList()
        val matchedTracks = mutableSetOf<Int>()
        val matchedDets   = mutableSetOf<Int>()

        assignments.forEach { (tIdx, dIdx) ->
            if (cost[tIdx][dIdx] <= Constants.MAX_MATCH_DISTANCE) {
                updateTrack(tracks[tIdx], detections[dIdx], frameIndex)
                matchedTracks.add(tIdx)
                matchedDets.add(dIdx)
            }
        }

        // 4. Unmatched detections → new tracks
        detections.forEachIndexed { dIdx, d ->
            if (dIdx !in matchedDets) initTrack(d, frameIndex)
        }

        // 5. Unmatched tracks → age out
        val toRemove = mutableListOf<TrackedSperm>()
        tracks.forEachIndexed { tIdx, t ->
            if (tIdx !in matchedTracks) {
                t.age++
                if (t.age > Constants.MAX_TRACK_AGE) {
                    t.isActive = false
                    toRemove.add(t)
                }
            }
        }
        tracks.removeAll(toRemove)

        return tracks.toList()
    }

    fun reset() {
        tracks.clear()
        nextId = 0
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UKF Predict Step
    // ─────────────────────────────────────────────────────────────────────────

    private fun predict(track: TrackedSperm) {
        val x = track.state
        val P = track.covariance

        // Generate sigma points
        val sigmas = generateSigmas(x, P)

        // Propagate through constant-acceleration process model
        val sigmasPred = Array(2 * n + 1) { i -> processModel(sigmas[i]) }

        // Weighted mean
        val xPred = DoubleArray(n)
        for (i in 0 until 2 * n + 1) {
            for (j in 0 until n) xPred[j] += Wm[i] * sigmasPred[i][j]
        }

        // Weighted covariance + Q
        val PPred = Array(n) { DoubleArray(n) }
        for (i in 0 until 2 * n + 1) {
            val diff = DoubleArray(n) { j -> sigmasPred[i][j] - xPred[j] }
            for (r in 0 until n) for (c in 0 until n) {
                PPred[r][c] += Wc[i] * diff[r] * diff[c]
            }
        }
        for (r in 0 until n) for (c in 0 until n) PPred[r][c] += Q[r][c]

        track.state = xPred
        track.covariance = PPred
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UKF Update Step
    // ─────────────────────────────────────────────────────────────────────────

    private fun updateTrack(track: TrackedSperm, det: SpermDetection, frameIndex: Int) {
        val x = track.state
        val P = track.covariance
        val m = Constants.UKF_MEAS_DIM   // 2

        val sigmas = generateSigmas(x, P)

        // Map sigmas through measurement model h(x) = [x, y]
        val sigmasH = Array(2 * n + 1) { i -> doubleArrayOf(sigmas[i][0], sigmas[i][1]) }

        // Predicted measurement mean
        val zPred = DoubleArray(m)
        for (i in 0 until 2 * n + 1) for (j in 0 until m) zPred[j] += Wm[i] * sigmasH[i][j]

        // Innovation covariance Pzz + R
        val Pzz = Array(m) { DoubleArray(m) }
        for (i in 0 until 2 * n + 1) {
            val dz = DoubleArray(m) { j -> sigmasH[i][j] - zPred[j] }
            for (r in 0 until m) for (c in 0 until m) Pzz[r][c] += Wc[i] * dz[r] * dz[c]
        }
        for (r in 0 until m) for (c in 0 until m) Pzz[r][c] += R[r][c]

        // Cross-covariance Pxz
        val Pxz = Array(n) { DoubleArray(m) }
        for (i in 0 until 2 * n + 1) {
            val dx = DoubleArray(n) { j -> sigmas[i][j] - x[j] }
            val dz = DoubleArray(m) { j -> sigmasH[i][j] - zPred[j] }
            for (r in 0 until n) for (c in 0 until m) Pxz[r][c] += Wc[i] * dx[r] * dz[c]
        }

        // Kalman gain K = Pxz * Pzz^{-1}  (2×2 invert analytically)
        val PzzInv = invert2x2(Pzz)
        val K = matMul(Pxz, PzzInv, n, m, m)

        // Innovation
        val z = doubleArrayOf(det.x.toDouble(), det.y.toDouble())
        val innov = DoubleArray(m) { j -> z[j] - zPred[j] }

        // Updated state
        val xUpd = DoubleArray(n) { j -> x[j] + (0 until m).sumOf { K[j][it] * innov[it] } }

        // Updated covariance P = P - K * Pzz * K^T
        val KPzz = matMul(K, Pzz, n, m, m)
        val KPzzKt = matMul(KPzz, transposeNxM(K, n, m), n, m, n)
        val PUpd = Array(n) { r -> DoubleArray(n) { c -> P[r][c] - KPzzKt[r][c] } }

        track.state = xUpd
        track.covariance = PUpd
        track.age = 0

        // Record history
        track.history.add(Pair(xUpd[0].toFloat(), xUpd[1].toFloat()))
        track.velocityHistory.add(Pair(xUpd[2].toFloat(), xUpd[3].toFloat()))
        track.frameIndices.add(frameIndex)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private fun initTrack(d: SpermDetection, frameIndex: Int) {
        val state = doubleArrayOf(d.x.toDouble(), d.y.toDouble(), 0.0, 0.0, 0.0, 0.0)
        val P = diag(doubleArrayOf(50.0, 50.0, 100.0, 100.0, 50.0, 50.0))
        tracks.add(
            TrackedSperm(
                id = nextId++,
                state = state,
                covariance = P,
                history = mutableListOf(Pair(d.x, d.y)),
                velocityHistory = mutableListOf(Pair(0f, 0f)),
                frameIndices = mutableListOf(frameIndex),
                age = 0,
                isActive = true
            )
        )
    }

    /** Merwe scaled sigma points. Returns (2n+1) arrays of length n. */
    private fun generateSigmas(x: DoubleArray, P: Array<DoubleArray>): Array<DoubleArray> {
        val scale = sqrt(n + lambda)
        val L = choleskyDecompose(P)

        val sigmas = Array(2 * n + 1) { DoubleArray(n) }
        sigmas[0] = x.copyOf()
        for (i in 0 until n) {
            val col = DoubleArray(n) { r -> L[r][i] * scale }
            for (j in 0 until n) {
                sigmas[i + 1][j]     = x[j] + col[j]
                sigmas[n + i + 1][j] = x[j] - col[j]
            }
        }
        return sigmas
    }

    /** Constant-acceleration process model: x_k+1 = F * x_k */
    private fun processModel(x: DoubleArray): DoubleArray {
        val (px, py, vx, vy, ax, ay) = x
        return doubleArrayOf(
            px + vx * dt + 0.5 * ax * dt * dt,
            py + vy * dt + 0.5 * ay * dt * dt,
            vx + ax * dt,
            vy + ay * dt,
            ax,
            ay
        )
    }

    /** Cholesky decomposition (lower triangular). Returns L where P = L*L^T */
    private fun choleskyDecompose(A: Array<DoubleArray>): Array<DoubleArray> {
        val size = A.size
        val L = Array(size) { DoubleArray(size) }
        for (i in 0 until size) {
            for (j in 0..i) {
                var sum = A[i][j]
                for (k in 0 until j) sum -= L[i][k] * L[j][k]
                L[i][j] = if (i == j) {
                    if (sum < 1e-10) sqrt(1e-10) else sqrt(sum)
                } else {
                    sum / (L[j][j] + 1e-10)
                }
            }
        }
        return L
    }

    private fun invert2x2(M: Array<DoubleArray>): Array<DoubleArray> {
        val det = M[0][0] * M[1][1] - M[0][1] * M[1][0]
        val invDet = 1.0 / (det + 1e-12)
        return arrayOf(
            doubleArrayOf( M[1][1] * invDet, -M[0][1] * invDet),
            doubleArrayOf(-M[1][0] * invDet,  M[0][0] * invDet)
        )
    }

    private fun matMul(A: Array<DoubleArray>, B: Array<DoubleArray>, rows: Int, inner: Int, cols: Int): Array<DoubleArray> {
        val C = Array(rows) { DoubleArray(cols) }
        for (r in 0 until rows) for (c in 0 until cols) for (k in 0 until inner) {
            C[r][c] += A[r][k] * B[k][c]
        }
        return C
    }

    private fun transposeNxM(A: Array<DoubleArray>, rows: Int, cols: Int): Array<DoubleArray> {
        return Array(cols) { c -> DoubleArray(rows) { r -> A[r][c] } }
    }

    private fun diag(v: DoubleArray): Array<DoubleArray> {
        return Array(v.size) { i -> DoubleArray(v.size) { j -> if (i == j) v[i] else 0.0 } }
    }
}

private operator fun DoubleArray.component1() = this[0]
private operator fun DoubleArray.component2() = this[1]
private operator fun DoubleArray.component3() = this[2]
private operator fun DoubleArray.component4() = this[3]
private operator fun DoubleArray.component5() = this[4]
private operator fun DoubleArray.component6() = this[5]
