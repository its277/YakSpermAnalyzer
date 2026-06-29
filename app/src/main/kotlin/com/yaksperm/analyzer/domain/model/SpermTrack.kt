package com.yaksperm.analyzer.domain.model

/**
 * A single sperm cell's trajectory across all frames where it was successfully tracked.
 * Positions and velocities are in original pixel space; CASA calculator converts to µm/s.
 */
data class SpermTrack(
    val trackId: Int,
    /** (x, y) center positions ordered by ascending frame index */
    val positions: List<Pair<Float, Float>>,
    /** (vx, vy) smoothed velocities from UKF state, same length as positions */
    val velocities: List<Pair<Float, Float>>,
    val frameIndices: List<Int>,
    val isActive: Boolean
)
