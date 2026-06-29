package com.yaksperm.analyzer.presentation.screens.processing

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.yaksperm.analyzer.domain.model.SpermDetection
import com.yaksperm.analyzer.Constants
import com.yaksperm.analyzer.presentation.theme.AmberCount
import com.yaksperm.analyzer.presentation.theme.Border
import com.yaksperm.analyzer.presentation.theme.CardBg
import com.yaksperm.analyzer.presentation.theme.CyanPrimary
import com.yaksperm.analyzer.presentation.theme.GreenMotile
import com.yaksperm.analyzer.presentation.theme.NavyBg
import com.yaksperm.analyzer.presentation.theme.RedAlert
import com.yaksperm.analyzer.presentation.theme.TextDim
import com.yaksperm.analyzer.presentation.theme.TextPrimary
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ProcessingScreen(
    navController: NavController,
    videoUri: Uri,
    viewModel: ProcessingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showCancelDialog by remember { mutableStateOf(false) }

    // Block back navigation during processing
    BackHandler {
        if (state is ProcessingState.Complete || state is ProcessingState.Error) {
            navController.popBackStack()
        } else {
            showCancelDialog = true
        }
    }

    // Start analysis on first composition
    LaunchedEffect(videoUri) {
        viewModel.startAnalysis(
            videoUri = videoUri,
            sampleId = "",
            technicianName = "",
            frameEnhancement = true
        )
    }

    // Navigate on completion
    LaunchedEffect(state) {
        if (state is ProcessingState.Complete) {
            val resultId = (state as ProcessingState.Complete).resultId
            navController.navigate("results/$resultId") {
                popUpTo("new_analysis") { inclusive = true }
            }
        }
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancel Analysis?", color = TextPrimary) },
            text = { Text("The current analysis will be discarded.", color = TextDim) },
            confirmButton = {
                Button(
                    onClick = { viewModel.cancel(); navController.popBackStack() },
                    colors = ButtonDefaults.buttonColors(containerColor = RedAlert)
                ) { Text("Cancel Analysis") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showCancelDialog = false }) { Text("Continue") }
            },
            containerColor = CardBg
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyBg)
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        // Header
        Text(
            "Analyzing Sample",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = TextPrimary,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            stepLabel(state),
            style = MaterialTheme.typography.bodyMedium,
            color = TextDim,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // ── Frame Preview with OBB overlays ───────────────────────────────────
        val currentBitmap = (state as? ProcessingState.DetectingCells)?.currentFrameBitmap
        val currentDetections = (state as? ProcessingState.DetectingCells)?.currentDetections ?: emptyList()

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF050E1A))
                .border(1.dp, Border, RoundedCornerShape(14.dp))
        ) {
            if (currentBitmap != null) {
                androidx.compose.foundation.Image(
                    bitmap = androidx.compose.ui.graphics.asImageBitmap(currentBitmap),
                    contentDescription = "Frame preview",
                    modifier = Modifier.fillMaxSize()
                )
                // OBB overlay
                DetectionOverlay(currentDetections)
            } else {
                ScanningAnimation()
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Step Pipeline Indicator ────────────────────────────────────────────
        PipelineIndicator(state)

        Spacer(Modifier.height(16.dp))

        // ── Progress Bar ───────────────────────────────────────────────────────
        val progress = when (val s = state) {
            is ProcessingState.DetectingCells -> s.current.toFloat() / s.total.toFloat()
            is ProcessingState.ComputingMetrics, is ProcessingState.SavingResults -> 1f
            else -> 0f
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = CyanPrimary,
            trackColor = Border
        )
        Text(
            "${(progress * 100).toInt()}%",
            style = MaterialTheme.typography.labelSmall,
            color = TextDim,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(Modifier.height(16.dp))

        // ── Live Stats ─────────────────────────────────────────────────────────
        val detState = state as? ProcessingState.DetectingCells
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LiveStatCard(
                "Frames",
                "${detState?.current ?: 0} / ${detState?.total ?: 0}",
                CyanPrimary,
                Modifier.weight(1f)
            )
            LiveStatCard(
                "Detected",
                "${detState?.cellCount ?: 0}",
                AmberCount,
                Modifier.weight(1f)
            )
            LiveStatCard(
                "Tracks",
                "${detState?.activeTracks ?: 0}",
                GreenMotile,
                Modifier.weight(1f)
            )
        }

        Spacer(Modifier.weight(1f))

        // ── Error display ──────────────────────────────────────────────────────
        (state as? ProcessingState.Error)?.let { errState ->
            Text(
                "Error: ${errState.message}",
                color = RedAlert,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Button(
                onClick = { navController.popBackStack() },
                colors = ButtonDefaults.buttonColors(containerColor = CardBg),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Go Back") }
        }

        // ── Cancel Button ──────────────────────────────────────────────────────
        if (state !is ProcessingState.Complete && state !is ProcessingState.Error) {
            OutlinedButton(
                onClick = { showCancelDialog = true },
                border = androidx.compose.foundation.BorderStroke(1.dp, RedAlert.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Cancel, null, tint = RedAlert, modifier = Modifier.size(16.dp))
                Text("  Cancel", color = RedAlert)
            }
        }
    }
}

// ── Supporting Composables ─────────────────────────────────────────────────────

@Composable
private fun PipelineIndicator(state: ProcessingState) {
    val steps = listOf("Extract", "Detect", "Track", "Metrics")
    val activeStep = when (state) {
        is ProcessingState.ExtractingFrames -> 0
        is ProcessingState.DetectingCells   -> 1
        is ProcessingState.ComputingMetrics -> 3
        is ProcessingState.SavingResults    -> 3
        else -> -1
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        steps.forEachIndexed { idx, label ->
            val isActive = idx == activeStep
            val isDone   = idx < activeStep
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isActive -> CyanPrimary
                                isDone   -> GreenMotile.copy(alpha = 0.6f)
                                else     -> Border
                            }
                        )
                ) {
                    Text(
                        "${idx + 1}",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
                Text(
                    label,
                    fontSize = 9.sp,
                    color = if (isActive) CyanPrimary else TextDim,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun DetectionOverlay(detections: List<SpermDetection>) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        detections.forEach { det ->
            val cx = det.x / Constants.MODEL_INPUT_SIZE.toFloat() * size.width
            val cy = det.y / Constants.MODEL_INPUT_SIZE.toFloat() * size.height
            val w  = det.width / Constants.MODEL_INPUT_SIZE.toFloat() * size.width
            val h  = det.height / Constants.MODEL_INPUT_SIZE.toFloat() * size.height
            val angleRad = Math.toRadians(det.angle.toDouble()).toFloat()

            // Draw 4 rotated corners
            val corners = listOf(
                Offset(-w / 2, -h / 2), Offset(w / 2, -h / 2),
                Offset(w / 2, h / 2),   Offset(-w / 2, h / 2)
            ).map { (ox, oy) ->
                Offset(
                    cx + ox * cos(angleRad) - oy * sin(angleRad),
                    cy + ox * sin(angleRad) + oy * cos(angleRad)
                )
            }

            for (i in corners.indices) {
                drawLine(
                    color = CyanPrimary.copy(alpha = 0.85f),
                    start = corners[i],
                    end = corners[(i + 1) % 4],
                    strokeWidth = 1.5f,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

@Composable
private fun ScanningAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "scan")
    val scanY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1600, easing = LinearEasing), RepeatMode.Restart),
        label = "scanLine"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawLine(
            color = CyanPrimary.copy(alpha = 0.6f),
            start = Offset(0f, size.height * scanY),
            end   = Offset(size.width, size.height * scanY),
            strokeWidth = 2f
        )
    }
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        Text("Extracting frames…", color = TextDim, fontSize = 12.sp)
    }
}

@Composable
private fun LiveStatCard(label: String, value: String, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, color = color, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextDim)
        }
    }
}

private fun stepLabel(state: ProcessingState) = when (state) {
    is ProcessingState.Idle             -> "Preparing…"
    is ProcessingState.ExtractingFrames -> "Extracting video frames"
    is ProcessingState.DetectingCells   -> "Detecting & tracking sperm cells"
    is ProcessingState.ComputingMetrics -> "Computing CASA parameters"
    is ProcessingState.SavingResults    -> "Saving results"
    is ProcessingState.Complete         -> "Done! Navigating to results…"
    is ProcessingState.Error            -> "Analysis failed"
}

private val Constants.MODEL_INPUT_SIZE get() = com.yaksperm.analyzer.Constants.MODEL_INPUT_SIZE
