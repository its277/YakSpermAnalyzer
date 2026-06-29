package com.yaksperm.analyzer.presentation.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yaksperm.analyzer.data.repository.AnalysisRepository
import com.yaksperm.analyzer.domain.model.AnalysisSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: AnalysisRepository
) : ViewModel() {

    private val _sessions = MutableStateFlow<List<AnalysisSession>>(emptyList())
    val sessions: StateFlow<List<AnalysisSession>> = _sessions.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private var allSessions: List<AnalysisSession> = emptyList()

    init {
        viewModelScope.launch {
            repository.getAllSessions().collect { list ->
                allSessions = list
                applyFilter()
            }
        }
    }

    fun onSearchQueryChanged(q: String) {
        _searchQuery.value = q
        applyFilter()
    }

    fun deleteSession(id: Long) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }

    private fun applyFilter() {
        val q = _searchQuery.value.trim().lowercase()
        _sessions.value = if (q.isEmpty()) allSessions
                          else allSessions.filter { it.sampleId.lowercase().contains(q) }
    }
}
