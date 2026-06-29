package com.yaksperm.analyzer.domain.tracker

/**
 * Mutable state for one tracked sperm cell.
 * [state] = [x, y, vx, vy, ax, ay] — UKF posterior mean.
 * [covariance] — 6×6 posterior covariance matrix.
 */
data class TrackedSperm(
    val id: Int,
    var state: DoubleArray,
    var covariance: Array<DoubleArray>,
    val history: MutableList<Pair<Float, Float>>,          // (x, y) position history
    val velocityHistory: MutableList<Pair<Float, Float>>,  // (vx, vy) velocity history
    val frameIndices: MutableList<Int>,
    var age: Int,
    var isActive: Boolean
) {
    override fun equals(other: Any?) = other is TrackedSperm && id == other.id
    override fun hashCode() = id
}
