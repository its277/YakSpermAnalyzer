package com.yaksperm.analyzer.domain.tracker

import kotlin.math.min

/**
 * Hungarian algorithm (Kuhn-Munkres) for optimal bipartite matching.
 *
 * Input:  [rows × cols] cost matrix where rows = tracks, cols = detections.
 * Output: List of (trackIndex, detectionIndex) pairs — optimal assignment.
 *
 * Pairs where cost > gate threshold are excluded by the caller.
 *
 * Time complexity: O(n³) where n = max(rows, cols).
 */
object HungarianMatcher {

    /**
     * Compute optimal assignments.
     * @param cost cost[i][j] = distance between track i and detection j.
     * @return list of (trackIdx, detectionIdx) matched pairs.
     */
    fun assign(cost: Array<DoubleArray>): List<Pair<Int, Int>> {
        val rows = cost.size
        if (rows == 0) return emptyList()
        val cols = cost[0].size
        if (cols == 0) return emptyList()

        val n = maxOf(rows, cols)

        // Pad cost matrix to square with a large value
        val INF = Double.MAX_VALUE / 2.0
        val c = Array(n) { r -> DoubleArray(n) { cc -> if (r < rows && cc < cols) cost[r][cc] else INF } }

        // u[i] = dual variable for row i, v[j] = dual variable for col j
        val u = DoubleArray(n + 1)
        val v = DoubleArray(n + 1)
        val p = IntArray(n + 1)    // p[j] = row assigned to column j (1-indexed, 0 = unassigned)
        val way = IntArray(n + 1)

        for (i in 1..n) {
            p[0] = i
            var j0 = 0
            val minVal = DoubleArray(n + 1) { INF }
            val used = BooleanArray(n + 1) { false }

            do {
                used[j0] = true
                val i0 = p[j0]
                var delta = INF
                var j1 = -1
                for (j in 1..n) {
                    if (!used[j]) {
                        val cur = c[i0 - 1][j - 1] - u[i0] - v[j]
                        if (cur < minVal[j]) {
                            minVal[j] = cur
                            way[j] = j0
                        }
                        if (minVal[j] < delta) {
                            delta = minVal[j]
                            j1 = j
                        }
                    }
                }
                for (j in 0..n) {
                    if (used[j]) {
                        u[p[j]] += delta
                        v[j] -= delta
                    } else {
                        minVal[j] -= delta
                    }
                }
                j0 = j1!!
            } while (p[j0] != 0)

            do {
                p[j0] = p[way[j0]]
                j0 = way[j0]
            } while (j0 != 0)
        }

        // Collect assignments within the original matrix bounds
        val assignments = mutableListOf<Pair<Int, Int>>()
        for (j in 1..n) {
            val i = p[j] - 1   // 0-indexed track
            val jIdx = j - 1   // 0-indexed detection
            if (i < rows && jIdx < cols && c[i][jIdx] < INF) {
                assignments.add(Pair(i, jIdx))
            }
        }
        return assignments
    }
}
