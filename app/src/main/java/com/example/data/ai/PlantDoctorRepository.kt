package com.example.data.ai

import android.graphics.Bitmap
import android.util.Log
import androidx.core.graphics.scale
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.RequestOptions
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.ThinkingLevel
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import com.google.firebase.ai.type.thinkingConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import kotlin.math.max

private const val TAG = "PlantDoctorRepository"

class PlantDoctorRepository {

    private val diagnosisSchema = Schema.obj(
        properties = mapOf(
            "cropNameAr" to Schema.string(
                description = "اسم المحصول أو النبات الظاهر في الصورة باللغة العربية (مثال: الطماطم، الزيتون، البطاطا، الحمضيات، القمح)"
            ),
            "diseaseNameAr" to Schema.string(
                description = "اسم المرض أو الآفة أو الحالة باللغة العربية الفصحى العلمية"
            ),
            "scientificName" to Schema.string(
                description = "الاسم العلمي اللاتيني الدقيق للمسبب المرضي أو اسم النبات إذا كان سليماً (مثل Phytophthora infestans)"
            ),
            "cause" to Schema.enumeration(
                values = listOf("فطري", "بكتيري", "فيروسي", "حشري", "نقص عناصر غذائية", "سليم"),
                description = "تصنيف نوع المسبب المرضي"
            ),
            "severity" to Schema.enumeration(
                values = listOf("منخفضة", "متوسطة", "مرتفعة", "حرجة"),
                description = "درجة خطورة الإصابة الظاهرة في الصورة"
            ),
            "activeIngredient" to Schema.string(
                description = "المادة الفعالة الموصى بها للمكافحة (أو المعاملة السمادية في حال نقص العناصر، أو لا يلزم في حال النبات السليم)"
            ),
            "phiDays" to Schema.string(
                description = "فترة التحريم قبل الجني PHI (مثال: 7 أيام قبل الحصاد)"
            ),
            "summary" to Schema.string(
                description = "وصف تشخيصي دقيق للأعراض الظاهرة فعلياً في الصورة باللغة العربية"
            ),
            "recommendations" to Schema.array(
                items = Schema.string(),
                description = "قائمة من 3 إلى 4 توصيات زراعية وعلاجية ووقائية عملية"
            )
        )
    )

    private val config = generationConfig {
        responseMimeType = "application/json"
        responseSchema = diagnosisSchema
        thinkingConfig = thinkingConfig {
            thinkingLevel = ThinkingLevel.LOW
        }
    }

    private val model by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = config,
            requestOptions = RequestOptions(timeoutInMillis = 90_000L)
        )
    }

    suspend fun diagnosePlantImage(
        bitmap: Bitmap,
        farmerNotes: String
    ): Result<PlantDiagnosisResult> {
        return try {
            val preparedBitmap = withContext(Dispatchers.IO) {
                val maxDim = 768
                val largest = max(bitmap.width, bitmap.height)
                val scaled = if (largest > maxDim) {
                    val ratio = maxDim.toFloat() / largest.toFloat()
                    bitmap.scale(
                        (bitmap.width * ratio).toInt().coerceAtLeast(1),
                        (bitmap.height * ratio).toInt().coerceAtLeast(1)
                    )
                } else {
                    bitmap
                }
                if (scaled.config != Bitmap.Config.ARGB_8888) {
                    scaled.copy(Bitmap.Config.ARGB_8888, false)
                } else {
                    scaled
                }
            }

            val promptText = buildString {
                append(
                    "أنت مهندس وقاية نباتات وخبير أمراض نباتية. افحص الصورة المرفقة فعلياً وبعناية فائقة:\n" +
                        "1. حدد نوع المحصول أو النبات الظاهر في الصورة (cropNameAr).\n" +
                        "2. شخص المرض أو الآفة أو نقص العناصر الغذائية بدقة استناداً إلى الأعراض المرئية في الصورة (البقع، الهالات، الأنفاق، الذبول، التلون)، مع كتابة الاسم العربي (diseaseNameAr) والاسم العلمي اللاتيني الدقيق (scientificName).\n" +
                        "3. صنف المسبب (cause): فطري، بكتيري، فيروسي، حشري، نقص عناصر غذائية، أو سليم.\n" +
                        "4. حدد درجة الخطورة، المادة الفعالة الموصى بها (activeIngredient)، وفترة التحريم قبل الجني PHI (phiDays)، مع 3 إلى 4 توصيات عملية موجزة."
                )
                if (farmerNotes.isNotBlank()) {
                    append("\nملاحظات المزارع أو الطالب المرفقة: $farmerNotes")
                }
            }

            val accumulatedJson = StringBuilder()
            withContext(Dispatchers.IO) {
                withTimeout(90_000L) {
                    model.generateContentStream(
                        content {
                            image(preparedBitmap)
                            text(promptText)
                        }
                    ).collect { chunk ->
                        chunk.text?.let { accumulatedJson.append(it) }
                    }
                }
            }

            val jsonText = accumulatedJson.toString()
            if (jsonText.isBlank()) {
                return Result.failure(IllegalStateException("لم يُرجع النموذج أي نتيجة تشخيصية."))
            }

            val json = JSONObject(jsonText)
            val recsArray = json.optJSONArray("recommendations")
            val recommendations = buildList {
                if (recsArray != null) {
                    for (i in 0 until recsArray.length()) {
                        val item = recsArray.optString(i)
                        if (item.isNotBlank()) add(item)
                    }
                }
            }

            Result.success(
                PlantDiagnosisResult(
                    cropNameAr = json.optString("cropNameAr", "نبات غير محدد"),
                    diseaseNameAr = json.optString("diseaseNameAr", "تشخيص غير محدد"),
                    scientificName = json.optString("scientificName", "Unknown species"),
                    cause = json.optString("cause", "فطري"),
                    severity = json.optString("severity", "متوسطة"),
                    activeIngredient = json.optString("activeIngredient", "يرجى استشارة مهندس وقاية النبات"),
                    phiDays = json.optString("phiDays", "حسب بطاقة المبيد"),
                    summary = json.optString("summary", ""),
                    recommendations = recommendations
                )
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Plant diagnosis timed out after 90s", e)
            Result.failure(Exception("انتهت مهلة التحليل (90 ثانية). تحقق من اتصال الإنترنت وحاول مجدداً."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Plant diagnosis failed", e)
            Result.failure(Exception(e.localizedMessage ?: "تعذر إتمام التشخيص عبر Gemini."))
        }
    }
}
