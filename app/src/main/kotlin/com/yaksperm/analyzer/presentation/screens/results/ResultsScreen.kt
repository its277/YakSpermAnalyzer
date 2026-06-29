package com.yaksperm.analyzer.presentation.screens.results

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.yaksperm.analyzer.domain.model.CasaResult
import com.yaksperm.analyzer.pdf.ReportGenerator
import com.yaksperm.analyzer.presentation.components.CasaParameterCard
import com.yaksperm.analyzer.presentation.components.CircularProgressArc
import com.yaksperm.analyzer.presentation.components.GradeTag
import com.yaksperm.analyzer.presentation.components.TrackVisualizationCanvas
import com.yaksperm.analyzer.presentation.theme.AmberCount
import com.yaksperm.analyzer.presentation.theme.Border
import com.yaksperm.analyzer.presentation.theme.CardBg
import com.yaksperm.analyzer.presentation.theme.CyanPrimary
import com.yaksperm.analyzer.presentation.theme.GreenMotile
import com.yaksperm.analyzer.presentation.theme.NavyBg
import com.yaksperm.analyzer.presentation.theme.TextDim
import com.yaksperm.analyzer.presentation.theme.TextPrimary
import com.yaksperm.analyzer.presentation.theme.TextSecond
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(
    navController: NavController,
    resultId: Long,
    viewModel: ResultsViewModel = hiltViewModel()
) {
    val result by viewModel.result.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableIntStateOf(0) }
    var isPdfGenerating by remember { mutableStateOf(false) }

    LaunchedEffect(resultId) { viewModel.loadResult(resultId) }

    Scaffold(
        containerColor = NavyBg,
        topBar = {
            TopAppBar(
                title = { Text("Analysis Results", fontWeight = FontWeight.Bold, color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, null, tint = TextSecond)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyBg)
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CyanPrimary)
            }
            return@Scaffold
        }

        val res = result ?: run {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Result not found", color = TextDim)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // ── Header card ────────────────────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                res.sampleId.ifBlank { "Unknown Sample" },
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = TextPrimary
                            )
                            Text(
                                SimpleDateFormat("dd MMM yyyy  HH:mm", Locale.getDefault())
                                    .format(Date(res.timestamp)),
                                style = MaterialTheme.typography.bodySmall,
                                color = TextDim
                            )
                            if (res.technicianName.isNotBlank()) {
                                Text(res.technicianName, style = MaterialTheme.typography.bodySmall, color = TextDim)
                            }
                        }
                        GradeTag(res.grade)
                    }

                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        CircularProgressArc(
                            value = res.totalMotilityPct,
                            color = CyanPrimary,
                            label = "Motility"
                        )
                        CircularProgressArc(
                            value = res.progressiveMotilityPct,
                            color = GreenMotile,
                            label = "Progressive"
                        )
                        CircularProgressArc(
                            value = res.totalTracked.toFloat(),
                            max = res.totalDetected.coerceAtLeast(1).toFloat(),
                            color = AmberCount,
                            label = "Tracked",
                            unit = ""
                        )
                    }
                }
            }

            // ── Tab row ────────────────────────────────────────────────────────
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = NavyBg,
                contentColor = CyanPrimary,
                edgePadding = 16.dp
            ) {
                listOf("Kinematics", "Motility", "Tracks").forEachIndexed { idx, label ->
                    Tab(
                        selected = selectedTab == idx,
                        onClick = { selectedTab = idx },
                        text = {
                            Text(
                                label,
                                color = if (selectedTab == idx) CyanPrimary else TextDim
                            )
                        }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            when (selectedTab) {
                0 -> KinematicsTab(res)
                1 -> MotilityTab(res)
                2 -> TracksTab()
            }

            Spacer(Modifier.height(16.dp))

            // ── Action Buttons ─────────────────────────────────────────────────
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Button(
                    onClick = {
                        scope.launch {
                            isPdfGenerating = true
                            try {
                                val pdfFile = withContext(Dispatchers.IO) {
                                    ReportGenerator(context).generate(res)
                                }
                                viewModel.updatePdfPath(resultId, pdfFile.absolutePath)
                                val uri = FileProvider.getUriForFile(
                                    context, "${context.packageName}.fileprovider", pdfFile
                                )
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(uri, "application/pdf")
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "PDF failed: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                            isPdfGenerating = false
                        }
                    },
                    enabled = !isPdfGenerating,
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.PictureAsPdf, null, modifier = Modifier.size(18.dp))
                    Text("  Export PDF", fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { navController.navigate("new_analysis") },
                    border = androidx.compose.foundation.BorderStroke(1.dp, Border),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("New Analysis", color = TextSecond)
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun KinematicsTab(res: CasaResult) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        // Velocity card
        Card(
            colors = CardDefaults.cardColors(containerColor = CardBg),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Velocity Parameters", fontWeight = FontWeight.SemiBold, color = CyanPrimary)
                Spacer(Modifier.height(8.dp))
                CasaParameterCard("VCL", "Curvilinear Velocity",   res.vclMean, "µm/s", CyanPrimary)
                CasaParameterCard("VAP", "Average Path Velocity",  res.vapMean, "µm/s", CyanPrimary)
                CasaParameterCard("VSL", "Straight-Line Velocity", res.vslMean, "µm/s", CyanPrimary)
            }
        }

        Spacer(Modifier.height(12.dp))

        // Linearity card
        Card(
            colors = CardDefaults.cardColors(containerColor = CardBg),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Linearity Ratios", fontWeight = FontWeight.SemiBold, color = AmberCount)
                Spacer(Modifier.height(8.dp))
                CasaParameterCard("LIN", "Linearity (VSL/VCL)",    res.linMean, "%", AmberCount)
                CasaParameterCard("STR", "Straightness (VSL/VAP)", res.strMean, "%", AmberCount)
                CasaParameterCard("WOB", "Wobble (VAP/VCL)",       res.wobMean, "%", AmberCount)
            }
        }
    }
}

@Composable
private fun MotilityTab(res: CasaResult) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Card(
            colors = CardDefaults.cardColors(containerColor = CardBg),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                MotilityBar("Progressive",     res.progressiveMotilityPct,    GreenMotile)
                Spacer(Modifier.height(12.dp))
                MotilityBar("Non-Progressive", res.nonProgressiveMotilityPct, CyanPrimary)
                Spacer(Modifier.height(12.dp))
                MotilityBar("Immotile",        100f - res.totalMotilityPct,   Color(0xFF4A6070))
            }
        }

        Spacer(Modifier.height(12.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = CardBg),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    CountItem("Total Detected", res.totalDetected, TextDim)
                    CountItem("Tracked", res.totalTracked, CyanPrimary)
                    CountItem("Progressive", res.progressiveMotile, GreenMotile)
                    CountItem("Non-Prog", res.nonProgressiveMotile, AmberCount)
                }
            }
        }
    }
}

@Composable
private fun MotilityBar(label: String, pct: Float, color: Color) {
    Column {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
            Text(label, color = color, fontWeight = FontWeight.Medium, fontSize = 13.sp)
            Text("${"%.1f".format(pct)}%", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .background(Border, RoundedCornerShape(5.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(pct / 100f)
                    .height(10.dp)
                    .background(color, RoundedCornerShape(5.dp))
            )
        }
    }
}

@Composable
private fun CountItem(label: String, value: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$value", color = color, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextDim)
    }
}

@Composable
private fun TracksTab() {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Card(
            colors = CardDefaults.cardColors(containerColor = CardBg),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                // TrackVisualizationCanvas requires tracks from processing result
                // In production, pass the track list stored in the analysis session
                Text(
                    "Track visualization available\nafter processing with track data.",
                    color = TextDim,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
