package com.yaksperm.analyzer.presentation.screens.newanalysis

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class NewAnalysisUiState(
    val sampleId: String = "",
    val technicianName: String = "",
    val selectedVideoUri: Uri? = null,
    val durationOption: String = "Full video",
    val frameEnhancement: Boolean = true,
    val canBegin: Boolean = false
)

@HiltViewModel
class NewAnalysisViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(NewAnalysisUiState())
    val uiState: StateFlow<NewAnalysisUiState> = _uiState.asStateFlow()

    init {
        // Auto-generate a sample ID suggestion
        val datePart = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        _uiState.value = _uiState.value.copy(sampleId = "YK-$datePart-001")
    }

    fun onSampleIdChanged(v: String) {
        _uiState.value = _uiState.value.copy(sampleId = v)
        updateCanBegin()
    }

    fun onTechnicianChanged(v: String) {
        _uiState.value = _uiState.value.copy(technicianName = v)
    }

    fun onVideoSelected(uri: Uri) {
        _uiState.value = _uiState.value.copy(selectedVideoUri = uri)
        updateCanBegin()
    }

    fun onDurationChanged(v: String) {
        _uiState.value = _uiState.value.copy(durationOption = v)
    }

    fun onFrameEnhancementToggled(v: Boolean) {
        _uiState.value = _uiState.value.copy(frameEnhancement = v)
    }

    private fun updateCanBegin() {
        _uiState.value = _uiState.value.copy(
            canBegin = _uiState.value.selectedVideoUri != null &&
                       _uiState.value.sampleId.isNotBlank()
        )
    }
}
