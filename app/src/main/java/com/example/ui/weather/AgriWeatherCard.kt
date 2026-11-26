package com.example.ui.weather

import android.app.AlertDialog
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
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
    val diseaseRiskAlert: String?
)

object WeatherRepository {

    private const val PREFS_NAME = "agropro_weather_prefs"
    private const val KEY_NAME = "loc_name"
    private const val KEY_LAT = "loc_lat"
    private const val KEY_LON = "loc_lon"

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

@Composable
fun AgriWeatherCard(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var selectedLocation by remember {
        mutableStateOf(WeatherRepository.getSavedLocation(context))
    }
    var weatherData by remember { mutableStateOf<AgriWeatherData?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var refreshTrigger by remember { mutableIntStateOf(0) }
    var showLocationDialog by remember { mutableStateOf(false) }

    LaunchedEffect(selectedLocation, refreshTrigger) {
        isLoading = true
        errorMessage = null
        val result = WeatherRepository.fetchAgriWeather(selectedLocation)
        result.onSuccess { data ->
            weatherData = data
            isLoading = false
        }.onFailure {
            errorMessage = "تعذر تحديث الطقس. تحقق من الاتصال بالإنترنت."
            isLoading = false
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    onClick = { showLocationDialog = true },
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "تغيير المنطقة",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = selectedLocation.name,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (weatherData != null && !isLoading) {
                        Text(
                            text = weatherData!!.conditionText,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    IconButton(
                        onClick = { refreshTrigger++ },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "تحديث الطقس",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            if (isLoading && weatherData == null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "جاري جلب بيانات الطقس الزراعي...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else if (errorMessage != null && weatherData == null) {
                Text(
                    text = errorMessage!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            } else if (weatherData != null) {
                val data = weatherData!!

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    WeatherMetricChip(
                        icon = Icons.Default.WbSunny,
                        label = "الحرارة",
                        value = "${data.temperatureC.toInt()}°م",
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    WeatherMetricChip(
                        icon = Icons.Default.WaterDrop,
                        label = "الرطوبة",
                        value = "${data.humidityPercent}%",
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    WeatherMetricChip(
                        icon = Icons.Default.Air,
                        label = "الرياح",
                        value = "${data.windSpeedKmh.toInt()} كم/س",
                        modifier = Modifier.weight(1f)
                    )
                }

                val (badgeBg, badgeText, badgeIcon) = when (data.sprayStatus) {
                    SpraySuitability.OPTIMAL -> Triple(
                        Color(0xFF1B5E20).copy(alpha = 0.14f),
                        Color(0xFF2E7D32),
                        Icons.Default.CheckCircle
                    )
                    SpraySuitability.CAUTION -> Triple(
                        Color(0xFFE65100).copy(alpha = 0.14f),
                        Color(0xFFEF6C00),
                        Icons.Default.Warning
                    )
                    SpraySuitability.UNSUITABLE -> Triple(
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f),
                        MaterialTheme.colorScheme.error,
                        Icons.Default.Warning
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = badgeBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = badgeIcon,
                            contentDescription = null,
                            tint = badgeText,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = data.sprayAdvice,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = badgeText
                        )
                    }
                }

                if (data.diseaseRiskAlert != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.65f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = data.diseaseRiskAlert,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                        )
                    }
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
private fun WeatherMetricChip(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
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
