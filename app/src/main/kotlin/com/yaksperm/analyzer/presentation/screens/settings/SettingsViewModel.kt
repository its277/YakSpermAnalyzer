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
    val minTrackLength: String = Constants.MIN_TRACK_LENGTH.toString(),
    val confidenceThresh: String = Constants.CONFIDENCE_THRESHOLD.toString(),
    val iouThresh: String = Constants.IOU_THRESHOLD.toString(),
    val pixelToUm: String = Constants.PIXEL_TO_UM.toString(),
    val objectiveName: String = "10×",
    val institutionName: String = "Research Institute",
    val reportFooter: String = "For research use only.",
    val includeLogo: Boolean = true
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: AppPreferences
) : ViewModel() {

    val uiState = combine(
        prefs.minTrackLength,
        prefs.confidenceThresh,
        prefs.iouThresh,
        prefs.pixelToUm,
        prefs.objective,
        prefs.institutionName,
        prefs.reportFooter,
        prefs.includeLogo
    ) { minTrack, conf, iou, pxToUm, obj, inst, foot, logo ->
        SettingsUiState(
            minTrackLength = minTrack.toString(),
            confidenceThresh = conf.toString(),
            iouThresh = iou.toString(),
            pixelToUm = pxToUm.toString(),
            objectiveName = obj,
            institutionName = inst,
            reportFooter = foot,
            includeLogo = logo
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun onMinTrackLengthChanged(v: String) {
        viewModelScope.launch {
            v.toIntOrNull()?.let { prefs.setMinTrackLength(it) }
        }
    }

    fun onConfidenceThreshChanged(v: String) {
        viewModelScope.launch {
            v.toFloatOrNull()?.let { prefs.setConfidenceThresh(it) }
        }
    }

    fun onIouThreshChanged(v: String) {
        viewModelScope.launch {
            v.toFloatOrNull()?.let { prefs.setIouThresh(it) }
        }
    }

    fun onPixelToUmChanged(v: String) {
        viewModelScope.launch {
            v.toFloatOrNull()?.let { prefs.setPixelToUm(it) }
        }
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
