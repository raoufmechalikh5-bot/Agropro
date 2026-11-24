package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "experiments")
data class ExperimentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val cropName: String,
    val experimentType: String,
    val locationOrLab: String,
    val hypothesis: String,
    val startDateMillis: Long,
    val status: String = "جارية",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "measurements",
    foreignKeys = [
        ForeignKey(
            entity = ExperimentEntity::class,
            parentColumns = ["id"],
            childColumns = ["experimentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["experimentId"])]
)
data class MeasurementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val experimentId: Long,
    val recordedDateMillis: Long,
    val dayNumber: Int,
    val plantHeightCm: Double,
    val leafCount: Int,
    val soilMoisturePercent: Double,
    val notes: String,
    val photoPath: String? = null
)

@Entity(tableName = "diagnosis_history")
data class DiagnosisHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cropNameAr: String = "",
    val diseaseNameAr: String,
    val scientificName: String,
    val cause: String,
    val severity: String,
    val activeIngredient: String,
    val phiDays: String,
    val summary: String,
    val photoPath: String? = null,
    val diagnosedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "pesticide_notes")
data class PesticideNoteEntity(
    @PrimaryKey val agrochemicalId: String,
    val noteText: String,
    val customDosageNote: String = "",
    val photoPath: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "custom_agrochemicals")
data class CustomAgrochemicalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val commercialName: String,
    val activeIngredient: String,
    val categoryName: String,
    val targetPests: String,
    val recommendedDosage: String,
    val phiDays: String,
    val notes: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "fertilizer_calculations")
data class FertilizerCalculationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cropName: String,
    val cropTitleAr: String,
    val stageName: String,
    val stageTitleAr: String,
    val areaValue: Double,
    val areaUnitName: String,
    val areaUnitLabelAr: String,
    val areaHectares: Double,
    val npkRatio: String,
    val totalNKg: Double,
    val totalPKg: Double,
    val totalKKg: Double,
    val urea46Kg: Double,
    val map61Kg: Double,
    val potassiumSulfate50Kg: Double,
    val totalCommercialKg: Double,
    val plotLabel: String = "",
    val savedAt: Long = System.currentTimeMillis()
)
