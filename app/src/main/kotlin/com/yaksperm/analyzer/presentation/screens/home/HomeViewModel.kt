package com.yaksperm.analyzer.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yaksperm.analyzer.data.repository.AnalysisRepository
import com.yaksperm.analyzer.domain.model.AnalysisSession
import com.yaksperm.analyzer.domain.model.CasaResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

data class HomeUiState(
    val totalCount: Int = 0,
    val monthCount: Int = 0,
    val avgMotility: Float = 0f,
    val latestResult: CasaResult? = null,
    val recentSessions: List<AnalysisSession> = emptyList()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: AnalysisRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadStats()
        observeSessions()
    }

    private fun loadStats() {
        viewModelScope.launch {
            val total = repository.getTotalCount()
            val startOfMonth = LocalDate.now()
                .withDayOfMonth(1)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
            val month = repository.getCountSince(startOfMonth)
            val avg = repository.getAverageMotility()
            val latest = repository.getLatestResult()

            _uiState.value = _uiState.value.copy(
                totalCount = total,
                monthCount = month,
                avgMotility = avg,
                latestResult = latest
            )
        }
    }

    private fun observeSessions() {
        viewModelScope.launch {
            repository.getAllSessions().collect { sessions ->
                _uiState.value = _uiState.value.copy(
                    recentSessions = sessions.take(3)
                )
                // Refresh stats whenever sessions change
                loadStats()
            }
        }
    }
}
