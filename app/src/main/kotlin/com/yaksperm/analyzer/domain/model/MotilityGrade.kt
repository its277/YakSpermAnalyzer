package com.yaksperm.analyzer.domain.model

import com.yaksperm.analyzer.Constants

/** WHO/Hamilton IVOS-II-based motility grade for the overall sample. */
enum class MotilityGrade { EXCELLENT, GOOD, FAIR, POOR }

fun classifyGrade(progressivePct: Float): MotilityGrade = when {
    progressivePct >= 50f -> MotilityGrade.EXCELLENT
    progressivePct >= 30f -> MotilityGrade.GOOD
    progressivePct >= 15f -> MotilityGrade.FAIR
    else                  -> MotilityGrade.POOR
}

fun MotilityGrade.label(): String = when (this) {
    MotilityGrade.EXCELLENT -> "Excellent"
    MotilityGrade.GOOD      -> "Good"
    MotilityGrade.FAIR      -> "Fair"
    MotilityGrade.POOR      -> "Poor"
}

fun MotilityGrade.interpretation(): String = when (this) {
    MotilityGrade.EXCELLENT -> "Sample meets breeding standards. Progressive motility ≥ 50%."
    MotilityGrade.GOOD      -> "Sample is suitable for AI use. Progressive motility ≥ 30%."
    MotilityGrade.FAIR      -> "Marginal sample. Consider extended evaluation."
    MotilityGrade.POOR      -> "Sample does not meet minimum standards. Discard or retest."
}
