package com.yaksperm.analyzer.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yaksperm.analyzer.Constants
import com.yaksperm.analyzer.data.preferences.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val minTrackLength: String  = Constants.MIN_TRACK_LENGTH.toString(),
    val confidenceThresh: String = Constants.CONFIDENCE_THRESHOLD.toString(),
    val iouThresh: String        = Constants.IOU_THRESHOLD.toString(),
    val pixelToUm: String        = Constants.PIXEL_TO_UM.toString(),
    val objectiveName: String    = "10×",
    val institutionName: String  = "Research Institute",
    val reportFooter: String     = "For research use only.",
    val includeLogo: Boolean     = true
)

// Typed intermediates so we never cross the 5-flow combine limit
private data class PrefsPartA(
    val minTrackLength: Int,
    val confidenceThresh: Float,
    val iouThresh: Float,
    val pixelToUm: Float,
    val objectiveName: String
)

private data class PrefsPartB(
    val institutionName: String,
    val reportFooter: String,
    val includeLogo: Boolean
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: AppPreferences
) : ViewModel() {

    private val partA = combine(
        prefs.minTrackLength,
        prefs.confidenceThresh,
        prefs.iouThresh,
        prefs.pixelToUm,
        prefs.objective
    ) { minTrack, conf, iou, pxToUm, obj ->
        PrefsPartA(minTrack, conf, iou, pxToUm, obj)
    }

    private val partB = combine(
        prefs.institutionName,
        prefs.reportFooter,
        prefs.includeLogo
    ) { inst, foot, logo ->
        PrefsPartB(inst, foot, logo)
    }

    val uiState = combine(partA, partB) { a, b ->
        SettingsUiState(
            minTrackLength  = a.minTrackLength.toString(),
            confidenceThresh = a.confidenceThresh.toString(),
            iouThresh       = a.iouThresh.toString(),
            pixelToUm       = a.pixelToUm.toString(),
            objectiveName   = a.objectiveName,
            institutionName = b.institutionName,
            reportFooter    = b.reportFooter,
            includeLogo     = b.includeLogo
        )
    }.stateIn(
        scope         = viewModelScope,
        started       = SharingStarted.WhileSubscribed(5_000),
        initialValue  = SettingsUiState()
    )

    fun onMinTrackLengthChanged(v: String) {
        viewModelScope.launch { v.toIntOrNull()?.let   { prefs.setMinTrackLength(it) } }
    }
    fun onConfidenceThreshChanged(v: String) {
        viewModelScope.launch { v.toFloatOrNull()?.let { prefs.setConfidenceThresh(it) } }
    }
    fun onIouThreshChanged(v: String) {
        viewModelScope.launch { v.toFloatOrNull()?.let { prefs.setIouThresh(it) } }
    }
    fun onPixelToUmChanged(v: String) {
        viewModelScope.launch { v.toFloatOrNull()?.let { prefs.setPixelToUm(it) } }
    }
    fun onObjectiveNameChanged(v: String) {
        viewModelScope.launch { prefs.setObjective(v) }
    }
    fun onInstitutionNameChanged(v: String) {
        viewModelScope.launch { prefs.setInstitutionName(v) }
    }
    fun onReportFooterChanged(v: String) {
        viewModelScope.launch { prefs.setReportFooter(v) }
    }
    fun onIncludeLogoChanged(v: Boolean) {
        viewModelScope.launch { prefs.setIncludeLogo(v) }
    }
}
