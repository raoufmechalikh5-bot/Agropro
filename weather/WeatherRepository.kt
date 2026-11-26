package com.example.data.weather

import android.content.Context
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

data class AgriLocation(
    val name: String,
    val latitude: Double,
    val longitude: Double
)

enum class SpraySuitability {
    OPTIMAL,   // مناسب للرش
    CAUTION,   // حذر (رياح متوسطة أو رطوبة مرتفعة)
    UNSUITABLE // غير مناسب (أمطار أو رياح قوية)
}

data class AgriWeatherData(
    val locationName: String,
    val temperatureC: Double,
    val humidityPercent: Int,
    val windSpeedKmh: Double,
    val precipitationMm: Double,
    val weatherCode: Int,
    val conditionText: String,
    val sprayStatus: SpraySuitability,
    val sprayAdvice: String,
    val diseaseRiskAlert: String?
)

object WeatherRepository {

    private const val PREFS_NAME = "agropro_weather_prefs"
    private const val KEY_NAME = "loc_name"
    private const val KEY_LAT = "loc_lat"
    private const val KEY_LON = "loc_lon"

    // قائمة ولايات ومناطق فلاحية جاهزة للاختيار السريع
    val presetLocations = listOf(
        AgriLocation("عين الدفلى", 36.2641, 1.9679),
        AgriLocation("البليدة (متيجة)", 36.4700, 2.8277),
        AgriLocation("الشلف", 36.1652, 1.3345),
        AgriLocation("بسكرة", 34.8504, 5.7280),
        AgriLocation("الوادي", 33.3561, 6.8632),
        AgriLocation("سطيف", 36.1911, 5.4137),
        AgriLocation("مستغانم", 35.9311, 0.0892),
        AgriLocation("معسكر", 35.3966, 0.1403),
        AgriLocation("تيارت", 35.3710, 1.3170),
        AgriLocation("الجزائر العاصمة", 36.7538, 3.0588),
        AgriLocation("قسنطينة", 36.3650, 6.6147),
        AgriLocation("تلمسان", 34.8783, -1.3150)
    )

    fun getSavedLocation(context: Context): AgriLocation {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val name = prefs.getString(KEY_NAME, null)
        val lat = prefs.getFloat(KEY_LAT, Float.NaN)
        val lon = prefs.getFloat(KEY_LON, Float.NaN)
        return if (name != null && !lat.isNaN() && !lon.isNaN()) {
            AgriLocation(name, lat.toDouble(), lon.toDouble())
        } else {
            presetLocations.first()
        }
    }

    fun saveLocation(context: Context, location: AgriLocation) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_NAME, location.name)
            .putFloat(KEY_LAT, location.latitude.toFloat())
            .putFloat(KEY_LON, location.longitude.toFloat())
            .apply()
    }

    suspend fun searchCity(query: String): List<AgriLocation> = withContext(Dispatchers.IO) {
        val clean = query.trim()
        if (clean.length < 2) return@withContext emptyList()
        try {
            val encoded = URLEncoder.encode(clean, "UTF-8")
            val url = URL("https://geocoding-api.open-meteo.com/v1/search?name=$encoded&count=6&language=ar&format=json")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 8000
                readTimeout = 8000
            }
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(body)
            val results = json.optJSONArray("results") ?: return@withContext emptyList()
            List(results.length()) { i ->
                val item = results.getJSONObject(i)
                val cityName = item.optString("name", clean)
                val admin1 = item.optString("admin1", "")
                val country = item.optString("country", "")
                val displayName = listOf(cityName, admin1, country)
                    .filter { it.isNotBlank() }
                    .distinct()
                    .joinToString("، ")
                AgriLocation(
                    name = displayName,
                    latitude = item.getDouble("latitude"),
                    longitude = item.getDouble("longitude")
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun fetchAgriWeather(location: AgriLocation): Result<AgriWeatherData> = withContext(Dispatchers.IO) {
        try {
            val apiUrl = "https://api.open-meteo.com/v1/forecast" +
                "?latitude=${location.latitude}" +
                "&longitude=${location.longitude}" +
                "&current=temperature_2m,relative_humidity_2m,precipitation,weather_code,wind_speed_10m" +
                "&timezone=auto"
            val conn = (URL(apiUrl).openConnection() as HttpURLConnection).apply {
                connectTimeout = 10000
                readTimeout = 10000
            }
            val responseText = conn.inputStream.bufferedReader().use { it.readText() }
            val root = JSONObject(responseText)
            val current = root.getJSONObject("current")

            val temp = current.getDouble("temperature_2m")
            val humidity = current.getInt("relative_humidity_2m")
            val wind = current.getDouble("wind_speed_10m")
            val precip = current.getDouble("precipitation")
            val code = current.getInt("weather_code")

            val (sprayStatus, sprayAdvice) = evaluateSprayConditions(temp, humidity, wind, precip, code)
            val diseaseAlert = evaluateDiseaseRisk(temp, humidity, precip)

            Result.success(
                AgriWeatherData(
                    locationName = location.name,
                    temperatureC = temp,
                    humidityPercent = humidity,
                    windSpeedKmh = wind,
                    precipitationMm = precip,
                    weatherCode = code,
                    conditionText = mapWeatherCodeToArabic(code),
                    sprayStatus = sprayStatus,
                    sprayAdvice = sprayAdvice,
                    diseaseRiskAlert = diseaseAlert
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun evaluateSprayConditions(
        temp: Double,
        humidity: Int,
        wind: Double,
        precip: Double,
        code: Int
    ): Pair<SpraySuitability, String> {
        val isRaining = precip > 0.1 || code in listOf(51, 53, 55, 61, 63, 65, 80, 81, 82, 95, 96, 99)
        return when {
            isRaining -> SpraySuitability.UNSUITABLE to "غير مناسب للرش: تساقط أمطار قد يغسل المبيد."
            wind > 18.0 -> SpraySuitability.UNSUITABLE to "غير مناسب للرش: رياح قوية (${wind.toInt()} كم/س) تسبب انجراف الرذاذ."
            temp > 33.0 -> SpraySuitability.UNSUITABLE to "غير مناسب للرش: حرارة مرتفعة تسبب تبخر المحلول واحتراق الأوراق."
            wind in 12.0..18.0 -> SpraySuitability.CAUTION to "رش بحذر: رياح متوسطة، استخدم فوهات قطرات خشنة."
            humidity < 40 -> SpraySuitability.CAUTION to "رش بحذر: جفاف الجو يسرّع تبخر الرذاذ، يُفضل الرش صباحاً."
            else -> SpraySuitability.OPTIMAL to "مناسب جداً للرش والتسميد الورقي الآن."
        }
    }

    private fun evaluateDiseaseRisk(temp: Double, humidity: Int, precip: Double): String? {
        return when {
            humidity >= 80 && temp in 14.0..26.0 ->
                "تنبيه وقائي: رطوبة عالية (${humidity}%) وحرارة معتدلة ترفع خطر الفطريات (البياض الزغبي وعين الطاووس)."
            temp <= 3.0 ->
                "تنبيه صقيع: انخفاض حاد في الحرارة قد يضر بالبراعم والشتلات."
            temp >= 37.0 ->
                "إجهاد حراري: يُنصح بتقريب فترات الري في الصباح الباكر."
            precip > 2.0 ->
                "رطوبة أرضية مرتفعة: راقب تصريف المياه لتجنب أعفان الجذور."
            else -> null
        }
    }

    private fun mapWeatherCodeToArabic(code: Int): String = when (code) {
        0 -> "سماء صافية"
        1, 2 -> "غائم جزئياً"
        3 -> "غائم كلياً"
        45, 48 -> "ضباب"
        51, 53, 55 -> "رذاذ خفيف"
        61, 63, 65 -> "أمطار"
        71, 73, 75 -> "ثلوج"
        80, 81, 82 -> "زخات مطرية"
        95, 96, 99 -> "عواصف رعدية"
        else -> "معتدل"
    }
}
