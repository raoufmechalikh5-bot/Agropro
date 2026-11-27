package com.example.ui.live

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity

/**
 * Keeps a lightweight, ongoing status notification while the AgroPro Live screen is active.
 * Android/realme decides whether this notification is eligible for Mini Capsule.
 */
internal object AgroLiveNotification {
    private const val CHANNEL_ID = "agro_live_status"
    private const val NOTIFICATION_ID = 7301

    fun show(context: Context, state: AgroLiveUiState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "AgroPro Live",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "حالة جلسة المساعد الزراعي المباشر"
                    setShowBadge(false)
                }
            )
        }

        val status = when {
            state.connecting -> "جارٍ الاتصال بالمساعد..."
            state.connected && state.microphoneEnabled && state.cameraEnabled -> "يستمع إليك • الكاميرا تعمل"
            state.connected && state.microphoneEnabled -> "يستمع إليك • الكاميرا متوقفة"
            state.connected && state.cameraEnabled -> "المحادثة نشطة • الميكروفون متوقف"
            state.connected -> "المحادثة نشطة • الصوت والكاميرا متوقفان"
            else -> "المحادثة غير نشطة"
        }

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentTitle("AgroPro Live")
            .setContentText(status)
            .setStyle(NotificationCompat.BigTextStyle().bigText(status))
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setAutoCancel(false)
            .build()

        runCatching { NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification) }
    }

    fun dismiss(context: Context) {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
    }
}
