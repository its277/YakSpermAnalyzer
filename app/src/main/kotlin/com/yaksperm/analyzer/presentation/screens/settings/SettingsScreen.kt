package com.yaksperm.analyzer.presentation.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.yaksperm.analyzer.Constants
import com.yaksperm.analyzer.presentation.components.BottomNavBar
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
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = NavyBg,
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold, color = TextPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyBg)
            )
        },
        bottomBar = { BottomNavBar(navController) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // Analysis Section
            SectionCard("Analysis Parameters") {
                SettingTextField(
                    label = "Min Track Length",
                    value = state.minTrackLength,
                    onValueChange = { viewModel.onMinTrackLengthChanged(it) }
                )
                SettingTextField(
                    label = "Confidence Threshold",
                    value = state.confidenceThresh,
                    onValueChange = { viewModel.onConfidenceThreshChanged(it) }
                )
                SettingTextField(
                    label = "IOU Threshold",
                    value = state.iouThresh,
                    onValueChange = { viewModel.onIouThreshChanged(it) }
                )
            }

            Spacer(Modifier.height(16.dp))

            // Calibration Section
            SectionCard("Calibration") {
                SettingTextField(
                    label = "Pixel-to-µm Ratio",
                    value = state.pixelToUm,
                    onValueChange = { viewModel.onPixelToUmChanged(it) }
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Objective Magnification", color = TextPrimary)
                    Text("10×", color = TextDim, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Report Section
            SectionCard("PDF Report") {
                SettingTextField(
                    label = "Institution Name",
                    value = state.institutionName,
                    onValueChange = { viewModel.onInstitutionNameChanged(it) }
                )
                SettingTextField(
                    label = "Report Footer Text",
                    value = state.reportFooter,
                    onValueChange = { viewModel.onReportFooterChanged(it) }
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Include Logo", color = TextPrimary)
                    Switch(
                        checked = state.includeLogo,
                        onCheckedChange = { viewModel.onIncludeLogoChanged(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NavyBg,
                            checkedTrackColor = CyanPrimary
                        )
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Model Section
            SectionCard("Inference Engine") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Model Name", color = TextPrimary)
                    Text(Constants.MODEL_FILE_NAME, color = TextDim)
                }
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Runtime", color = TextPrimary)
                    Text("TensorFlow Lite", color = TextDim)
                }
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Acceleration", color = TextPrimary)
                    Text("GPU/NNAPI (Auto)", color = TextDim)
                }
            }
            
            Spacer(Modifier.height(16.dp))

            // About Section
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("YakSperm Analyzer v${Constants.APP_VERSION}", color = TextDim, fontSize = 12.sp)
                Text("Build Date: ${Constants.BUILD_DATE}", color = TextDim, fontSize = 12.sp)
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
private fun SettingTextField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = TextDim) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CyanPrimary,
            unfocusedBorderColor = Border,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            cursorColor = CyanPrimary
        )
    )
}
