package com.example.data.local

import kotlinx.coroutines.flow.Flow

class FertilizerCalculationRepository(
    private val fertilizerCalculationDao: FertilizerCalculationDao
) {
    val savedCalculations: Flow<List<FertilizerCalculationEntity>> =
        fertilizerCalculationDao.getAllCalculations()

    suspend fun saveCalculation(calculation: FertilizerCalculationEntity): Long =
        fertilizerCalculationDao.insertCalculation(calculation)

    suspend fun deleteCalculation(id: Long) =
        fertilizerCalculationDao.deleteCalculationById(id)

    suspend fun clearAll() =
        fertilizerCalculationDao.clearAllCalculations()
}
