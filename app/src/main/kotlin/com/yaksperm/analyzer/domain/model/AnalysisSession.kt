package com.yaksperm.analyzer.domain.model

/** Lightweight summary for the history list — avoids loading full CASA data. */
data class AnalysisSession(
    val id: Long,
    val sampleId: String,
    val technicianName: String,
    val timestamp: Long,
    val grade: MotilityGrade,
    val totalMotilityPct: Float,
    val progressiveMotilityPct: Float,
    val totalTracked: Int,
    val pdfPath: String?
)
