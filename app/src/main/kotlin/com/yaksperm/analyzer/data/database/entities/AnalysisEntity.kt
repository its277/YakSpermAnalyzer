package com.yaksperm.analyzer.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "analyses")
data class AnalysisEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: String,
    val sampleId: String,
    val technicianName: String,
    val timestamp: Long,

    // Counts
    val totalDetected: Int,
    val totalTracked: Int,
    val progressiveMotile: Int,
    val nonProgressiveMotile: Int,
    val immotile: Int,

    // Motility %
    val totalMotilityPct: Float,
    val progressiveMotilityPct: Float,
    val nonProgressiveMotilityPct: Float,

    // Velocity (µm/s)
    val vclMean: Float,
    val vapMean: Float,
    val vslMean: Float,

    // Linearity (%)
    val linMean: Float,
    val strMean: Float,
    val wobMean: Float,

    val grade: String,         // MotilityGrade.name()
    val videoPath: String,
    val framesProcessed: Int,
    val durationSeconds: Float,
    val pdfPath: String? = null
)
