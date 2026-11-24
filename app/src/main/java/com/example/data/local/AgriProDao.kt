package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ExperimentDao {
    @Query("SELECT * FROM experiments ORDER BY createdAt DESC")
    fun getAllExperiments(): Flow<List<ExperimentEntity>>

    @Query("SELECT * FROM experiments WHERE id = :id")
    fun getExperimentById(id: Long): Flow<ExperimentEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExperiment(experiment: ExperimentEntity): Long

    @Update
    suspend fun updateExperiment(experiment: ExperimentEntity)

    @Query("DELETE FROM experiments WHERE id = :id")
    suspend fun deleteExperimentById(id: Long)

    @Query("SELECT COUNT(*) FROM experiments")
    suspend fun countExperiments(): Int
}

@Dao
interface MeasurementDao {
    @Query("SELECT * FROM measurements WHERE experimentId = :experimentId ORDER BY dayNumber ASC, recordedDateMillis ASC")
    fun getMeasurementsForExperiment(experimentId: Long): Flow<List<MeasurementEntity>>

    @Query("SELECT * FROM measurements ORDER BY recordedDateMillis ASC")
    fun getAllMeasurements(): Flow<List<MeasurementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeasurement(measurement: MeasurementEntity): Long

    @Query("DELETE FROM measurements WHERE id = :id")
    suspend fun deleteMeasurementById(id: Long)
}

@Dao
interface DiagnosisHistoryDao {
    @Query("SELECT * FROM diagnosis_history ORDER BY diagnosedAt DESC LIMIT 20")
    fun getRecentDiagnoses(): Flow<List<DiagnosisHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiagnosis(record: DiagnosisHistoryEntity)

    @Query("DELETE FROM diagnosis_history WHERE id = :id")
    suspend fun deleteDiagnosisById(id: Long)
}

@Dao
interface PesticideNoteDao {
    @Query("SELECT * FROM pesticide_notes ORDER BY updatedAt DESC")
    fun getAllNotes(): Flow<List<PesticideNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNote(note: PesticideNoteEntity)

    @Query("DELETE FROM pesticide_notes WHERE agrochemicalId = :agrochemicalId")
    suspend fun deleteNote(agrochemicalId: String)
}

@Dao
interface CustomAgrochemicalDao {
    @Query("SELECT * FROM custom_agrochemicals ORDER BY createdAt DESC")
    fun getAllCustomAgrochemicals(): Flow<List<CustomAgrochemicalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomAgrochemical(item: CustomAgrochemicalEntity): Long

    @Query("DELETE FROM custom_agrochemicals WHERE id = :id")
    suspend fun deleteCustomAgrochemical(id: Long)
}

@Dao
interface FertilizerCalculationDao {
    @Query("SELECT * FROM fertilizer_calculations ORDER BY savedAt DESC")
    fun getAllCalculations(): Flow<List<FertilizerCalculationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalculation(calculation: FertilizerCalculationEntity): Long

    @Query("DELETE FROM fertilizer_calculations WHERE id = :id")
    suspend fun deleteCalculationById(id: Long)

    @Query("DELETE FROM fertilizer_calculations")
    suspend fun clearAllCalculations()
}
