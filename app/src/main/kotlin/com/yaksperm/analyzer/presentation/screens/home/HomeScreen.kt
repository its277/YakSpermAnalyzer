package com.yaksperm.analyzer.presentation.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.yaksperm.analyzer.presentation.components.BottomNavBar
import com.yaksperm.analyzer.presentation.components.CircularProgressArc
import com.yaksperm.analyzer.presentation.components.GradeTag
import com.yaksperm.analyzer.presentation.theme.AmberCount
import com.yaksperm.analyzer.presentation.theme.Border
import com.yaksperm.analyzer.presentation.theme.CardAlt
import com.yaksperm.analyzer.presentation.theme.CardBg
import com.yaksperm.analyzer.presentation.theme.CyanDim
import com.yaksperm.analyzer.presentation.theme.CyanGlow
import com.yaksperm.analyzer.presentation.theme.CyanPrimary
import com.yaksperm.analyzer.presentation.theme.GreenMotile
import com.yaksperm.analyzer.presentation.theme.NavyBg
import com.yaksperm.analyzer.presentation.theme.TextDim
import com.yaksperm.analyzer.presentation.theme.TextPrimary
import com.yaksperm.analyzer.presentation.theme.TextSecond
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = NavyBg,
        bottomBar = { BottomNavBar(navController) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
        ) {
            // ── Top Bar ───────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Biotech,
                    contentDescription = null,
                    tint = CyanPrimary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        "YakSperm Analyzer",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextPrimary
                    )
                    Text(
                        "CASA v1.0 — Offline",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyanPrimary
                    )
                }
            }

            // ── Hero gradient card ─────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF0A2040), Color(0xFF051525))
                        )
                    )
                    .padding(24.dp)
            ) {
                Column {
                    Text(
                        "Ready for Analysis",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = TextPrimary
                    )
                    Text(
                        "Import a microscopy video to begin CASA",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecond,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = { navController.navigate("new_analysis") },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Add, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "New Analysis",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = { navController.navigate("history") },
                        border = androidx.compose.foundation.BorderStroke(1.dp, Border),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("View History", color = TextSecond)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Last Result Card ──────────────────────────────────────────────
            state.latestResult?.let { latest ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clickable { /* navigate to result */ },
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Last Result",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TextDim
                                )
                                Text(
                                    latest.sampleId.ifBlank { "Unknown Sample" },
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    fontSize = 16.sp
                                )
                                Text(
                                    SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                                        .format(Date(latest.timestamp)),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextDim
                                )
                            }
                            GradeTag(latest.grade)
                        }

                        Spacer(Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            CircularProgressArc(
                                value = latest.totalMotilityPct,
                                color = CyanPrimary,
                                label = "Motility"
                            )
                            CircularProgressArc(
                                value = latest.progressiveMotilityPct,
                                color = GreenMotile,
                                label = "Progressive"
                            )
                            CircularProgressArc(
                                value = latest.totalTracked.toFloat(),
                                max = latest.totalDetected.coerceAtLeast(1).toFloat(),
                                color = AmberCount,
                                label = "Tracked",
                                unit = ""
                            )
                        }
                    }
                }
            } ?: run {
                // Empty state
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No analyses yet", color = TextDim, fontWeight = FontWeight.Medium)
                        Text(
                            "Run your first analysis to see results here.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextDim,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Stats Row ─────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatChip(
                    label = "Total Analyses",
                    value = state.totalCount.toString(),
                    color = CyanPrimary,
                    modifier = Modifier.weight(1f)
                )
                StatChip(
                    label = "This Month",
                    value = state.monthCount.toString(),
                    color = GreenMotile,
                    modifier = Modifier.weight(1f)
                )
                StatChip(
                    label = "Avg Motility",
                    value = "%.0f%%".format(state.avgMotility),
                    color = AmberCount,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StatChip(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = CardAlt),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, color = color, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextDim)
        }
    }
}
