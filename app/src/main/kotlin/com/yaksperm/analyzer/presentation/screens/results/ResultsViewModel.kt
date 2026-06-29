package com.yaksperm.analyzer.presentation.screens.results

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yaksperm.analyzer.data.repository.AnalysisRepository
import com.yaksperm.analyzer.domain.model.CasaResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResultsViewModel @Inject constructor(
    private val repository: AnalysisRepository
) : ViewModel() {

    private val _result = MutableStateFlow<CasaResult?>(null)
    val result: StateFlow<CasaResult?> = _result.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadResult(id: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _result.value = repository.getResultById(id)
            _isLoading.value = false
        }
    }

    fun updatePdfPath(id: Long, path: String) {
        viewModelScope.launch {
            repository.updatePdfPath(id, path)
        }
    }
}
