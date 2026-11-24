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
        val safeHarvestDateStr = java.text.SimpleDateFormat("yyyy/MM/dd", java.util.Locale.US)
            .format(java.util.Date(now + phiDaysNumber * dayMs))

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
