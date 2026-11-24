package com.example.data.ai

import android.graphics.Bitmap

data class SelectedPlantImage(
    val id: Long = System.nanoTime(),
    val bitmap: Bitmap,
    val label: String
)

data class PlantDiagnosisResult(
    val cropNameAr: String,
    val diseaseNameAr: String,
    val scientificName: String,
    val cause: String,
    val severity: String,
    val confidencePercent: Int = 85,
    val clarificationNeeded: String = "",
    val activeIngredient: String,
    val phiDays: String,
    val summary: String,
    val recommendations: List<String>
)

sealed interface PlantDoctorUiState {
    data object Idle : PlantDoctorUiState
    data object Loading : PlantDoctorUiState
    data class Success(val result: PlantDiagnosisResult) : PlantDoctorUiState
    data class Error(val message: String) : PlantDoctorUiState
}
