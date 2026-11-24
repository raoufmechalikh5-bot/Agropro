package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        ExperimentEntity::class,
        MeasurementEntity::class,
        DiagnosisHistoryEntity::class,
        PesticideNoteEntity::class,
        CustomAgrochemicalEntity::class,
        FertilizerCalculationEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AgriProDatabase : RoomDatabase() {
    abstract fun experimentDao(): ExperimentDao
    abstract fun measurementDao(): MeasurementDao
    abstract fun diagnosisHistoryDao(): DiagnosisHistoryDao
    abstract fun pesticideNoteDao(): PesticideNoteDao
    abstract fun customAgrochemicalDao(): CustomAgrochemicalDao
    abstract fun fertilizerCalculationDao(): FertilizerCalculationDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `pesticide_notes` (
                        `agrochemicalId` TEXT NOT NULL,
                        `noteText` TEXT NOT NULL,
                        `customDosageNote` TEXT NOT NULL,
                        `photoPath` TEXT,
                        `updatedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`agrochemicalId`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `custom_agrochemicals` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `commercialName` TEXT NOT NULL,
                        `activeIngredient` TEXT NOT NULL,
                        `categoryName` TEXT NOT NULL,
                        `targetPests` TEXT NOT NULL,
                        `recommendedDosage` TEXT NOT NULL,
                        `phiDays` TEXT NOT NULL,
                        `notes` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE `diagnosis_history` ADD COLUMN `cropNameAr` TEXT NOT NULL DEFAULT ''"
                )
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `fertilizer_calculations` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `cropName` TEXT NOT NULL,
                        `cropTitleAr` TEXT NOT NULL,
                        `stageName` TEXT NOT NULL,
                        `stageTitleAr` TEXT NOT NULL,
                        `areaValue` REAL NOT NULL,
                        `areaUnitName` TEXT NOT NULL,
                        `areaUnitLabelAr` TEXT NOT NULL,
                        `areaHectares` REAL NOT NULL,
                        `npkRatio` TEXT NOT NULL,
                        `totalNKg` REAL NOT NULL,
                        `totalPKg` REAL NOT NULL,
                        `totalKKg` REAL NOT NULL,
                        `urea46Kg` REAL NOT NULL,
                        `map61Kg` REAL NOT NULL,
                        `potassiumSulfate50Kg` REAL NOT NULL,
                        `totalCommercialKg` REAL NOT NULL,
                        `plotLabel` TEXT NOT NULL,
                        `savedAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        @Volatile
        private var INSTANCE: AgriProDatabase? = null

        fun getInstance(context: Context): AgriProDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AgriProDatabase::class.java,
                    "agripro_field_db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
