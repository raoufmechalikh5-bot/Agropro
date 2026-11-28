package com.example.ui.weather

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

data class AgriLocation(
    val name: String,
    val latitude: Double,
    val longitude: Double
)

enum class SpraySuitability {
    OPTIMAL,
    CAUTION,
    UNSUITABLE
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
    val diseaseRiskAlert: String?,
    val updatedAtMillis: Long = System.currentTimeMillis()
) {
    fun estimateDailyEt0Mm(): Double {
        val baseEt0 = (0.18 * temperatureC).coerceAtLeast(1.0)
        val drynessFactor = (1.0 + (60 - humidityPercent) * 0.008).coerceIn(0.65, 1.45)
        val windFactor = (1.0 + (windSpeedKmh - 8.0) * 0.015).coerceIn(0.85, 1.35)
        return (baseEt0 * drynessFactor * windFactor).coerceIn(1.0, 11.0)
    }

    fun toFieldNoteSummary(): String {
        return "الطقس (${locationName}): ${temperatureC.toInt()}°م | رطوبة ${humidityPercent}% | رياح ${windSpeedKmh.toInt()} كم/س ($conditionText)"
    }
}

object WeatherRepository {

    private const val PREFS_NAME = "agropro_weather_prefs"
    private const val KEY_NAME = "loc_name"
    private const val KEY_LAT = "loc_lat"
    private const val KEY_LON = "loc_lon"
    private const val KEY_CACHED_JSON = "cached_weather_json"

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

    fun saveCachedWeather(context: Context, data: AgriWeatherData) {
        val obj = JSONObject()
            .put("locationName", data.locationName)
            .put("temperatureC", data.temperatureC)
            .put("humidityPercent", data.humidityPercent)
            .put("windSpeedKmh", data.windSpeedKmh)
            .put("precipitationMm", data.precipitationMm)
            .put("weatherCode", data.weatherCode)
            .put("conditionText", data.conditionText)
            .put("sprayStatus", data.sprayStatus.name)
            .put("sprayAdvice", data.sprayAdvice)
            .put("diseaseRiskAlert", data.diseaseRiskAlert ?: "")
            .put("updatedAtMillis", data.updatedAtMillis)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_CACHED_JSON, obj.toString())
            .apply()
    }

    fun getCachedWeather(context: Context): AgriWeatherData? {
        val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_CACHED_JSON, null) ?: return null
        return try {
            val obj = JSONObject(raw)
            val alert = obj.optString("diseaseRiskAlert", "")
            AgriWeatherData(
                locationName = obj.getString("locationName"),
                temperatureC = obj.getDouble("temperatureC"),
                humidityPercent = obj.getInt("humidityPercent"),
                windSpeedKmh = obj.getDouble("windSpeedKmh"),
                precipitationMm = obj.getDouble("precipitationMm"),
                weatherCode = obj.getInt("weatherCode"),
                conditionText = obj.getString("conditionText"),
                sprayStatus = SpraySuitability.valueOf(obj.getString("sprayStatus")),
                sprayAdvice = obj.getString("sprayAdvice"),
                diseaseRiskAlert = alert.ifBlank { null },
                updatedAtMillis = obj.optLong("updatedAtMillis", System.currentTimeMillis())
            )
        } catch (_: Exception) {
            null
        }
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
            wind in 12.0..18.0 -> SpraySuitability.CAUTION to "رش بحذر: رياح متوسطة (${wind.toInt()} كم/س)، استخدم فوهات خشنة."
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

@Composable
fun AgriWeatherCard(
    modifier: Modifier = Modifier,
    expandedByDefault: Boolean = false
) {
    val context = LocalContext.current

    var selectedLocation by remember {
        mutableStateOf(WeatherRepository.getSavedLocation(context))
    }
    var weatherData by remember { mutableStateOf(WeatherRepository.getCachedWeather(context)) }
    var isLoading by remember { mutableStateOf(weatherData == null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var refreshTrigger by remember { mutableIntStateOf(0) }
    var showLocationDialog by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(expandedByDefault) }

    LaunchedEffect(selectedLocation, refreshTrigger) {
        isLoading = true
        errorMessage = null
        val result = WeatherRepository.fetchAgriWeather(selectedLocation)
        result.onSuccess { data ->
            weatherData = data
            WeatherRepository.saveCachedWeather(context, data)
            isLoading = false
        }.onFailure {
            errorMessage = "تعذر تحديث الطقس"
            isLoading = false
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .heightIn(
                min = if (isExpanded) 220.dp else 150.dp,
                max = if (isExpanded) 240.dp else 175.dp
            )
            .clickable { if (weatherData != null) isExpanded = !isExpanded },
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF4A9DE0),
        tonalElevation = 2.dp
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = painterResource(id = R.drawable.weather_sky_background),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0x55143F70),
                                Color(0x481C5D91),
                                Color(0x75416F9A)
                            )
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.08f))
            )
            // Subtle dark gradient behind weather labels and readings for stronger contrast.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.0f to Color(0x33203345),
                                0.42f to Color(0x14203345),
                                1.0f to Color(0x26203345)
                            )
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        onClick = { showLocationDialog = true },
                        shape = RoundedCornerShape(50),
                        color = Color.White.copy(alpha = 0.22f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "تغيير المنطقة",
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = selectedLocation.name,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isLoading && weatherData == null) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            IconButton(
                                onClick = { refreshTrigger++ },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "تحديث الطقس",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        IconButton(
                            onClick = { if (weatherData != null) isExpanded = !isExpanded },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (isExpanded) "طي البطاقة" else "توسيع البطاقة",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                if (weatherData != null) {
                    val data = weatherData!!
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0x6630475A), RoundedCornerShape(18.dp))
                            .padding(horizontal = 11.dp, vertical = 7.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = data.conditionText,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                                Text(
                                    text = "الطقس الحالي",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.94f)
                                )
                            }
                            Text(
                                text = "${data.temperatureC.toInt()}°",
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.22f), RoundedCornerShape(14.dp))
                                .padding(horizontal = 9.dp, vertical = 5.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CompactMetric(Icons.Default.WaterDrop, "الرطوبة ${data.humidityPercent}%")
                            CompactMetric(Icons.Default.Air, "الرياح ${data.windSpeedKmh.toInt()} كم/س")
                            CompactMetric(Icons.Default.WbSunny, "المطر ${String.format(Locale.US, "%.1f", data.precipitationMm)} مم")
                        }
                    }

                    val (sprayBg, sprayText, sprayIcon) = when (data.sprayStatus) {
                        SpraySuitability.OPTIMAL -> Triple(
                            Color(0xE51B6B43),
                            Color.White,
                            Icons.Default.CheckCircle
                        )
                        SpraySuitability.CAUTION -> Triple(
                            Color(0xE5A85A13),
                            Color.White,
                            Icons.Default.Warning
                        )
                        SpraySuitability.UNSUITABLE -> Triple(
                            Color(0xE5A83232),
                            Color.White,
                            Icons.Default.Warning
                        )
                    }
                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = sprayBg,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(7.dp)
                        ) {
                            Icon(
                                imageVector = sprayIcon,
                                contentDescription = null,
                                tint = sprayText,
                                modifier = Modifier.size(17.dp)
                            )
                            Text(
                                text = data.sprayAdvice,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = sprayText,
                                maxLines = if (isExpanded) 6 else 2,
                                overflow = TextOverflow.Clip
                            )
                        }
                    }
                    }

                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier.padding(top = 1.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "الرياح: ${data.windSpeedKmh.toInt()} كم/س • التبخر اليومي ET₀ ≈ ${String.format(Locale.US, "%.1f", data.estimateDailyEt0Mm())} ملم",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                            if (data.diseaseRiskAlert != null) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color.White.copy(alpha = 0.88f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = data.diseaseRiskAlert,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF24415B),
                                        modifier = Modifier.padding(9.dp)
                                    )
                                }
                            }
                        }
                    }
                } else if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }
            }
        }
    }

    if (showLocationDialog) {
        LocationPickerDialog(
            currentLocation = selectedLocation,
            onSelectLocation = { newLoc ->
                selectedLocation = newLoc
                WeatherRepository.saveLocation(context, newLoc)
                showLocationDialog = false
            },
            onDismiss = { showLocationDialog = false }
        )
    }
}

@Composable
private fun CompactMetric(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.92f),
            modifier = Modifier.size(13.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
private fun LocationPickerDialog(
    currentLocation: AgriLocation,
    onSelectLocation: (AgriLocation) -> Unit,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<AgriLocation>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تحديد موقع المزرعة") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { q ->
                        searchQuery = q
                        if (q.trim().length >= 2) {
                            scope.launch {
                                isSearching = true
                                searchResults = WeatherRepository.searchCity(q)
                                isSearching = false
                            }
                        } else {
                            searchResults = emptyList()
                        }
                    },
                    label = { Text("ابحث عن بلدية أو مدينة...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (isSearching) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else if (searchResults.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        searchResults.forEach { loc ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectLocation(loc) }
                            ) {
                                Text(
                                    text = loc.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                }

                HorizontalDivider()

                Text(
                    text = "أو اختر منطقة سريعة:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    WeatherRepository.presetLocations.forEach { preset ->
                        FilterChip(
                            selected = preset.name == currentLocation.name,
                            onClick = { onSelectLocation(preset) },
                            label = { Text(preset.name) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("إغلاق")
            }
        }
    )
}

enum class WeatherContextMode {
    IRRIGATION,
    FERTILIZER,
    NOTEBOOK
}

@Composable
fun WeatherContextBanner(
    mode: WeatherContextMode,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val weather = remember { WeatherRepository.getCachedWeather(context) } ?: return
    val clipboard = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    val (compactTitle, fullAdvice, accentColor) = when (mode) {
        WeatherContextMode.IRRIGATION -> {
            val et0 = String.format(Locale.US, "%.1f", weather.estimateDailyEt0Mm())
            val rainNote = if (weather.precipitationMm > 0.5) {
                " • أمطار ${weather.precipitationMm} ملم (اخفض الري)"
            } else {
                " • التبخر ET₀ ≈ $et0 ملم/يوم"
            }
            Triple(
                "${weather.locationName}: ${weather.temperatureC.toInt()}°م$rainNote",
                "الرطوبة ${weather.humidityPercent}% والرياح ${weather.windSpeedKmh.toInt()} كم/س. يُنصح بضبط التبخر اليومي على $et0 ملم/يوم.",
                MaterialTheme.colorScheme.primary
            )
        }
        WeatherContextMode.FERTILIZER -> {
            val isUnsuitable = weather.sprayStatus == SpraySuitability.UNSUITABLE
            val shortStatus = when {
                weather.precipitationMm > 2.0 -> "أمطار: قسّم دفعات النيتروجين (N)"
                isUnsuitable -> "تنبيه: الجو غير مناسب للرش الورقي"
                else -> "الطقس مناسب للتسميد (${weather.temperatureC.toInt()}°م)"
            }
            val detail = when {
                weather.precipitationMm > 2.0 -> "الأمطار المسجلة (${weather.precipitationMm} ملم) قد تغسل النيتروجين؛ قسّم السماد على دفعات."
                isUnsuitable -> weather.sprayAdvice
                else -> "درجة الحرارة (${weather.temperatureC.toInt()}°م) والرطوبة (${weather.humidityPercent}%) مناسبتان للتسميد الأرضي والورقي."
            }
            Triple(
                "${weather.locationName} • $shortStatus",
                detail,
                if (isUnsuitable) MaterialTheme.colorScheme.error else Color(0xFF2E7D32)
            )
        }
        WeatherContextMode.NOTEBOOK -> {
            Triple(
                weather.toFieldNoteSummary(),
                "اضغط «نسخ» لإرفاق هذه الظروف الجوية في ملاحظات التجربة أو المعاملة الحقلية.",
                MaterialTheme.colorScheme.secondary
            )
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(12.dp),
        color = accentColor.copy(alpha = 0.09f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.WbSunny,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = compactTitle,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = accentColor,
                    maxLines = 2,
                    modifier = Modifier.weight(1f)
                )
                if (mode == WeatherContextMode.NOTEBOOK) {
                    Text(
                        text = if (copied) "تم ✓" else "نسخ",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        modifier = Modifier
                            .clickable {
                                clipboard.setText(AnnotatedString(weather.toFieldNoteSummary()))
                                copied = true
                            }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            AnimatedVisibility(visible = expanded) {
                Text(
                    text = fullAdvice,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
