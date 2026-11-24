package com.example.data.ai

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import androidx.core.graphics.scale
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

// رابط الخادم الوسيط الآمن على Cloudflare
private const val PROXY_URL = "https://agro-pro-api.raoufmechalikh5.workers.dev"

private val MODELS = listOf(
    "gemini-3.8-flash",
    "gemini-3.8-pro",
    "gemini-3.5-flash"
)

class PlantDoctorRepository {

    suspend fun diagnosePlantImage(
        bitmap: Bitmap,
        farmerNotes: String
    ): Result<PlantDiagnosisResult> = withContext(Dispatchers.IO) {
        try {
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

            val geminiPayload = JSONObject().apply {
                put("contents", JSONArray().put(JSONObject().apply {
                    put("parts", JSONArray()
                        .put(JSONObject().put("inline_data", JSONObject()
                            .put("mime_type", "image/jpeg").put("data", b64)))
                        .put(JSONObject().put("text", prompt)))
                }))
                put("generationConfig", JSONObject().put("responseMimeType", "application/json"))
            }

            var lastErrorMsg = "تعذر الاتصال بخدمة الذكاء الاصطناعي."

            for (modelName in MODELS) {
                try {
                    val wrapperBody = JSONObject().apply {
                        put("model", modelName)
                        put("payload", geminiPayload)
                    }

                    val conn = URL(PROXY_URL).openConnection() as HttpURLConnection
                    conn.requestMethod = "POST"
                    conn.connectTimeout = 15_000
                    conn.readTimeout = 35_000
                    conn.doOutput = true
                    conn.setRequestProperty("Content-Type", "application/json")
                    conn.outputStream.use { it.write(wrapperBody.toString().toByteArray()) }

                    val code = conn.responseCode
                    val text = (if (code in 200..299) conn.inputStream else conn.errorStream)
                        ?.bufferedReader()?.use { it.readText() } ?: ""

                    if (code !in 200..299) {
                        Log.e(TAG, "HTTP $code on $modelName:$text")
                        val apiMsg = try {
                            JSONObject(text).getJSONObject("error").getString("message")
                        } catch (e: Exception) {
                            "رمز الخطأ $code"
                        }
                        lastErrorMsg = "[$modelName]$apiMsg"
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
                        lastErrorMsg = "[$modelName] أرجع الخادم رداً فارغاً."
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
                    lastErrorMsg = "[$modelName] ${e.localizedMessage ?: "خطأ في الاتصال"}"
                }
            }

            Result.failure(Exception(lastErrorMsg))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Plant diagnosis failed", e)
            Result.failure(Exception(e.localizedMessage ?: "تعذر إتمام التشخيص."))
        }
    }
}
