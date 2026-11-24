package com.example.data.ai

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import androidx.core.graphics.scale
import com.example.BuildConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.max

private const val TAG = "PlantDoctorRepository"

// قائمة النماذج مرتبة حسب الأولوية: يبدأ بالأحدث وينتقل تلقائياً للاحتياطي عند الضغط
private val MODELS = listOf(
    "gemini-3.8-flash",
    "gemini-3.8-flash-lite",
    "gemini-3.0-flash"
)

class PlantDoctorRepository {

    suspend fun diagnosePlantImage(
        bitmap: Bitmap,
        farmerNotes: String
    ): Result<PlantDiagnosisResult> = withContext(Dispatchers.IO) {
        try {
            val key = BuildConfig.GEMINI_API_KEY
            if (key.isBlank()) {
                return@withContext Result.failure(Exception("مفتاح الذكاء الاصطناعي غير مضبوط في التطبيق."))
            }

            // تصغير الصورة إلى 512 بكسل لتسريع الرفع والمعالجة
            val largest = max(bitmap.width, bitmap.height)
            var img = bitmap
            if (largest > 512) {
                val r = 512f / largest
                img = bitmap.scale(
                    (bitmap.width * r).toInt().coerceAtLeast(1),
                    (bitmap.height * r).toInt().coerceAtLeast(1)
                )
            }
            if (img.config != Bitmap.Config.ARGB_8888) {
                img = img.copy(Bitmap.Config.ARGB_8888, false)
            }
            val out = ByteArrayOutputStream()
            img.compress(Bitmap.CompressFormat.JPEG, 75, out)
            val b64 = Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)

            val prompt = buildString {
                append(
                    "أنت مهندس وقاية نباتات. افحص الصورة بعناية وأجب بصيغة JSON فقط بالمفاتيح التالية: " +
                        "cropNameAr (اسم المحصول بالعربية)، diseaseNameAr (اسم المرض أو الحالة بالعربية)، " +
                        "scientificName (الاسم العلمي)، cause (واحدة فقط من: فطري، بكتيري، فيروسي، حشري، نقص عناصر غذائية، سليم)، " +
                        "severity (واحدة فقط من: منخفضة، متوسطة، مرتفعة، حرجة)، " +
                        "activeIngredient (المادة الفعالة أو المعاملة الموصى بها)، " +
                        "phiDays (فترة التحريم قبل الجني)، summary (وصف الأعراض الظاهرة)، " +
                        "recommendations (مصفوفة من 3 إلى 4 توصيات عملية)."
                )
                if (farmerNotes.isNotBlank()) append("\nملاحظات المزارع: $farmerNotes")
            }

            val genConfig = JSONObject().apply {
                put("responseMimeType", "application/json")
                put("thinkingConfig", JSONObject().put("thinkingBudget", 0))
            }

            val body = JSONObject().apply {
                put("contents", JSONArray().put(JSONObject().apply {
                    put("parts", JSONArray()
                        .put(JSONObject().put("inline_data", JSONObject()
                            .put("mime_type", "image/jpeg").put("data", b64)))
                        .put(JSONObject().put("text", prompt)))
                }))
                put("generationConfig", genConfig)
            }

            val payloadBytes = body.toString().toByteArray()
            var lastErrorMsg = "الخوادم مشغولة حالياً، يرجى المحاولة بعد قليل."

            // تجربة النماذج بالترتيب عند وجود ضغط
            for (modelName in MODELS) {
                try {
                    val conn = URL("https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent")
                        .openConnection() as HttpURLConnection
                    conn.requestMethod = "POST"
                    conn.connectTimeout = 12_000
                    conn.readTimeout = 20_000
                    conn.doOutput = true
                    conn.setRequestProperty("Content-Type", "application/json")
                    conn.setRequestProperty("x-goog-api-key", key)
                    conn.outputStream.use { it.write(payloadBytes) }

                    val code = conn.responseCode
                    val text = (if (code in 200..299) conn.inputStream else conn.errorStream)
                        ?.bufferedReader()?.use { it.readText() } ?: ""

                    // إذا كان هناك ضغط (503) أو تجاوز حد الطلبات (429) أو النموذج غير متاح (404)، انتقل للتالي فوراً
                    if (code == 503 || code == 429 || code == 404) {
                        Log.w(TAG, "Model $modelName busy/unavailable (HTTP$code), switching to fallback...")
                        continue
                    }

                    if (code !in 200..299) {
                        Log.e(TAG, "HTTP $code on $modelName:$text")
                        lastErrorMsg = if (code == 400 || code == 403) {
                            "خطأ في صلاحية مفتاح API (رمز $code)."
                        } else {
                            "تعذر الاتصال بخدمة التشخيص (رمز $code)."
                        }
                        continue
                    }

                    val parts = JSONObject(text).getJSONArray("candidates").getJSONObject(0)
                        .getJSONObject("content").getJSONArray("parts")
                    val raw = buildString {
                        for (i in 0 until parts.length()) {
                            val part = parts.getJSONObject(i)
                            if (!part.optBoolean("thought", false)) {
                                append(part.optString("text"))
                            }
                        }
                    }.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()

                    if (raw.isBlank()) {
                        continue
                    }

                    val json = JSONObject(raw)
                    val arr = json.optJSONArray("recommendations")
                    val recs = buildList {
                        if (arr != null) for (i in 0 until arr.length()) {
                            val s = arr.optString(i)
                            if (s.isNotBlank()) add(s)
                        }
                    }

                    return@withContext Result.success(
                        PlantDiagnosisResult(
                            cropNameAr = json.optString("cropNameAr", "نبات غير محدد"),
                            diseaseNameAr = json.optString("diseaseNameAr", "تشخيص غير محدد"),
                            scientificName = json.optString("scientificName", "Unknown species"),
                            cause = json.optString("cause", "فطري"),
                            severity = json.optString("severity", "متوسطة"),
                            activeIngredient = json.optString("activeIngredient", "يرجى استشارة مهندس وقاية النبات"),
                            phiDays = json.optString("phiDays", "حسب بطاقة المبيد"),
                            summary = json.optString("summary", ""),
                            recommendations = recs
                        )
                    )
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.w(TAG, "Error with model $modelName: ${e.message}")
                    lastErrorMsg = "يرجى التأكد من اتصال الإنترنت والمحاولة مرة أخرى."
                }
            }

            Result.failure(Exception(lastErrorMsg))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Plant diagnosis failed", e)
            Result.failure(Exception("تعذر إتمام التشخيص، يرجى المحاولة مرة أخرى."))
        }
    }
}
