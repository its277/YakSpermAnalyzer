package com.yaksperm.analyzer.domain.model

/**
 * Raw detection from a single YOLO inference pass on one video frame.
 * Coordinates are in the original (unscaled) frame pixel space.
 */
data class SpermDetection(
    val x: Float,             // center-x in pixels
    val y: Float,             // center-y in pixels
    val width: Float,
    val height: Float,
    val angle: Float,         // OBB rotation angle in degrees
    val confidence: Float,
    val frameIndex: Int
)
