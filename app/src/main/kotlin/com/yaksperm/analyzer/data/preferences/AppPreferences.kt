package com.yaksperm.analyzer.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.yaksperm.analyzer.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "yaksperm_prefs")

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val ds = context.dataStore

    // ── Keys ─────────────────────────────────────────────────────────────────
    object Keys {
        val INSTITUTION_NAME    = stringPreferencesKey("institution_name")
        val REPORT_FOOTER       = stringPreferencesKey("report_footer")
        val INCLUDE_LOGO        = booleanPreferencesKey("include_logo")
        val PIXEL_TO_UM         = floatPreferencesKey("pixel_to_um")
        val CONFIDENCE_THRESH   = floatPreferencesKey("confidence_threshold")
        val IOU_THRESH          = floatPreferencesKey("iou_threshold")
        val MIN_TRACK_LENGTH    = intPreferencesKey("min_track_length")
        val FRAME_ENHANCEMENT   = booleanPreferencesKey("frame_enhancement")
        val OBJECTIVE           = stringPreferencesKey("objective_name")
    }

    // ── Flows ─────────────────────────────────────────────────────────────────
    val institutionName: Flow<String> = ds.data.map { it[Keys.INSTITUTION_NAME] ?: "Research Institute" }
    val reportFooter:    Flow<String> = ds.data.map { it[Keys.REPORT_FOOTER]    ?: "For research use only." }
    val includeLogo:     Flow<Boolean> = ds.data.map { it[Keys.INCLUDE_LOGO]     ?: true }
    val pixelToUm:       Flow<Float>  = ds.data.map { it[Keys.PIXEL_TO_UM]      ?: Constants.PIXEL_TO_UM }
    val confidenceThresh:Flow<Float>  = ds.data.map { it[Keys.CONFIDENCE_THRESH]?: Constants.CONFIDENCE_THRESHOLD }
    val iouThresh:       Flow<Float>  = ds.data.map { it[Keys.IOU_THRESH]       ?: Constants.IOU_THRESHOLD }
    val minTrackLength:  Flow<Int>    = ds.data.map { it[Keys.MIN_TRACK_LENGTH]  ?: Constants.MIN_TRACK_LENGTH }
    val frameEnhancement:Flow<Boolean> = ds.data.map { it[Keys.FRAME_ENHANCEMENT]?: true }
    val objective:       Flow<String> = ds.data.map { it[Keys.OBJECTIVE]         ?: "10×" }

    // ── Setters ───────────────────────────────────────────────────────────────
    suspend fun setInstitutionName(v: String)  = ds.edit { it[Keys.INSTITUTION_NAME]  = v }
    suspend fun setReportFooter(v: String)     = ds.edit { it[Keys.REPORT_FOOTER]     = v }
    suspend fun setIncludeLogo(v: Boolean)     = ds.edit { it[Keys.INCLUDE_LOGO]      = v }
    suspend fun setPixelToUm(v: Float)         = ds.edit { it[Keys.PIXEL_TO_UM]       = v }
    suspend fun setConfidenceThresh(v: Float)  = ds.edit { it[Keys.CONFIDENCE_THRESH] = v }
    suspend fun setIouThresh(v: Float)         = ds.edit { it[Keys.IOU_THRESH]        = v }
    suspend fun setMinTrackLength(v: Int)      = ds.edit { it[Keys.MIN_TRACK_LENGTH]  = v }
    suspend fun setFrameEnhancement(v: Boolean)= ds.edit { it[Keys.FRAME_ENHANCEMENT] = v }
    suspend fun setObjective(v: String)        = ds.edit { it[Keys.OBJECTIVE]         = v }
}
