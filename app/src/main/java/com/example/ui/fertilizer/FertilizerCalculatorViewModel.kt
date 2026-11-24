package com.example.ui.fertilizer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.FertilizerCalculationEntity
import com.example.data.local.FertilizerCalculationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToInt

enum class FertilizerCrop(
    val titleAr: String,
    val baseNKgHa: Double,
    val basePKgHa: Double,
    val baseKKgHa: Double,
    val idealPhRange: String,
    val commercialBlendHint: String,
    val agronomyTipAr: String
) {
    TOMATO(
        titleAr = "الطماطم (صناعية / محمية)",
        baseNKgHa = 180.0,
        basePKgHa = 90.0,
        baseKKgHa = 240.0,
        idealPhRange = "6.0 - 6.8",
        commercialBlendHint = "15-8-20 أو 12-12-36 (عند الإثمار)",
        agronomyTipAr = "تتطلب الطماطم بوتاسيوم مرتفعاً لتلوين الثمار وزيادة المواد الصلبة الذائبة وتقليل عفن الطرف الزهري."
    ),
    OLIVE(
        titleAr = "الزيتون (بعل / مروي)",
        baseNKgHa = 110.0,
        basePKgHa = 55.0,
        baseKKgHa = 140.0,
        idealPhRange = "6.5 - 7.8",
        commercialBlendHint = "16-8-20 أو 20-10-25",
        agronomyTipAr = "البوتاسيوم عنصر حاسم في بساتين الزيتون لرفع نسبة الزيت وتحمل الإجهاد المائي الصيفي."
    ),
    POTATO(
        titleAr = "البطاطا الموسمية",
        baseNKgHa = 165.0,
        basePKgHa = 110.0,
        baseKKgHa = 220.0,
        idealPhRange = "5.5 - 6.5",
        commercialBlendHint = "15-10-20 أو سلفات البوتاسيوم + DAP",
        agronomyTipAr = "تحتاج البطاطا فوسفوراً تأسيسياً لتكوين الدرنات وبوتاسيوم وافراً لتخزين النشا وتجنب التشقق."
    ),
    CITRUS(
        titleAr = "الحمضيات (برتقال / ليمون)",
        baseNKgHa = 150.0,
        basePKgHa = 65.0,
        baseKKgHa = 165.0,
        idealPhRange = "6.0 - 7.5",
        commercialBlendHint = "18-8-20 مع عناصر صغرى مخلبية",
        agronomyTipAr = "التوازن بين الآزوت والبوتاسيوم يمنع تساقط العقد الحديث ويحسن سماكة القشرة وجودة العصير."
    ),
    WHEAT(
        titleAr = "القمح الصلب والشعير",
        baseNKgHa = 130.0,
        basePKgHa = 65.0,
        baseKKgHa = 65.0,
        idealPhRange = "6.2 - 7.6",
        commercialBlendHint = "20-10-10 أو DAP تأسيسي + يوريا عند التفريع",
        agronomyTipAr = "يُضاف الفوسفور والبوتاسيوم عند البذر، ويُقسّم الآزوت بين مرحلة التفريع والاستطالة لرفع وزن الحبوب."
    ),
    GRAPEVINE(
        titleAr = "العنب والكروم",
        baseNKgHa = 95.0,
        basePKgHa = 50.0,
        baseKKgHa = 145.0,
        idealPhRange = "6.0 - 7.5",
        commercialBlendHint = "12-6-18 أو 13-40-13 عند التأسيس",
        agronomyTipAr = "الإفراط في الآزوت يسبب نمواً خضرياً كثيفاً على حساب العناقيد؛ الأولوية للبوتاسيوم لرفع نسبة السكر (Brix)."
    ),
    STRAWBERRY(
        titleAr = "الفراولة (التوت الأرضي)",
        baseNKgHa = 140.0,
        basePKgHa = 95.0,
        baseKKgHa = 200.0,
        idealPhRange = "5.5 - 6.5",
        commercialBlendHint = "14-10-20 ذواب عبر التسميد بالري",
        agronomyTipAr = "الفراولة حساسة للملوحة؛ يُفضل استخدام سلفات البوتاسيوم وتقسيم الجرعات في مياه الري."
    ),
    DATE_PALM(
        titleAr = "نخيل التمر",
        baseNKgHa = 140.0,
        basePKgHa = 70.0,
        baseKKgHa = 180.0,
        idealPhRange = "6.5 - 8.0",
        commercialBlendHint = "16-8-20 + سماد عضوي متحلل شتاءً",
        agronomyTipAr = "يدعم البوتاسيوم والفوسفور حجم الثمار وجودة التلقيح ومقاومة حرارة الواحات."
    )
}

enum class FertilizerStage(
    val titleAr: String,
    val nFactor: Double,
    val pFactor: Double,
    val kFactor: Double
) {
    FULL_SEASON("البرنامج الموسمي الكامل", 1.0, 1.0, 1.0),
    ESTABLISHMENT("التأسيس وتقوية الجذور", 0.25, 0.55, 0.20),
    VEGETATIVE("النمو الخضري والتفريع", 0.50, 0.20, 0.25),
    FLOWERING_FRUIT("الإزهار وعقد ونضج الثمار", 0.25, 0.25, 0.55)
}

enum class FertilizerAreaUnit(
    val labelAr: String,
    val shortLabelAr: String,
    val hectaresPerUnit: Double
) {
    HECTARE("هكتار (ha)", "هكتار", 1.0),
    DUNAM("دونم (1000 م²)", "دونم", 0.1),
    SQUARE_METER("متر مربع (m²)", "م²", 0.0001)
}

data class FertilizerNpkInput(
    val crop: FertilizerCrop,
    val stage: FertilizerStage,
    val areaValue: Double,
    val areaUnit: FertilizerAreaUnit
)

data class FertilizerNpkResult(
    val areaHectares: Double,
    val nPerHaKg: Double,
    val pPerHaKg: Double,
    val kPerHaKg: Double,
    val totalNKg: Double,
    val totalPKg: Double,
    val totalKKg: Double,
    val ratioN: Double,
    val ratioP: Double,
    val ratioK: Double,
    val nSharePercent: Int,
    val pSharePercent: Int,
    val kSharePercent: Int,
    val urea46Kg: Double,
    val map61Kg: Double,
    val potassiumSulfate50Kg: Double,
    val totalCommercialKg: Double
) {
    val formattedRatio: String
        get() = "${formatRatioPart(ratioN)} : ${formatRatioPart(ratioP)} : ${formatRatioPart(ratioK)}"

    private fun formatRatioPart(value: Double): String {
        val rounded = (value * 10.0).roundToInt() / 10.0
        return if (rounded % 1.0 == 0.0) {
            String.format(Locale.US, "%.0f", rounded)
        } else {
            String.format(Locale.US, "%.1f", rounded)
        }
    }
}

object FertilizerNpkCalculator {
    fun calculate(input: FertilizerNpkInput): FertilizerNpkResult {
        val safeArea = max(0.0, input.areaValue)
        val areaHa = safeArea * input.areaUnit.hectaresPerUnit

        val nHa = input.crop.baseNKgHa * input.stage.nFactor
        val pHa = input.crop.basePKgHa * input.stage.pFactor
        val kHa = input.crop.baseKKgHa * input.stage.kFactor

        val totalN = nHa * areaHa
        val totalP = pHa * areaHa
        val totalK = kHa * areaHa

        val minPositive = listOf(nHa, pHa, kHa).filter { it > 0.0 }.minOrNull() ?: 1.0
        val ratioN = nHa / minPositive
        val ratioP = pHa / minPositive
        val ratioK = kHa / minPositive

        val sumNpk = max(1e-6, nHa + pHa + kHa)
        val nPct = ((nHa / sumNpk) * 100.0).roundToInt()
        val pPct = ((pHa / sumNpk) * 100.0).roundToInt()
        val kPct = (100 - nPct - pPct).coerceAtLeast(0)

        // Commercial fertilizer equivalents:
        // Urea (46% N), MAP (12-61-0 -> 61% P2O5), Potassium Sulfate (0-0-50 -> 50% K2O)
        val ureaKg = totalN / 0.46
        val mapKg = totalP / 0.61
        val sopKg = totalK / 0.50

        return FertilizerNpkResult(
            areaHectares = areaHa,
            nPerHaKg = nHa,
            pPerHaKg = pHa,
            kPerHaKg = kHa,
            totalNKg = totalN,
            totalPKg = totalP,
            totalKKg = totalK,
            ratioN = ratioN,
            ratioP = ratioP,
            ratioK = ratioK,
            nSharePercent = nPct,
            pSharePercent = pPct,
            kSharePercent = kPct,
            urea46Kg = ureaKg,
            map61Kg = mapKg,
            potassiumSulfate50Kg = sopKg,
            totalCommercialKg = ureaKg + mapKg + sopKg
        )
    }
}

data class FertilizerCalculatorUiState(
    val selectedCrop: FertilizerCrop = FertilizerCrop.TOMATO,
    val selectedStage: FertilizerStage = FertilizerStage.FULL_SEASON,
    val areaInput: String = "1.0",
    val areaUnit: FertilizerAreaUnit = FertilizerAreaUnit.HECTARE,
    val plotLabelInput: String = "",
    val isAreaValid: Boolean = true,
    val lastSavedAtMillis: Long? = null,
    val result: FertilizerNpkResult = FertilizerNpkCalculator.calculate(
        FertilizerNpkInput(
            crop = FertilizerCrop.TOMATO,
            stage = FertilizerStage.FULL_SEASON,
            areaValue = 1.0,
            areaUnit = FertilizerAreaUnit.HECTARE
        )
    )
)

class FertilizerCalculatorViewModel(
    private val repository: FertilizerCalculationRepository? = null
) : ViewModel() {
    private val _uiState = MutableStateFlow(FertilizerCalculatorUiState())
    val uiState: StateFlow<FertilizerCalculatorUiState> = _uiState.asStateFlow()

    val savedCalculations: StateFlow<List<FertilizerCalculationEntity>> =
        (repository?.savedCalculations ?: emptyFlow())
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    fun selectCrop(crop: FertilizerCrop) {
        updateState { it.copy(selectedCrop = crop, lastSavedAtMillis = null) }
    }

    fun selectStage(stage: FertilizerStage) {
        updateState { it.copy(selectedStage = stage, lastSavedAtMillis = null) }
    }

    fun updateAreaInput(text: String) {
        val sanitized = text.replace(',', '.')
        updateState { it.copy(areaInput = sanitized, lastSavedAtMillis = null) }
    }

    fun selectAreaUnit(unit: FertilizerAreaUnit) {
        updateState { it.copy(areaUnit = unit, lastSavedAtMillis = null) }
    }

    fun applyQuickArea(value: String) {
        updateState { it.copy(areaInput = value, lastSavedAtMillis = null) }
    }

    fun updatePlotLabel(text: String) {
        _uiState.update { it.copy(plotLabelInput = text, lastSavedAtMillis = null) }
    }

    fun saveCurrentCalculation() {
        val current = _uiState.value
        val parsedArea = current.areaInput.toDoubleOrNull()
        if (!current.isAreaValid || parsedArea == null || parsedArea <= 0.0) return

        val repo = repository ?: return
        val now = System.currentTimeMillis()
        val calc = current.result

        viewModelScope.launch {
            repo.saveCalculation(
                FertilizerCalculationEntity(
                    cropName = current.selectedCrop.name,
                    cropTitleAr = current.selectedCrop.titleAr,
                    stageName = current.selectedStage.name,
                    stageTitleAr = current.selectedStage.titleAr,
                    areaValue = parsedArea,
                    areaUnitName = current.areaUnit.name,
                    areaUnitLabelAr = current.areaUnit.shortLabelAr,
                    areaHectares = calc.areaHectares,
                    npkRatio = calc.formattedRatio,
                    totalNKg = calc.totalNKg,
                    totalPKg = calc.totalPKg,
                    totalKKg = calc.totalKKg,
                    urea46Kg = calc.urea46Kg,
                    map61Kg = calc.map61Kg,
                    potassiumSulfate50Kg = calc.potassiumSulfate50Kg,
                    totalCommercialKg = calc.totalCommercialKg,
                    plotLabel = current.plotLabelInput.trim(),
                    savedAt = now
                )
            )
            _uiState.update { it.copy(lastSavedAtMillis = now) }
        }
    }

    fun loadSavedCalculation(record: FertilizerCalculationEntity) {
        val crop = FertilizerCrop.entries.firstOrNull { it.name == record.cropName }
            ?: FertilizerCrop.TOMATO
        val stage = FertilizerStage.entries.firstOrNull { it.name == record.stageName }
            ?: FertilizerStage.FULL_SEASON
        val unit = FertilizerAreaUnit.entries.firstOrNull { it.name == record.areaUnitName }
            ?: FertilizerAreaUnit.HECTARE
        val formattedArea = if (record.areaValue % 1.0 == 0.0) {
            record.areaValue.toInt().toString()
        } else {
            record.areaValue.toString()
        }

        updateState {
            it.copy(
                selectedCrop = crop,
                selectedStage = stage,
                areaInput = formattedArea,
                areaUnit = unit,
                plotLabelInput = record.plotLabel,
                lastSavedAtMillis = null
            )
        }
    }

    fun deleteSavedCalculation(id: Long) {
        val repo = repository ?: return
        viewModelScope.launch {
            repo.deleteCalculation(id)
        }
    }

    private fun updateState(transform: (FertilizerCalculatorUiState) -> FertilizerCalculatorUiState) {
        _uiState.update { prev ->
            val next = transform(prev)
            val parsedArea = next.areaInput.toDoubleOrNull()
            val isValid = parsedArea != null && parsedArea > 0.0
            val effectiveArea = if (isValid) parsedArea!! else 0.0
            val calculated = FertilizerNpkCalculator.calculate(
                FertilizerNpkInput(
                    crop = next.selectedCrop,
                    stage = next.selectedStage,
                    areaValue = effectiveArea,
                    areaUnit = next.areaUnit
                )
            )
            next.copy(isAreaValid = isValid, result = calculated)
        }
    }

    class Factory(
        private val repository: FertilizerCalculationRepository? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FertilizerCalculatorViewModel(repository) as T
        }
    }
}
