package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

enum class ThemePreference {
    SYSTEM, LIGHT, DARK
}

data class SavedCalculatorPrefs(
    val cropName: String? = null,
    val areaInput: String? = null,
    val areaUnitName: String? = null,
    val stageName: String? = null,
    val nitrogenPpm: String? = null,
    val phosphorusPpm: String? = null,
    val potassiumPpm: String? = null,
    val soilPh: Float? = null,
    val soilEc: String? = null,
    val irrigationName: String? = null,
    val climateName: String? = null
)

private val Context.dataStore by preferencesDataStore(name = "agripro_settings")

class ThemePreferencesRepository(private val context: Context) {
    private val themeKey = stringPreferencesKey("theme_preference")
    private val seededSamplesKey = booleanPreferencesKey("has_seeded_samples")

    private val calcCropKey = stringPreferencesKey("calc_crop")
    private val calcAreaKey = stringPreferencesKey("calc_area")
    private val calcAreaUnitKey = stringPreferencesKey("calc_area_unit")
    private val calcStageKey = stringPreferencesKey("calc_stage")
    private val calcNKey = stringPreferencesKey("calc_n")
    private val calcPKey = stringPreferencesKey("calc_p")
    private val calcKKey = stringPreferencesKey("calc_k")
    private val calcPhKey = floatPreferencesKey("calc_ph")
    private val calcEcKey = stringPreferencesKey("calc_ec")
    private val calcIrrigationKey = stringPreferencesKey("calc_irrigation")
    private val calcClimateKey = stringPreferencesKey("calc_climate")

    val themePreference: Flow<ThemePreference> = context.dataStore.data.map { prefs ->
        when (prefs[themeKey]) {
            ThemePreference.LIGHT.name -> ThemePreference.LIGHT
            ThemePreference.DARK.name -> ThemePreference.DARK
            else -> ThemePreference.SYSTEM
        }
    }

    suspend fun setThemePreference(preference: ThemePreference) {
        context.dataStore.edit { prefs ->
            prefs[themeKey] = preference.name
        }
    }

    suspend fun hasSeededSamples(): Boolean {
        return context.dataStore.data.map { prefs -> prefs[seededSamplesKey] ?: false }.first()
    }

    suspend fun markSamplesSeeded() {
        context.dataStore.edit { prefs ->
            prefs[seededSamplesKey] = true
        }
    }

    suspend fun loadCalculatorPrefs(): SavedCalculatorPrefs {
        return context.dataStore.data.map { prefs ->
            SavedCalculatorPrefs(
                cropName = prefs[calcCropKey],
                areaInput = prefs[calcAreaKey],
                areaUnitName = prefs[calcAreaUnitKey],
                stageName = prefs[calcStageKey],
                nitrogenPpm = prefs[calcNKey],
                phosphorusPpm = prefs[calcPKey],
                potassiumPpm = prefs[calcKKey],
                soilPh = prefs[calcPhKey],
                soilEc = prefs[calcEcKey],
                irrigationName = prefs[calcIrrigationKey],
                climateName = prefs[calcClimateKey]
            )
        }.first()
    }

    suspend fun saveCalculatorPrefs(saved: SavedCalculatorPrefs) {
        context.dataStore.edit { prefs ->
            saved.cropName?.let { prefs[calcCropKey] = it }
            saved.areaInput?.let { prefs[calcAreaKey] = it }
            saved.areaUnitName?.let { prefs[calcAreaUnitKey] = it }
            saved.stageName?.let { prefs[calcStageKey] = it }
            saved.nitrogenPpm?.let { prefs[calcNKey] = it }
            saved.phosphorusPpm?.let { prefs[calcPKey] = it }
            saved.potassiumPpm?.let { prefs[calcKKey] = it }
            saved.soilPh?.let { prefs[calcPhKey] = it }
            saved.soilEc?.let { prefs[calcEcKey] = it }
            saved.irrigationName?.let { prefs[calcIrrigationKey] = it }
            saved.climateName?.let { prefs[calcClimateKey] = it }
        }
    }
}
