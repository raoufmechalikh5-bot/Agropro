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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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

    /** يعيد أقرب موعد قادم للتذكير، أو null إذا كان تذكيراً منفرداً انتهى وقته. */
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
            val builder = NotificationCompat.Builder(context, ReminderScheduler.CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_popup_reminder)
                .setContentTitle("Agro pro")
                .setContentText(reminder.title)
                .setStyle(NotificationCompat.BigTextStyle().bigText(reminder.title))
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

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("إغلاق") }
        },
        title = { Text("التذكيرات") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
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

                reminders.forEach { r ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = r.title,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = formatter.format(Date(r.timeMillis)) + " • " + repeatLabel(r.repeat),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        IconButton(onClick = {
                            ReminderScheduler.cancel(context, r.id)
                            val updated = reminders.filter { it.id != r.id }
                            ReminderStore.save(context, updated)
                            reminders = updated
                        }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "حذف التذكير"
                            )
                        }
                    }
                }
            }
        }
    )
}
