package com.example.data.catalog

import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToInt

enum class CropProfile(
    val titleAr: String,
    val targetN: Double,
    val targetP: Double,
    val targetK: Double,
    val baseEtMmDay: Double,
    val optimalPhRange: ClosedFloatingPointRange<Double>,
    val salinityThresholdDsM: Double
) {
    OLIVE(
        titleAr = "الزيتون (بعل / مروي)",
        targetN = 110.0,
        targetP = 55.0,
        targetK = 140.0,
        baseEtMmDay = 3.8,
        optimalPhRange = 6.5..7.8,
        salinityThresholdDsM = 4.0
    ),
    TOMATO(
        titleAr = "الطماطم (صناعية / محمية)",
        targetN = 180.0,
        targetP = 95.0,
        targetK = 240.0,
        baseEtMmDay = 5.4,
        optimalPhRange = 6.0..6.8,
        salinityThresholdDsM = 2.5
    ),
    POTATO(
        titleAr = "البطاطا الموسمية",
        targetN = 165.0,
        targetP = 110.0,
        targetK = 220.0,
        baseEtMmDay = 4.8,
        optimalPhRange = 5.5..6.5,
        salinityThresholdDsM = 1.8
    ),
    CITRUS(
        titleAr = "الحمضيات (برتقال / ليمون)",
        targetN = 150.0,
        targetP = 65.0,
        targetK = 160.0,
        baseEtMmDay = 4.5,
        optimalPhRange = 6.0..7.5,
        salinityThresholdDsM = 1.7
    ),
    WHEAT(
        titleAr = "القمح الصلب والشعير",
        targetN = 130.0,
        targetP = 65.0,
        targetK = 70.0,
        baseEtMmDay = 3.5,
        optimalPhRange = 6.2..7.6,
        salinityThresholdDsM = 5.5
    )
}

enum class GrowthStage(
    val titleAr: String,
    val nWeight: Double,
    val pWeight: Double,
    val kWeight: Double,
    val kcFactor: Double
) {
    ESTABLISHMENT("التأسيس والإنبات", 0.25, 0.55, 0.20, 0.55),
    VEGETATIVE("النمو الخضري والتفريع", 0.50, 0.25, 0.25, 0.85),
    FLOWERING("الإزهار وعقد الثمار", 0.20, 0.30, 0.40, 1.12),
    FRUIT_FILLING("التسمين والنضج", 0.15, 0.15, 0.55, 1.05),
    DORMANCY("السكون والخدمة الشتوية", 0.10, 0.35, 0.25, 0.45)
}

enum class AreaUnit(val labelAr: String) {
    HECTARE("هكتار (ha)"),
    SQUARE_METER("متر مربع (m²)")
}

enum class IrrigationMethod(val labelAr: String, val efficiency: Double) {
    DRIP("الري بالتنقيط (كفاءة 90%)", 0.90),
    SPRINKLER("الرش المحوري (كفاءة 75%)", 0.75),
    SURFACE("الغمر التقليدي (كفاءة 55%)", 0.55)
}

enum class ClimateZone(val labelAr: String, val multiplier: Double) {
    COASTAL("ساحلي معتدل", 0.90),
    SEMI_ARID("سهوب وشبه جاف", 1.15),
    ARID_OASIS("واحات ومناطق صحراوية", 1.40)
}

data class AgronomyCalculationInput(
    val crop: CropProfile,
    val areaValue: Double,
    val areaUnit: AreaUnit,
    val stage: GrowthStage,
    val soilNitrogenPpm: Double,
    val soilPhosphorusPpm: Double,
    val soilPotassiumPpm: Double,
    val soilPh: Double,
    val soilEcDsM: Double,
    val irrigationMethod: IrrigationMethod,
    val climateZone: ClimateZone
)

data class AgronomyCalculationResult(
    val areaHectares: Double,
    val netNPerHa: Double,
    val netPPerHa: Double,
    val netKPerHa: Double,
    val urea46KgHa: Double,
    val dapOrMapKgHa: Double,
    val potassiumSulfate50KgHa: Double,
    val totalFertilizerKg: Double,
    val dailyWaterM3: Double,
    val dailyWaterLiters: Double,
    val leachingFractionPercent: Int,
    val advisories: List<String>
)

object AgronomyCalculator {
    fun calculate(input: AgronomyCalculationInput): AgronomyCalculationResult {
        val areaHa = when (input.areaUnit) {
            AreaUnit.HECTARE -> max(0.01, input.areaValue)
            AreaUnit.SQUARE_METER -> max(0.0001, input.areaValue / 10_000.0)
        }

        // Soil reserve credit (rough agronomic conversion from ppm to kg/ha available in top 25cm)
        val nCredit = (input.soilNitrogenPpm * 0.65).coerceAtMost(input.crop.targetN * 0.6)
        val pCredit = (input.soilPhosphorusPpm * 0.85).coerceAtMost(input.crop.targetP * 0.65)
        val kCredit = (input.soilPotassiumPpm * 0.30).coerceAtMost(input.crop.targetK * 0.60)

        // pH fixation factor for Phosphorus & Micronutrients in alkaline Mediterranean soils
        val pPhFactor = when {
            input.soilPh >= 7.8 -> 1.25
            input.soilPh <= 5.5 -> 1.20
            else -> 1.0
        }

        val rawN = max(0.0, (input.crop.targetN - nCredit) * input.stage.nWeight)
        val rawP = max(0.0, (input.crop.targetP - pCredit) * input.stage.pWeight * pPhFactor)
        val rawK = max(0.0, (input.crop.targetK - kCredit) * input.stage.kWeight)

        // Commercial fertilizer equivalents (kg/ha)
        // Urea 46% N, MAP (12-61-0 -> 61% P2O5), Potassium Sulfate (0-0-50 -> 50% K2O)
        val ureaKgHa = rawN / 0.46
        val mapKgHa = rawP / 0.61
        val mopSopKgHa = rawK / 0.50

        val totalFertilizerForArea = (ureaKgHa + mapKgHa + mopSopKgHa) * areaHa

        // Irrigation calculation: ETc = ETo * Kc * climateFactor
        // 1 mm over 1 ha = 10 m3
        val etcMmDay = input.crop.baseEtMmDay * input.stage.kcFactor * input.climateZone.multiplier
        val grossMmDay = etcMmDay / input.irrigationMethod.efficiency

        // Salinity leaching fraction when EC exceeds crop threshold
        val excessEc = max(0.0, input.soilEcDsM - input.crop.salinityThresholdDsM)
        val leachingFraction = (excessEc * 0.08).coerceIn(0.0, 0.25)
        val finalWaterMmDay = grossMmDay * (1.0 + leachingFraction)

        val dailyM3 = finalWaterMmDay * 10.0 * areaHa
        val dailyLiters = dailyM3 * 1000.0

        val advisories = buildList {
            if (input.soilPh > 7.6) {
                add("التربة قاعدية/كلسية (pH ${String.format(Locale.US, "%.1f", input.soilPh)}): يرتفع تثبيت الفوسفور والحديد. يُنصح باستخدام سماد MAP الحامضي أو إضافة هيومات البوتاسيوم وحمض الفوسفوريك في شبكة التنقيط.")
            } else if (input.soilPh < 5.8) {
                add("التربة حامضية (pH ${String.format(Locale.US, "%.1f", input.soilPh)}): يُوصى بتصحيح الحموضة عبر الجير الزراعي (CaCO₃) لتحسين امتصاص الفوسفور والموليبدينوم.")
            } else {
                add("درجة حموضة التربة (${String.format(Locale.US, "%.1f", input.soilPh)}) مثالية لامتصاص معظم العناصر الغذائية الكبرى والصغرى.")
            }

            if (input.soilEcDsM > input.crop.salinityThresholdDsM) {
                val lfPct = (leachingFraction * 100).roundToInt()
                add("تنبيه ملوحة مرتفعة (${String.format(Locale.US, "%.1f", input.soilEcDsM)} dS/m): تم إضافة معامل غسيل ملحي بنسبة $lfPct% لحجم مياه الري مع تفضيل سلفات البوتاسيوم الخالية من الكلور.")
            }

            if (input.stage == GrowthStage.FLOWERING || input.stage == GrowthStage.FRUIT_FILLING) {
                add("مرحلة حساسة للعطش: يُفضل تقسيم مياه الري اليومية إلى ريات متكررة وقصيرة وتدعيم البوتاسيوم والكالسيوم لتعزيز جودة وصلابة الثمار.")
            }

            if (input.irrigationMethod == IrrigationMethod.SURFACE) {
                add("الانتقال من الري بالغمر إلى الري بالتنقيط يمكن أن يوفر قرابة 38% من كمية المياه اليومية ويرفع كفاءة التسميد الآزوتي.")
            }
        }

        return AgronomyCalculationResult(
            areaHectares = areaHa,
            netNPerHa = rawN,
            netPPerHa = rawP,
            netKPerHa = rawK,
            urea46KgHa = ureaKgHa,
            dapOrMapKgHa = mapKgHa,
            potassiumSulfate50KgHa = mopSopKgHa,
            totalFertilizerKg = totalFertilizerForArea,
            dailyWaterM3 = dailyM3,
            dailyWaterLiters = dailyLiters,
            leachingFractionPercent = (leachingFraction * 100).roundToInt(),
            advisories = advisories
        )
    }
}
