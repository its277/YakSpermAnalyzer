package com.yaksperm.analyzer.presentation.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import com.yaksperm.analyzer.domain.model.MotilityGrade
import com.yaksperm.analyzer.presentation.theme.AmberCount
import com.yaksperm.analyzer.presentation.theme.AmberDim
import com.yaksperm.analyzer.presentation.theme.CyanPrimary
import com.yaksperm.analyzer.presentation.theme.GreenDim
import com.yaksperm.analyzer.presentation.theme.GreenMotile
import com.yaksperm.analyzer.presentation.theme.RedAlert
import com.yaksperm.analyzer.presentation.theme.RedDim

/**
 * Small colored badge displaying the motility grade.
 */
@Composable
fun GradeTag(grade: MotilityGrade, modifier: Modifier = Modifier) {
    val (bg, accent) = when (grade) {
        MotilityGrade.EXCELLENT -> GreenDim    to GreenMotile
        MotilityGrade.GOOD      -> Color(0xFF0D2A3D) to CyanPrimary
        MotilityGrade.FAIR      -> AmberDim    to AmberCount
        MotilityGrade.POOR      -> RedDim      to RedAlert
    }
    val text = when (grade) {
        MotilityGrade.EXCELLENT -> "EXCELLENT"
        MotilityGrade.GOOD      -> "GOOD"
        MotilityGrade.FAIR      -> "FAIR"
        MotilityGrade.POOR      -> "POOR"
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .border(1.dp, accent, RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = accent,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
        )
    }
}
