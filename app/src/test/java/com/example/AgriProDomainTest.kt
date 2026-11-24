package com.example

import com.example.data.catalog.AgrochemicalCatalog
import com.example.data.catalog.AgronomyCalculationInput
import com.example.data.catalog.AgronomyCalculator
import com.example.data.catalog.AreaUnit
import com.example.data.catalog.ClimateZone
import com.example.data.catalog.CompatibilityLevel
import com.example.data.catalog.CropProfile
import com.example.data.catalog.GrowthStage
import com.example.data.catalog.IrrigationMethod
import com.example.data.local.ExperimentEntity
import com.example.data.local.MeasurementEntity
import com.example.ui.fertilizer.FertilizerAreaUnit
import com.example.ui.fertilizer.FertilizerCrop
import com.example.ui.fertilizer.FertilizerNpkCalculator
import com.example.ui.fertilizer.FertilizerNpkInput
import com.example.ui.fertilizer.FertilizerStage
import com.example.util.CsvExporter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AgriProDomainTest {

    @Test
    fun agronomyCalculator_computesSalinityLeachingAndNpK() {
        val result = AgronomyCalculator.calculate(
            AgronomyCalculationInput(
                crop = CropProfile.TOMATO,
                areaValue = 2.0,
                areaUnit = AreaUnit.HECTARE,
                stage = GrowthStage.FRUIT_FILLING,
                soilNitrogenPpm = 20.0,
                soilPhosphorusPpm = 12.0,
                soilPotassiumPpm = 100.0,
                soilPh = 8.0,
                soilEcDsM = 4.0,
                irrigationMethod = IrrigationMethod.DRIP,
                climateZone = ClimateZone.SEMI_ARID
            )
        )
        assertTrue(result.urea46KgHa > 0.0)
        assertTrue(result.dapOrMapKgHa > 0.0)
        assertTrue(result.potassiumSulfate50KgHa > 0.0)
        assertTrue(result.dailyWaterM3 > 0.0)
        assertTrue(result.leachingFractionPercent > 0)
        assertTrue(result.advisories.any { it.contains("pH") })
    }

    @Test
    fun tankMixChecker_flagsDangerousPairsAndApprovesCompatiblePairs() {
        val sulfur = AgrochemicalCatalog.items.first { it.id == "thiovit_jet" }
        val oil = AgrochemicalCatalog.items.first { it.id == "summer_oil" }
        val calcium = AgrochemicalCatalog.items.first { it.id == "calcium_nitrate" }
        val map = AgrochemicalCatalog.items.first { it.id == "map_soluble" }
        val amistar = AgrochemicalCatalog.items.first { it.id == "amistar_top" }
        val vertimec = AgrochemicalCatalog.items.first { it.id == "vertimec" }
        val mustang = AgrochemicalCatalog.items.first { it.id == "mustang_306" }
        val topik = AgrochemicalCatalog.items.first { it.id == "topik_240" }
        val roundup = AgrochemicalCatalog.items.first { it.id == "roundup_360" }
        val kocide = AgrochemicalCatalog.items.first { it.id == "kocide_2000" }

        assertTrue(AgrochemicalCatalog.items.size >= 20)
        assertTrue(com.example.data.catalog.FlashcardCatalog.cards.size >= 20)

        assertEquals(
            CompatibilityLevel.INCOMPATIBLE,
            AgrochemicalCatalog.checkCompatibility(sulfur, oil).level
        )
        assertEquals(
            CompatibilityLevel.INCOMPATIBLE,
            AgrochemicalCatalog.checkCompatibility(calcium, map).level
        )
        assertEquals(
            CompatibilityLevel.INCOMPATIBLE,
            AgrochemicalCatalog.checkCompatibility(mustang, topik).level
        )
        assertEquals(
            CompatibilityLevel.INCOMPATIBLE,
            AgrochemicalCatalog.checkCompatibility(roundup, kocide).level
        )
        assertEquals(
            CompatibilityLevel.COMPATIBLE,
            AgrochemicalCatalog.checkCompatibility(amistar, vertimec).level
        )
    }

    @Test
    fun csvExporter_includesBomAndMeasurements() {
        val exp = ExperimentEntity(
            id = 1,
            title = "تجربة الزيتون",
            cropName = "الزيتون",
            experimentType = "تسميد",
            locationOrLab = "الحقل أ",
            hypothesis = "زيادة البوتاسيوم",
            startDateMillis = 1700000000000L
        )
        val measurements = listOf(
            MeasurementEntity(
                id = 1,
                experimentId = 1,
                recordedDateMillis = 1700000000000L,
                dayNumber = 7,
                plantHeightCm = 42.5,
                leafCount = 18,
                soilMoisturePercent = 58.0,
                notes = "نمو جيد, سليم"
            )
        )
        val csv = CsvExporter.buildExperimentCsv(exp, measurements)
        assertTrue(csv.startsWith("\uFEFF"))
        assertTrue(csv.contains("تجربة الزيتون"))
        assertTrue(csv.contains("\"نمو جيد, سليم\""))
    }

    @Test
    fun fertilizerNpkCalculator_computesRatiosAndAreaUnits() {
        val wheatFullSeason = FertilizerNpkCalculator.calculate(
            FertilizerNpkInput(
                crop = FertilizerCrop.WHEAT,
                stage = FertilizerStage.FULL_SEASON,
                areaValue = 2.0,
                areaUnit = FertilizerAreaUnit.HECTARE
            )
        )
        assertEquals("2 : 1 : 1", wheatFullSeason.formattedRatio)
        assertEquals(260.0, wheatFullSeason.totalNKg, 0.01)
        assertEquals(130.0, wheatFullSeason.totalPKg, 0.01)
        assertEquals(130.0, wheatFullSeason.totalKKg, 0.01)
        assertEquals(100, wheatFullSeason.nSharePercent + wheatFullSeason.pSharePercent + wheatFullSeason.kSharePercent)

        val wheatDunam = FertilizerNpkCalculator.calculate(
            FertilizerNpkInput(
                crop = FertilizerCrop.WHEAT,
                stage = FertilizerStage.FULL_SEASON,
                areaValue = 20.0,
                areaUnit = FertilizerAreaUnit.DUNAM
            )
        )
        assertEquals(wheatFullSeason.areaHectares, wheatDunam.areaHectares, 0.0001)
        assertEquals(wheatFullSeason.totalNKg, wheatDunam.totalNKg, 0.01)
    }
}
