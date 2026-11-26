package com.example.reminders

import android.Manifest
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.ui.weather.SpraySuitability
import com.example.ui.weather.WeatherRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import org.json.JSONArray
import org.json.JSONObject

const val REPEAT_NONE = 0
const val REPEAT_DAILY = 1
const val REPEAT_WEEKLY = 2

data class Reminder(
    val id: Int,
    val title: String,
    val timeMillis: Long,
    val repeat: Int
)

object ReminderStore {
    private const val PREFS = "agropro_reminders"
    private const val KEY = "list"

    fun load(context: Context): List<Reminder> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                Reminder(
                    id = o.getInt("id"),
                    title = o.getString("title"),
                    timeMillis = o.getLong("time"),
                    repeat = o.optInt("repeat", REPEAT_NONE)
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun save(context: Context, list: List<Reminder>) {
        val arr = JSONArray()
        list.forEach { r ->
            arr.put(
                JSONObject()
                    .put("id", r.id)
                    .put("title", r.title)
                    .put("time", r.timeMillis)
                    .put("repeat", r.repeat)
            )
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY, arr.toString()).apply()
    }
}

object ReminderScheduler {
    const val CHANNEL_ID = "agropro_reminders"
    const val EXTRA_ID = "reminder_id"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "تذكيرات المزرعة",
                    NotificationManager.IMPORTANCE_HIGH
                )
            )
        }
    }

    private fun alarmIntent(context: Context, id: Int): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).putExtra(EXTRA_ID, id)
        return PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun schedule(context: Context, reminder: Reminder) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            reminder.timeMillis,
            alarmIntent(context, reminder.id)
        )
    }

    fun cancel(context: Context, id: Int) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(alarmIntent(context, id))
    }

    fun nextTime(reminder: Reminder, now: Long): Long? {
        if (reminder.timeMillis > now) return reminder.timeMillis
        if (reminder.repeat == REPEAT_NONE) return null
        val step = if (reminder.repeat == REPEAT_DAILY) 1 else 7
        val cal = Calendar.getInstance().apply { timeInMillis = reminder.timeMillis }
        while (cal.timeInMillis <= now) {
            cal.add(Calendar.DAY_OF_YEAR, step)
        }
        return cal.timeInMillis
    }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra(ReminderScheduler.EXTRA_ID, -1)
        val all = ReminderStore.load(context)
        val reminder = all.firstOrNull { it.id == id } ?: return

        ReminderScheduler.ensureChannel(context)
        val canNotify = Build.VERSION.SDK_INT < 33 ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

        if (canNotify) {
            // فحص حارس الطقس للمهام المرتبطة بالرش أو الري أو التسميد
            val cachedWeather = WeatherRepository.getCachedWeather(context)
            val isSprayTask = listOf("رش", "مبيد", "ورقي", "فطري", "حشري", "وقاية").any {
                reminder.title.contains(it)
            }
            val isIrrigationTask = listOf("ري", "سقي").any { reminder.title.contains(it) }

            val weatherWarning = when {
                cachedWeather != null && isSprayTask && cachedWeather.sprayStatus == SpraySuitability.UNSUITABLE ->
                    "\n⚠️ تحذير طقس (${cachedWeather.locationName}): ${cachedWeather.sprayAdvice}"
                cachedWeather != null && isIrrigationTask && cachedWeather.precipitationMm > 2.0 ->
                    "\n🌧️ تنبيه طقس: توجد أمطار مسجلة (${cachedWeather.precipitationMm} ملم)، تحقق من رطوبة التربة قبل الري."
                else -> ""
            }

            val notificationTitle = if (weatherWarning.isNotEmpty()) {
                "⚠️ Agro pro • تنبيه طقس للمهمة"
            } else {
                "Agro pro • تذكير حقلي"
            }
            val fullText = reminder.title + weatherWarning

            val builder = NotificationCompat.Builder(context, ReminderScheduler.CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_popup_reminder)
                .setContentTitle(notificationTitle)
                .setContentText(fullText)
                .setStyle(NotificationCompat.BigTextStyle().bigText(fullText))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)

            val launch = context.packageManager.getLaunchIntentForPackage(context.packageName)
            if (launch != null) {
                builder.setContentIntent(
                    PendingIntent.getActivity(
                        context,
                        id,
                        launch,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                )
            }
            context.getSystemService(NotificationManager::class.java).notify(id, builder.build())
        }

        if (reminder.repeat == REPEAT_NONE) {
            ReminderStore.save(context, all.filter { it.id != id })
        } else {
            val next = ReminderScheduler.nextTime(reminder, System.currentTimeMillis())
            if (next != null) {
                val updated = reminder.copy(timeMillis = next)
                ReminderStore.save(context, all.map { if (it.id == id) updated else it })
                ReminderScheduler.schedule(context, updated)
            }
        }
    }
}

class ReminderBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val now = System.currentTimeMillis()
        val kept = mutableListOf<Reminder>()
        ReminderStore.load(context).forEach { r ->
            val next = ReminderScheduler.nextTime(r, now)
            if (next != null) {
                val updated = r.copy(timeMillis = next)
                kept.add(updated)
                ReminderScheduler.schedule(context, updated)
            }
        }
        ReminderStore.save(context, kept)
    }
}

private fun pickDateTime(context: Context, initial: Long, onPicked: (Long) -> Unit) {
    val c = Calendar.getInstance().apply { timeInMillis = initial }
    DatePickerDialog(
        context,
        { _, year, month, day ->
            TimePickerDialog(
                context,
                { _, hour, minute ->
                    val picked = Calendar.getInstance().apply {
                        set(year, month, day, hour, minute, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    onPicked(picked.timeInMillis)
                },
                c.get(Calendar.HOUR_OF_DAY),
                c.get(Calendar.MINUTE),
                true
            ).show()
        },
        c.get(Calendar.YEAR),
        c.get(Calendar.MONTH),
        c.get(Calendar.DAY_OF_MONTH)
    ).show()
}

private fun repeatLabel(repeat: Int): String = when (repeat) {
    REPEAT_DAILY -> "يومياً"
    REPEAT_WEEKLY -> "أسبوعياً"
    else -> "مرة واحدة"
}

@Composable
fun RemindersDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val formatter = remember { SimpleDateFormat("EEEE d MMMM yyyy - HH:mm", Locale("ar")) }
    val cachedWeather = remember { WeatherRepository.getCachedWeather(context) }

    var reminders by remember {
        mutableStateOf(ReminderStore.load(context).sortedBy { it.timeMillis })
    }
    var title by remember { mutableStateOf("") }
    var whenMillis by remember { mutableLongStateOf(System.currentTimeMillis() + 60 * 60 * 1000L) }
    var repeat by remember { mutableIntStateOf(REPEAT_NONE) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    fun addBatchReminders(items: List<Pair<String, Int>>) {
        val now = System.currentTimeMillis()
        val baseId = (now % 1000000L).toInt()
        val newList = items.mapIndexed { idx, (taskTitle, daysOffset) ->
            val cal = Calendar.getInstance().apply {
                timeInMillis = now
                add(Calendar.DAY_OF_YEAR, daysOffset)
                set(Calendar.HOUR_OF_DAY, 7)
                set(Calendar.MINUTE, 30)
                set(Calendar.SECOND, 0)
                if (timeInMillis <= now) add(Calendar.DAY_OF_YEAR, 1)
            }
            Reminder(
                id = baseId + idx + 1,
                title = taskTitle,
                timeMillis = cal.timeInMillis,
                repeat = REPEAT_NONE
            )
        }
        val merged = (reminders + newList).sortedBy { it.timeMillis }
        ReminderStore.save(context, merged)
        ReminderScheduler.ensureChannel(context)
        newList.forEach { ReminderScheduler.schedule(context, it) }
        reminders = merged
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("إغلاق") }
        },
        title = { Text("التذكيرات الذكية") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // شريط حالة الطقس وصلاحية الرش في المزرعة
                if (cachedWeather != null) {
                    val isOptimal = cachedWeather.sprayStatus == SpraySuitability.OPTIMAL
                    val bgColor = if (isOptimal) {
                        Color(0xFF1B5E20).copy(alpha = 0.12f)
                    } else {
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.65f)
                    }
                    val fgColor = if (isOptimal) {
                        Color(0xFF2E7D32)
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = bgColor,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (isOptimal) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = fgColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "${cachedWeather.locationName}: ${cachedWeather.sprayAdvice}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = fgColor
                            )
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("مركز التذكيرات الزراعية", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                            Text("أنشئ برنامجاً جاهزاً أو أضف مهمة خاصة بك", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                Text("برامج جاهزة حسب المحصول", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AssistChip(
                        onClick = {
                            addBatchReminders(
                                listOf(
                                    "رش وقائي بالنحاس (وقاية الزيتون)" to 0,
                                    "تسميد ورقي بالبورون والزنك قبل الإزهار" to 10,
                                    "فحص مصائد ذبابة ثمار الزيتون" to 21
                                )
                            )
                        },
                        label = { Text("برنامج الزيتون") }
                    )
                    AssistChip(
                        onClick = {
                            addBatchReminders(
                                listOf(
                                    "رش مبيد فطري وقائي (اللفحة والبياض)" to 0,
                                    "تسميد متوازن + كالسيوم منعاً لعفن الطرف الزهري" to 7,
                                    "انتهاء فترة التحريم وفحص الثمار" to 14
                                )
                            )
                        },
                        label = { Text("برنامج الطماطم") }
                    )
                    AssistChip(
                        onClick = {
                            addBatchReminders(
                                listOf(
                                    "تسميد آزوتي (دفعة التفريع)" to 0,
                                    "مكافحة الأعشاب الضارة والصدأ" to 15
                                )
                            )
                        },
                        label = { Text("برنامج الحبوب") }
                    )
                }

                HorizontalDivider()

                Text("إنشاء تذكير جديد", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("نص التذكير (مثال: ري حقل الزيتون)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedButton(
                    onClick = { pickDateTime(context, whenMillis) { whenMillis = it } },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(formatter.format(Date(whenMillis)))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(REPEAT_NONE, REPEAT_DAILY, REPEAT_WEEKLY).forEach { option ->
                        FilterChip(
                            selected = repeat == option,
                            onClick = { repeat = option },
                            label = { Text(repeatLabel(option)) }
                        )
                    }
                }

                Button(
                    onClick = {
                        val reminder = Reminder(
                            id = (System.currentTimeMillis() % Int.MAX_VALUE).toInt(),
                            title = title.trim(),
                            timeMillis = whenMillis,
                            repeat = repeat
                        )
                        val updated = (reminders + reminder).sortedBy { it.timeMillis }
                        ReminderStore.save(context, updated)
                        ReminderScheduler.ensureChannel(context)
                        ReminderScheduler.schedule(context, reminder)
                        reminders = updated
                        title = ""
                    },
                    enabled = title.isNotBlank() && whenMillis > System.currentTimeMillis(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("إضافة تذكير")
                }

                HorizontalDivider()

                if (reminders.isEmpty()) {
                    Text(
                        text = "لا توجد تذكيرات بعد.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                if (reminders.isNotEmpty()) {
                    Text("مواعيدك القادمة", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                }
                reminders.forEach { r ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(start = 14.dp, end = 6.dp, top = 12.dp, bottom = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(12.dp), modifier = Modifier.size(42.dp)) {
                                androidx.compose.foundation.layout.Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Event, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(r.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                                Text(formatter.format(Date(r.timeMillis)), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(repeatLabel(r.repeat), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(onClick = {
                                ReminderScheduler.cancel(context, r.id)
                                val updated = reminders.filter { it.id != r.id }
                                ReminderStore.save(context, updated)
                                reminders = updated
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف التذكير", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    )
}
