package com.example.data.local

import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExperimentRepository(
    private val experimentDao: ExperimentDao,
    private val measurementDao: MeasurementDao,
    private val diagnosisHistoryDao: DiagnosisHistoryDao,
    private val pesticideNoteDao: PesticideNoteDao,
    private val customAgrochemicalDao: CustomAgrochemicalDao,
    private val prefsRepo: ThemePreferencesRepository? = null
) {
    val allExperiments: Flow<List<ExperimentEntity>> = experimentDao.getAllExperiments()
    val allMeasurements: Flow<List<MeasurementEntity>> = measurementDao.getAllMeasurements()
    val recentDiagnoses: Flow<List<DiagnosisHistoryEntity>> = diagnosisHistoryDao.getRecentDiagnoses()
    val allPesticideNotes: Flow<List<PesticideNoteEntity>> = pesticideNoteDao.getAllNotes()
    val customAgrochemicals: Flow<List<CustomAgrochemicalEntity>> = customAgrochemicalDao.getAllCustomAgrochemicals()

    fun getMeasurementsForExperiment(experimentId: Long): Flow<List<MeasurementEntity>> =
        measurementDao.getMeasurementsForExperiment(experimentId)

    suspend fun insertExperiment(experiment: ExperimentEntity): Long =
        experimentDao.insertExperiment(experiment)

    suspend fun updateExperiment(experiment: ExperimentEntity) =
        experimentDao.updateExperiment(experiment)

    suspend fun deleteExperiment(id: Long) =
        experimentDao.deleteExperimentById(id)

    suspend fun insertMeasurement(measurement: MeasurementEntity): Long =
        measurementDao.insertMeasurement(measurement)

    suspend fun deleteMeasurement(id: Long) =
        measurementDao.deleteMeasurementById(id)

    suspend fun saveDiagnosis(record: DiagnosisHistoryEntity) =
        diagnosisHistoryDao.insertDiagnosis(record)

    suspend fun deleteDiagnosis(id: Long) =
        diagnosisHistoryDao.deleteDiagnosisById(id)

    suspend fun upsertPesticideNote(note: PesticideNoteEntity) =
        pesticideNoteDao.upsertNote(note)

    suspend fun deletePesticideNote(agrochemicalId: String) =
        pesticideNoteDao.deleteNote(agrochemicalId)

    suspend fun insertCustomAgrochemical(item: CustomAgrochemicalEntity): Long =
        customAgrochemicalDao.insertCustomAgrochemical(item)

    suspend fun deleteCustomAgrochemical(id: Long) =
        customAgrochemicalDao.deleteCustomAgrochemical(id)

    suspend fun createTreatmentPlanFromDiagnosis(
        cropNameAr: String,
        diseaseNameAr: String,
        scientificName: String,
        activeIngredient: String,
        phiDaysText: String,
        recommendations: List<String>
    ): Long {
        val now = System.currentTimeMillis()
        val dayMs = 86_400_000L
        val phiDaysNumber = Regex("\\d+").find(phiDaysText)?.value?.toIntOrNull() ?: 7
        val safeHarvestDateStr = SimpleDateFormat("yyyy/MM/dd", Locale.US)
            .format(Date(now + phiDaysNumber * dayMs))

        val expId = experimentDao.insertExperiment(
            ExperimentEntity(
                title = "خطة علاج: $diseaseNameAr",
                cropName = "$cropNameAr ($scientificName)",
                experimentType = "بروتوكول وقاية وعلاج",
                locationOrLab = "الحقل المصاب - متابعة ميدانية",
                hypothesis = "مكافحة $diseaseNameAr باستخدام المادة الفعالة ($activeIngredient) والالتزام بفترة تحريم $phiDaysText.",
                startDateMillis = now,
                status = "جارية",
                createdAt = now
            )
        )

        measurementDao.insertMeasurement(
            MeasurementEntity(
                experimentId = expId,
                recordedDateMillis = now,
                dayNumber = 1,
                plantHeightCm = 0.0,
                leafCount = 0,
                soilMoisturePercent = 0.0,
                notes = "الرش العلاجي الأول بالمادة الفعالة: $activeIngredient. ${recommendations.firstOrNull().orEmpty()}"
            )
        )

        measurementDao.insertMeasurement(
            MeasurementEntity(
                experimentId = expId,
                recordedDateMillis = now + phiDaysNumber * dayMs,
                dayNumber = phiDaysNumber,
                plantHeightCm = 0.0,
                leafCount = 0,
                soilMoisturePercent = 0.0,
                notes = "انتهاء فترة الأمان (PHI: $phiDaysText) — التاريخ الآمن للجني والحصاد: $safeHarvestDateStr"
            )
        )

        return expId
    }

    suspend fun ensureSampleExperimentsSeeded() {
        if (prefsRepo?.hasSeededSamples() == true) return
        if (experimentDao.countExperiments() > 0) {
            prefsRepo?.markSamplesSeeded()
            return
        }

        val now = System.currentTimeMillis()
        val dayMs = 86_400_000L

        val exp1Id = experimentDao.insertExperiment(
            ExperimentEntity(
                title = "تأثير التسميد البوتاسي على نمو الطماطم الصناعية",
                cropName = "الطماطم (Solanum lycopersicum)",
                experimentType = "تجربة تسميد ميدانية",
                locationOrLab = "محطة التجارب الزراعية - قطعة ب3",
                hypothesis = "رفع جرعة سلفات البوتاسيوم أثناء مرحلة الإثمار يحسن ارتفاع الساق وكفاءة امتصاص الرطوبة.",
                startDateMillis = now - 35L * dayMs,
                status = "جارية",
                createdAt = now - 35L * dayMs
            )
        )

        val exp1Measurements = listOf(
            MeasurementEntity(
                experimentId = exp1Id,
                recordedDateMillis = now - 35L * dayMs,
                dayNumber = 1,
                plantHeightCm = 14.5,
                leafCount = 6,
                soilMoisturePercent = 64.0,
                notes = "شتل سليم ومتجانس بعد عملية الزرع والتنقيط التأسيسي."
            ),
            MeasurementEntity(
                experimentId = exp1Id,
                recordedDateMillis = now - 28L * dayMs,
                dayNumber = 7,
                plantHeightCm = 22.0,
                leafCount = 10,
                soilMoisturePercent = 61.5,
                notes = "بداية التفرع الجانبي وإضافة الدفعة الأولى من الفوسفور MAP."
            ),
            MeasurementEntity(
                experimentId = exp1Id,
                recordedDateMillis = now - 21L * dayMs,
                dayNumber = 14,
                plantHeightCm = 34.2,
                leafCount = 16,
                soilMoisturePercent = 58.0,
                notes = "ظهور أولى النورات الزهرية مع استقرار رطوبة التربة."
            ),
            MeasurementEntity(
                experimentId = exp1Id,
                recordedDateMillis = now - 14L * dayMs,
                dayNumber = 21,
                plantHeightCm = 48.6,
                leafCount = 24,
                soilMoisturePercent = 62.0,
                notes = "بداية عقد الثمار؛ تطبيق سلفات البوتاسيوم 50% عبر شبكة الري."
            ),
            MeasurementEntity(
                experimentId = exp1Id,
                recordedDateMillis = now - 7L * dayMs,
                dayNumber = 28,
                plantHeightCm = 63.4,
                leafCount = 31,
                soilMoisturePercent = 59.5,
                notes = "نمو خضري قوي وخلو الأوراق السفلية من الندوة المبكرة."
            )
        )
        exp1Measurements.forEach { measurementDao.insertMeasurement(it) }

        val exp2Id = experimentDao.insertExperiment(
            ExperimentEntity(
                title = "اختبار إنبات القمح الصلب تحت الإجهاد الملحي",
                cropName = "القمح الصلب (Triticum durum)",
                experimentType = "تجربة مخبرية (إنبات)",
                locationOrLab = "مخبر الفيزيولوجيا النباتية - طاولة 2",
                hypothesis = "ارتفاع الملوحة فوق 6 dS/m يبطئ استطالة البادرة ويقلص نسبة الإنبات التراكمية.",
                startDateMillis = now - 20L * dayMs,
                status = "مكتملة",
                createdAt = now - 20L * dayMs
            )
        )

        val exp2Measurements = listOf(
            MeasurementEntity(
                experimentId = exp2Id,
                recordedDateMillis = now - 20L * dayMs,
                dayNumber = 1,
                plantHeightCm = 0.0,
                leafCount = 0,
                soilMoisturePercent = 78.0,
                notes = "تعقيم البذور ووضعها في أطباق بتري مع محلول NaCl بتركيز 4 dS/m."
            ),
            MeasurementEntity(
                experimentId = exp2Id,
                recordedDateMillis = now - 16L * dayMs,
                dayNumber = 4,
                plantHeightCm = 2.8,
                leafCount = 1,
                soilMoisturePercent = 74.0,
                notes = "بزوغ الجذير والرويشة في 82% من البذور المعاملة."
            ),
            MeasurementEntity(
                experimentId = exp2Id,
                recordedDateMillis = now - 12L * dayMs,
                dayNumber = 8,
                plantHeightCm = 7.4,
                leafCount = 2,
                soilMoisturePercent = 71.0,
                notes = "انفراد الورقة الأولى بلون أخضر طبيعي."
            ),
            MeasurementEntity(
                experimentId = exp2Id,
                recordedDateMillis = now - 8L * dayMs,
                dayNumber = 12,
                plantHeightCm = 13.1,
                leafCount = 3,
                soilMoisturePercent = 69.0,
                notes = "استقرار طول البادرة وجمع القياسات النهائية للتحليل الإحصائي."
            )
        )
        exp2Measurements.forEach { measurementDao.insertMeasurement(it) }

        prefsRepo?.markSamplesSeeded()
    }
}
