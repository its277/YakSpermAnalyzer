package com.yaksperm.analyzer.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yaksperm.analyzer.presentation.theme.CyanDim
import com.yaksperm.analyzer.presentation.theme.CyanPrimary
import com.yaksperm.analyzer.presentation.theme.TextDim
import com.yaksperm.analyzer.presentation.theme.TextPrimary

/**
 * Animated circular arc progress indicator with center label.
 * Animates sweep angle on first composition.
 */
@Composable
fun CircularProgressArc(
    value: Float,
    max: Float = 100f,
    size: Dp = 80.dp,
    strokeWidth: Dp = 7.dp,
    color: Color = CyanPrimary,
    bgColor: Color = CyanDim,
    label: String,
    unit: String = "%",
    modifier: Modifier = Modifier
) {
    var targetSweep by remember { mutableFloatStateOf(0f) }
    val animSweep by animateFloatAsState(
        targetValue = targetSweep,
        animationSpec = tween(durationMillis = 900),
        label = "arcSweep"
    )

    LaunchedEffect(value) {
        targetSweep = if (max > 0f) (value / max * 360f).coerceIn(0f, 360f) else 0f
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {
        Box(contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(size)) {
                val stroke = strokeWidth.toPx()
                val inset  = stroke / 2f
                val arcSize = Size(this.size.width - stroke, this.size.height - stroke)
                val arcTopLeft = Offset(inset, inset)

                // Background ring
                drawArc(
                    color = bgColor,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
                // Progress arc
                if (animSweep > 0f) {
                    drawArc(
                        color = color,
                        startAngle = -90f,
                        sweepAngle = animSweep,
                        useCenter = false,
                        topLeft = arcTopLeft,
                        size = arcSize,
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                }
            }

            // Center text
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${value.toInt()}$unit",
                    fontSize = (size.value * 0.18f).sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
        }

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextDim
        )
    }
}
