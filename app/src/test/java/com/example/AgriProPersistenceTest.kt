package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AgriProDatabase
import com.example.data.local.CustomAgrochemicalEntity
import com.example.data.local.ExperimentEntity
import com.example.data.local.FertilizerCalculationEntity
import com.example.data.local.MeasurementEntity
import com.example.data.local.PesticideNoteEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AgriProPersistenceTest {

    private lateinit var db: AgriProDatabase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AgriProDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun experimentMeasurementsAndPhotosPersistAcrossQueries() = runTest {
        val expId = db.experimentDao().insertExperiment(
            ExperimentEntity(
                title = "تجربة الزيتون المروي",
                cropName = "الزيتون",
                experimentType = "تجربة تسميد ميدانية",
                locationOrLab = "الحقل الشمالي",
                hypothesis = "التسميد البوتاسي يحسن النمو",
                startDateMillis = 1700000000000L
            )
        )

        db.measurementDao().insertMeasurement(
            MeasurementEntity(
                experimentId = expId,
                recordedDateMillis = 1700086400000L,
                dayNumber = 7,
                plantHeightCm = 55.4,
                leafCount = 24,
                soilMoisturePercent = 63.0,
                notes = "ورقة سليمة",
                photoPath = "/data/user/0/com.aistudio.agripro.kxfmqt/files/experiment_photos/photo_1.jpg"
            )
        )

        val experiments = db.experimentDao().getAllExperiments().first()
        val measurements = db.measurementDao().getMeasurementsForExperiment(expId).first()

        assertEquals(1, experiments.size)
        assertEquals("تجربة الزيتون المروي", experiments.first().title)
        assertEquals(1, measurements.size)
        assertEquals(55.4, measurements.first().plantHeightCm, 0.01)
        assertNotNull(measurements.first().photoPath)
    }

    @Test
    fun customPesticideNotesAndCustomProductsPersist() = runTest {
        db.pesticideNoteDao().upsertNote(
            PesticideNoteEntity(
                agrochemicalId = "amistar_top",
                noteText = "فعالية ممتازة ضد عين الطاووس بعد أمطار الخريف",
                customDosageNote = "85 مل / 100 لتر",
                photoPath = "/data/user/0/com.aistudio.agripro.kxfmqt/files/pesticide_photos/photo_2.jpg"
            )
        )

        db.customAgrochemicalDao().insertCustomAgrochemical(
            CustomAgrochemicalEntity(
                commercialName = "نيترو ماغ محلي",
                activeIngredient = "نترات المغنيسيوم 11-0-0 + 15% MgO",
                categoryName = "FERTILIZER",
                targetPests = "اصفرار ما بين العروق في الحمضيات",
                recommendedDosage = "200 غ / 100 لتر",
                phiDays = "0 يوم",
                notes = "يرش صباحاً"
            )
        )

        val notes = db.pesticideNoteDao().getAllNotes().first()
        val customItems = db.customAgrochemicalDao().getAllCustomAgrochemicals().first()

        assertEquals(1, notes.size)
        assertEquals("85 مل / 100 لتر", notes.first().customDosageNote)
        assertEquals(1, customItems.size)
        assertEquals("نيترو ماغ محلي", customItems.first().commercialName)
    }

    @Test
    fun fertilizerCalculationsPersistAndDeleteOffline() = runTest {
        val dao = db.fertilizerCalculationDao()
        val id1 = dao.insertCalculation(
            FertilizerCalculationEntity(
                cropName = "TOMATO",
                cropTitleAr = "الطماطم (صناعية / محمية)",
                stageName = "FULL_SEASON",
                stageTitleAr = "البرنامج الموسمي الكامل",
                areaValue = 2.0,
                areaUnitName = "HECTARE",
                areaUnitLabelAr = "هكتار",
                areaHectares = 2.0,
                npkRatio = "2 : 1 : 2.7",
                totalNKg = 360.0,
                totalPKg = 180.0,
                totalKKg = 480.0,
                urea46Kg = 782.6,
                map61Kg = 295.1,
                potassiumSulfate50Kg = 960.0,
                totalCommercialKg = 2037.7,
                plotLabel = "بيت محمي 1",
                savedAt = 1700000000000L
            )
        )
        val id2 = dao.insertCalculation(
            FertilizerCalculationEntity(
                cropName = "WHEAT",
                cropTitleAr = "القمح الصلب والشعير",
                stageName = "FULL_SEASON",
                stageTitleAr = "البرنامج الموسمي الكامل",
                areaValue = 10.0,
                areaUnitName = "DUNAM",
                areaUnitLabelAr = "دونم",
                areaHectares = 1.0,
                npkRatio = "2 : 1 : 1",
                totalNKg = 130.0,
                totalPKg = 65.0,
                totalKKg = 65.0,
                urea46Kg = 282.6,
                map61Kg = 106.6,
                potassiumSulfate50Kg = 130.0,
                totalCommercialKg = 519.2,
                plotLabel = "الحقل الشرقي",
                savedAt = 1700000050000L
            )
        )

        val saved = dao.getAllCalculations().first()
        assertEquals(2, saved.size)
        // Ordered by savedAt DESC
        assertEquals(id2, saved.first().id)
        assertEquals("الحقل الشرقي", saved.first().plotLabel)
        assertEquals("2 : 1 : 1", saved.first().npkRatio)

        dao.deleteCalculationById(id1)
        val afterDelete = dao.getAllCalculations().first()
        assertEquals(1, afterDelete.size)
        assertEquals(id2, afterDelete.single().id)
    }
}
