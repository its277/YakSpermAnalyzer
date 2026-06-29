package com.yaksperm.analyzer.presentation.screens.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.yaksperm.analyzer.domain.model.AnalysisSession
import com.yaksperm.analyzer.presentation.components.BottomNavBar
import com.yaksperm.analyzer.presentation.components.GradeTag
import com.yaksperm.analyzer.presentation.theme.AmberCount
import com.yaksperm.analyzer.presentation.theme.Border
import com.yaksperm.analyzer.presentation.theme.CardBg
import com.yaksperm.analyzer.presentation.theme.CyanPrimary
import com.yaksperm.analyzer.presentation.theme.NavyBg
import com.yaksperm.analyzer.presentation.theme.RedAlert
import com.yaksperm.analyzer.presentation.theme.TextDim
import com.yaksperm.analyzer.presentation.theme.TextPrimary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    navController: NavController,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val sessions by viewModel.sessions.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = NavyBg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Analysis History", fontWeight = FontWeight.Bold, color = TextPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyBg)
            )
        },
        bottomBar = { BottomNavBar(navController) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                placeholder = { Text("Search by Sample ID", color = TextDim) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = TextDim) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanPrimary,
                    unfocusedBorderColor = Border,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )
            
            if (sessions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No results found.", color = TextDim)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(sessions, key = { it.id }) { session ->
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { dismissValue ->
                                if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                    viewModel.deleteSession(session.id)
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Analysis deleted")
                                    }
                                    true
                                } else false
                            }
                        )

                        SwipeToDismissBox(
                            state = dismissState,
                            backgroundContent = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(vertical = 4.dp)
                                        .background(RedAlert, RoundedCornerShape(12.dp))
                                        .padding(end = 20.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color.White)
                                }
                            },
                            enableDismissFromStartToEnd = false
                        ) {
                            HistoryCard(session) {
                                navController.navigate("results/${session.id}")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryCard(session: AnalysisSession, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        session.sampleId.ifBlank { "Unknown Sample" },
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )
                    Text(
                        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(session.timestamp)),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextDim
                    )
                }
                GradeTag(session.grade)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                MiniStat("Motility", "${session.totalMotilityPct.toInt()}%", CyanPrimary)
                MiniStat("Progressive", "${session.progressiveMotilityPct.toInt()}%", CyanPrimary)
                MiniStat("Count", "${session.totalTracked}", AmberCount)
            }
        }
    }
}

@Composable
private fun MiniStat(label: String, value: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = valueColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextDim)
    }
}
