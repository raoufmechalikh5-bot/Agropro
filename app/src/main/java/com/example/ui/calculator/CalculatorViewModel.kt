package com.example.ui.calculator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.catalog.AgronomyCalculationInput
import com.example.data.catalog.AgronomyCalculationResult
import com.example.data.catalog.AgronomyCalculator
import com.example.data.catalog.AreaUnit
import com.example.data.catalog.ClimateZone
import com.example.data.catalog.CropProfile
import com.example.data.catalog.GrowthStage
import com.example.data.catalog.IrrigationMethod
import com.example.data.local.SavedCalculatorPrefs
import com.example.data.local.ThemePreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CalculatorUiState(
    val selectedCrop: CropProfile = CropProfile.OLIVE,
    val areaInput: String = "2.5",
    val areaUnit: AreaUnit = AreaUnit.HECTARE,
    val selectedStage: GrowthStage = GrowthStage.FLOWERING,
    val nitrogenPpmText: String = "28",
    val phosphorusPpmText: String = "16",
    val potassiumPpmText: String = "145",
    val soilPh: Float = 7.7f,
    val soilEcText: String = "2.1",
    val irrigationMethod: IrrigationMethod = IrrigationMethod.DRIP,
    val climateZone: ClimateZone = ClimateZone.SEMI_ARID,
    val result: AgronomyCalculationResult = AgronomyCalculator.calculate(
        AgronomyCalculationInput(
            crop = CropProfile.OLIVE,
            areaValue = 2.5,
            areaUnit = AreaUnit.HECTARE,
            stage = GrowthStage.FLOWERING,
            soilNitrogenPpm = 28.0,
            soilPhosphorusPpm = 16.0,
            soilPotassiumPpm = 145.0,
            soilPh = 7.7,
            soilEcDsM = 2.1,
            irrigationMethod = IrrigationMethod.DRIP,
            climateZone = ClimateZone.SEMI_ARID
        )
    )
)

class CalculatorViewModel(
    private val prefsRepo: ThemePreferencesRepository? = null
) : ViewModel() {
    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    init {
        if (prefsRepo != null) {
            viewModelScope.launch {
                val saved = prefsRepo.loadCalculatorPrefs()
                _uiState.update { prev ->
                    val next = prev.copy(
                        selectedCrop = saved.cropName?.let { name ->
                            CropProfile.entries.firstOrNull { it.name == name }
                        } ?: prev.selectedCrop,
                        areaInput = saved.areaInput ?: prev.areaInput,
                        areaUnit = saved.areaUnitName?.let { name ->
                            AreaUnit.entries.firstOrNull { it.name == name }
                        } ?: prev.areaUnit,
                        selectedStage = saved.stageName?.let { name ->
                            GrowthStage.entries.firstOrNull { it.name == name }
                        } ?: prev.selectedStage,
                        nitrogenPpmText = saved.nitrogenPpm ?: prev.nitrogenPpmText,
                        phosphorusPpmText = saved.phosphorusPpm ?: prev.phosphorusPpmText,
                        potassiumPpmText = saved.potassiumPpm ?: prev.potassiumPpmText,
                        soilPh = saved.soilPh ?: prev.soilPh,
                        soilEcText = saved.soilEc ?: prev.soilEcText,
                        irrigationMethod = saved.irrigationName?.let { name ->
                            IrrigationMethod.entries.firstOrNull { it.name == name }
                        } ?: prev.irrigationMethod,
                        climateZone = saved.climateName?.let { name ->
                            ClimateZone.entries.firstOrNull { it.name == name }
                        } ?: prev.climateZone
                    )
                    next.copy(result = computeResult(next))
                }
            }
        }
    }

    fun selectCrop(crop: CropProfile) = updateAndRecalc { it.copy(selectedCrop = crop) }
    fun updateAreaInput(text: String) = updateAndRecalc { it.copy(areaInput = text) }
    fun selectAreaUnit(unit: AreaUnit) = updateAndRecalc { it.copy(areaUnit = unit) }
    fun selectStage(stage: GrowthStage) = updateAndRecalc { it.copy(selectedStage = stage) }
    fun updateNitrogen(text: String) = updateAndRecalc { it.copy(nitrogenPpmText = text) }
    fun updatePhosphorus(text: String) = updateAndRecalc { it.copy(phosphorusPpmText = text) }
    fun updatePotassium(text: String) = updateAndRecalc { it.copy(potassiumPpmText = text) }
    fun updatePh(ph: Float) = updateAndRecalc { it.copy(soilPh = ph) }
    fun updateEc(text: String) = updateAndRecalc { it.copy(soilEcText = text) }
    fun selectIrrigation(method: IrrigationMethod) = updateAndRecalc { it.copy(irrigationMethod = method) }
    fun selectClimate(zone: ClimateZone) = updateAndRecalc { it.copy(climateZone = zone) }

    fun applySoilPreset(n: String, p: String, k: String, ph: Float, ec: String, zone: ClimateZone) {
        updateAndRecalc {
            it.copy(
                nitrogenPpmText = n,
                phosphorusPpmText = p,
                potassiumPpmText = k,
                soilPh = ph,
                soilEcText = ec,
                climateZone = zone
            )
        }
    }

    private fun updateAndRecalc(transform: (CalculatorUiState) -> CalculatorUiState) {
        var latestState: CalculatorUiState? = null
        _uiState.update { prev ->
            val next = transform(prev)
            val updated = next.copy(result = computeResult(next))
            latestState = updated
            updated
        }
        val snapshot = latestState
        if (prefsRepo != null && snapshot != null) {
            viewModelScope.launch {
                prefsRepo.saveCalculatorPrefs(
                    SavedCalculatorPrefs(
                        cropName = snapshot.selectedCrop.name,
                        areaInput = snapshot.areaInput,
                        areaUnitName = snapshot.areaUnit.name,
                        stageName = snapshot.selectedStage.name,
                        nitrogenPpm = snapshot.nitrogenPpmText,
                        phosphorusPpm = snapshot.phosphorusPpmText,
                        potassiumPpm = snapshot.potassiumPpmText,
                        soilPh = snapshot.soilPh,
                        soilEc = snapshot.soilEcText,
                        irrigationName = snapshot.irrigationMethod.name,
                        climateName = snapshot.climateZone.name
                    )
                )
            }
        }
    }

    private fun computeResult(state: CalculatorUiState): AgronomyCalculationResult {
        val area = state.areaInput.toDoubleOrNull()?.coerceAtLeast(0.01) ?: 1.0
        val n = state.nitrogenPpmText.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0
        val p = state.phosphorusPpmText.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0
        val k = state.potassiumPpmText.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0
        val ec = state.soilEcText.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.5

        return AgronomyCalculator.calculate(
            AgronomyCalculationInput(
                crop = state.selectedCrop,
                areaValue = area,
                areaUnit = state.areaUnit,
                stage = state.selectedStage,
                soilNitrogenPpm = n,
                soilPhosphorusPpm = p,
                soilPotassiumPpm = k,
                soilPh = state.soilPh.toDouble(),
                soilEcDsM = ec,
                irrigationMethod = state.irrigationMethod,
                climateZone = state.climateZone
            )
        )
    }

    class Factory(
        private val prefsRepo: ThemePreferencesRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CalculatorViewModel(prefsRepo) as T
        }
    }
}
