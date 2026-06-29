package com.yaksperm.analyzer.presentation.screens.newanalysis

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.yaksperm.analyzer.presentation.theme.Border
import com.yaksperm.analyzer.presentation.theme.CardBg
import com.yaksperm.analyzer.presentation.theme.CyanDim
import com.yaksperm.analyzer.presentation.theme.CyanPrimary
import com.yaksperm.analyzer.presentation.theme.NavyBg
import com.yaksperm.analyzer.presentation.theme.TextDim
import com.yaksperm.analyzer.presentation.theme.TextPrimary
import com.yaksperm.analyzer.presentation.theme.TextSecond

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewAnalysisScreen(
    navController: NavController,
    viewModel: NewAnalysisViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    val videoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { viewModel.onVideoSelected(it) } }

    Scaffold(
        containerColor = NavyBg,
        topBar = {
            TopAppBar(
                title = {
                    Text("New Analysis", fontWeight = FontWeight.Bold, color = TextPrimary)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, null, tint = TextSecond)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyBg)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // ── Sample Info ────────────────────────────────────────────────────
            SectionCard(title = "Sample Information") {
                OutlinedTextField(
                    value = state.sampleId,
                    onValueChange = { viewModel.onSampleIdChanged(it) },
                    label = { Text("Sample ID", color = TextDim) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = state.technicianName,
                    onValueChange = { viewModel.onTechnicianChanged(it) },
                    label = { Text("Technician Name", color = TextDim) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors()
                )
            }

            Spacer(Modifier.height(14.dp))

            // ── Video Source ───────────────────────────────────────────────────
            SectionCard(title = "Video Source") {
                // Gallery option
                VideoSourceOption(
                    icon = Icons.Filled.VideoFile,
                    label = "Import from Gallery",
                    description = "Select a pre-recorded MP4/AVI file",
                    selected = state.selectedVideoUri != null,
                    enabled = true,
                    onClick = { videoPicker.launch("video/*") }
                )
                state.selectedVideoUri?.let { uri ->
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CheckCircle, null, tint = CyanPrimary, modifier = Modifier.size(16.dp))
                        Text(
                            "  ${uri.lastPathSegment ?: "video selected"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = CyanPrimary
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                // USB Camera — future
                VideoSourceOption(
                    icon = Icons.Filled.Usb,
                    label = "USB Camera Feed",
                    description = "Coming in v2.0",
                    selected = false,
                    enabled = false,
                    onClick = {}
                )
            }

            Spacer(Modifier.height(14.dp))

            // ── Analysis Settings ──────────────────────────────────────────────
            SectionCard(title = "Analysis Settings") {
                SettingRow(label = "Motility Grading", value = "Hamilton IVOS-II", locked = true)
                SettingRow(label = "Frame Rate (FPS)", value = "30  (fixed)", locked = true)
                SettingRow(label = "Duration", value = state.durationOption, locked = false) {
                    // Simplified: cycle through options
                    val opts = listOf("15s", "30s", "60s", "Full video")
                    val idx = opts.indexOf(state.durationOption)
                    viewModel.onDurationChanged(opts[(idx + 1) % opts.size])
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Frame Enhancement", color = TextPrimary, fontWeight = FontWeight.Medium)
                        Text("CLAHE + Gaussian denoise", style = MaterialTheme.typography.bodySmall, color = TextDim)
                    }
                    Switch(
                        checked = state.frameEnhancement,
                        onCheckedChange = { viewModel.onFrameEnhancementToggled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NavyBg,
                            checkedTrackColor = CyanPrimary
                        )
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Begin Button ───────────────────────────────────────────────────
            Button(
                onClick = {
                    val uri = state.selectedVideoUri ?: return@Button
                    val encoded = Uri.encode(uri.toString())
                    navController.navigate("processing/$encoded")
                },
                enabled = state.canBegin,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyanPrimary,
                    disabledContainerColor = CyanDim
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(Icons.Filled.PlayArrow, null, modifier = Modifier.size(20.dp))
                Text(
                    "  Begin Analysis",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                title.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = CyanPrimary,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun VideoSourceOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    description: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (selected) CyanPrimary else Border
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .background(if (selected) CyanPrimary.copy(alpha = 0.07f) else Color.Transparent)
            .clickable(enabled = enabled) { onClick() }
            .alpha(if (enabled) 1f else 0.4f)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = if (selected) CyanPrimary else TextDim, modifier = Modifier.size(22.dp))
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(label, color = if (selected) CyanPrimary else TextPrimary, fontWeight = FontWeight.Medium)
            Text(description, style = MaterialTheme.typography.bodySmall, color = TextDim)
        }
    }
}

@Composable
private fun SettingRow(label: String, value: String, locked: Boolean, onClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !locked && onClick != null) { onClick?.invoke() }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = TextPrimary, fontWeight = FontWeight.Medium)
        Text(value, color = if (locked) TextDim else CyanPrimary, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = CyanPrimary,
    unfocusedBorderColor = Border,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    cursorColor = CyanPrimary
)
