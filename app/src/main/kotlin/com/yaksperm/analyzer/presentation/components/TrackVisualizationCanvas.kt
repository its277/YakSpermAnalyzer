package com.yaksperm.analyzer.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yaksperm.analyzer.Constants
import com.yaksperm.analyzer.domain.casa.CasaCalculator
import com.yaksperm.analyzer.domain.model.SpermTrack
import com.yaksperm.analyzer.presentation.theme.CyanPrimary
import com.yaksperm.analyzer.presentation.theme.GreenMotile
import com.yaksperm.analyzer.presentation.theme.NavyBg
import com.yaksperm.analyzer.presentation.theme.TextDim

/**
 * Full-width canvas composable that draws all UKF sperm trajectories
 * color-coded by motility classification.
 *
 * Green = Progressive, Cyan = Non-Progressive, Gray = Immotile
 */
@Composable
fun TrackVisualizationCanvas(
    tracks: List<SpermTrack>,
    modifier: Modifier = Modifier
) {
    val classifiedTracks = tracks.map { track ->
        val kin = CasaCalculator.computeTrackKinematics(track)
        val color = when {
            kin.vcl < Constants.IMMOTILE_VCL_THRESHOLD -> Color(0xFF4A6070)
            kin.vap >= Constants.PROGRESSIVE_VAP_THRESHOLD && kin.str >= Constants.PROGRESSIVE_STR_THRESHOLD -> GreenMotile
            else -> CyanPrimary
        }
        Pair(track, color)
    }

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF050E1A))
        ) {
            if (tracks.isEmpty()) return@Canvas

            // Find bounding box for scaling
            val allX = tracks.flatMap { t -> t.positions.map { it.first } }
            val allY = tracks.flatMap { t -> t.positions.map { it.second } }
            val minX = (allX.minOrNull() ?: 0f) - 20f
            val maxX = (allX.maxOrNull() ?: 640f) + 20f
            val minY = (allY.minOrNull() ?: 0f) - 20f
            val maxY = (allY.maxOrNull() ?: 480f) + 20f
            val rangeX = (maxX - minX).coerceAtLeast(1f)
            val rangeY = (maxY - minY).coerceAtLeast(1f)

            fun scaleX(x: Float) = ((x - minX) / rangeX) * size.width
            fun scaleY(y: Float) = ((y - minY) / rangeY) * size.height

            classifiedTracks.forEach { (track, color) ->
                if (track.positions.size < 2) return@forEach

                val path = Path()
                track.positions.forEachIndexed { idx, (px, py) ->
                    val sx = scaleX(px)
                    val sy = scaleY(py)
                    if (idx == 0) path.moveTo(sx, sy) else path.lineTo(sx, sy)
                }

                // Subtle glow — draw twice: once wider+transparent, once solid
                drawPath(
                    path = path,
                    color = color.copy(alpha = 0.15f),
                    style = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
                drawPath(
                    path = path,
                    color = color.copy(alpha = 0.85f),
                    style = Stroke(width = 1.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                // Head dot
                val last = track.positions.last()
                drawCircle(
                    color = color,
                    radius = 3f,
                    center = Offset(scaleX(last.first), scaleY(last.second))
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Legend
        Row(verticalAlignment = Alignment.CenterVertically) {
            LegendDot(GreenMotile); Spacer(Modifier.width(4.dp))
            Text("Progressive", fontSize = 10.sp, color = TextDim)
            Spacer(Modifier.width(14.dp))
            LegendDot(CyanPrimary); Spacer(Modifier.width(4.dp))
            Text("Non-Progressive", fontSize = 10.sp, color = TextDim)
            Spacer(Modifier.width(14.dp))
            LegendDot(Color(0xFF4A6070)); Spacer(Modifier.width(4.dp))
            Text("Immotile", fontSize = 10.sp, color = TextDim)
        }
    }
}

@Composable
private fun LegendDot(color: Color) {
    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(color)
    )
}
