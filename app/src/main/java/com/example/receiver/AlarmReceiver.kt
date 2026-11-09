package com.example.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.alarm.ZangaAlarmScheduler
import com.example.data.local.ZangaDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

data class ActiveAlarmAlert(
    val activityId: Long,
    val title: String,
    val isExpired: Boolean,
    val minutesRemaining: Int
)

object AlarmEventBus {
    private val _alarmEvents = MutableSharedFlow<ActiveAlarmAlert>(extraBufferCapacity = 10)
    val alarmEvents = _alarmEvents.asSharedFlow()

    fun postAlarm(alert: ActiveAlarmAlert) {
        _alarmEvents.tryEmit(alert)
    }
}

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "zanga_activity_alarms"
        const val CHANNEL_NAME = "ZANGA Activity Alarms & Deadlines"

        const val ACTION_ALARM_WARNING = "com.example.zanga.ACTION_ALARM_WARNING"
        const val ACTION_ALARM_EXPIRED = "com.example.zanga.ACTION_ALARM_EXPIRED"
        const val ACTION_COMPLETE_ACTIVITY = "com.example.zanga.ACTION_COMPLETE_ACTIVITY"
        const val ACTION_EXTEND_ACTIVITY = "com.example.zanga.ACTION_EXTEND_ACTIVITY"
        const val ACTION_DISMISS_ALARM = "com.example.zanga.ACTION_DISMISS_ALARM"

        const val EXTRA_ACTIVITY_ID = "extra_activity_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_CATEGORY = "extra_category"
        const val EXTRA_MINUTES_REMAINING = "extra_minutes_remaining"
        const val EXTRA_EXTENSION_MINUTES = "extra_extension_minutes"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val activityId = intent.getLongExtra(EXTRA_ACTIVITY_ID, -1L)
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Scheduled Activity"

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(notificationManager)

        when (action) {
            ACTION_ALARM_EXPIRED -> {
                handleAlarmExpired(context, notificationManager, activityId, title)
            }
            ACTION_ALARM_WARNING -> {
                val minutesRemaining = intent.getIntExtra(EXTRA_MINUTES_REMAINING, 15)
                handleAlarmWarning(context, notificationManager, activityId, title, minutesRemaining)
            }
            ACTION_COMPLETE_ACTIVITY -> {
                handleCompleteActivity(context, notificationManager, activityId)
            }
            ACTION_EXTEND_ACTIVITY -> {
                val extensionMinutes = intent.getIntExtra(EXTRA_EXTENSION_MINUTES, 15)
                handleExtendActivity(context, notificationManager, activityId, extensionMinutes)
            }
            ACTION_DISMISS_ALARM -> {
                notificationManager.cancel(activityId.toInt())
            }
        }
    }

    private fun handleAlarmExpired(
        context: Context,
        notificationManager: NotificationManager,
        activityId: Long,
        title: String
    ) {
        triggerVibrationAndSound(context, isExpired = true)

        // Post in-app event
        AlarmEventBus.postAlarm(
            ActiveAlarmAlert(
                activityId = activityId,
                title = title,
                isExpired = true,
                minutesRemaining = 0
            )
        )

        val fullScreenIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_ACTIVITY_ID, activityId)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            activityId.toInt(),
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Mark as completed action
        val completeIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_COMPLETE_ACTIVITY
            putExtra(EXTRA_ACTIVITY_ID, activityId)
        }
        val completePendingIntent = PendingIntent.getBroadcast(
            context,
            (activityId * 10 + 5).toInt(),
            completeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Extend +15 min action
        val extendIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_EXTEND_ACTIVITY
            putExtra(EXTRA_ACTIVITY_ID, activityId)
            putExtra(EXTRA_EXTENSION_MINUTES, 15)
        }
        val extendPendingIntent = PendingIntent.getBroadcast(
            context,
            (activityId * 10 + 6).toInt(),
            extendIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Stop / Dismiss action
        val dismissIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_DISMISS_ALARM
            putExtra(EXTRA_ACTIVITY_ID, activityId)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            (activityId * 10 + 7).toInt(),
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val message = "TIME IS UP! Your allocated time for $title has expired."

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("ZANGA TIME ALERT")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(fullScreenPendingIntent)
            .setAutoCancel(true)
            .addAction(android.R.drawable.checkbox_on_background, "Complete", completePendingIntent)
            .addAction(android.R.drawable.ic_menu_rotate, "+15 Min", extendPendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", dismissPendingIntent)
            .build()

        notificationManager.notify(activityId.toInt(), notification)

        // Mark activity status as OVERDUE in DB if still pending
        CoroutineScope(Dispatchers.IO).launch {
            val db = ZangaDatabase.getDatabase(context)
            val activity = db.activityDao().getActivityByIdDirect(activityId)
            if (activity != null && activity.status == "PENDING") {
                db.activityDao().update(activity.copy(status = "OVERDUE"))
            }
        }
    }

    private fun handleAlarmWarning(
        context: Context,
        notificationManager: NotificationManager,
        activityId: Long,
        title: String,
        minutesRemaining: Int
    ) {
        triggerVibrationAndSound(context, isExpired = false)

        AlarmEventBus.postAlarm(
            ActiveAlarmAlert(
                activityId = activityId,
                title = title,
                isExpired = false,
                minutesRemaining = minutesRemaining
            )
        )

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            context,
            activityId.toInt(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val urgencyPrefix = when (minutesRemaining) {
            30 -> "Initial Warning: 30 minutes remaining"
            15 -> "Warning: 15 minutes remaining"
            5 -> "Urgent Warning: 5 minutes remaining"
            else -> "$minutesRemaining minutes remaining"
        }

        val message = "$urgencyPrefix for \"$title\""

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("ZANGA Countdown Warning")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(if (minutesRemaining <= 5) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(openPendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify((activityId + 1000).toInt(), notification)
    }

    private fun handleCompleteActivity(
        context: Context,
        notificationManager: NotificationManager,
        activityId: Long
    ) {
        notificationManager.cancel(activityId.toInt())
        ZangaAlarmScheduler.cancelActivityAlarms(context, activityId)

        CoroutineScope(Dispatchers.IO).launch {
            val db = ZangaDatabase.getDatabase(context)
            val activity = db.activityDao().getActivityByIdDirect(activityId)
            if (activity != null) {
                val now = System.currentTimeMillis()
                val actualDuration = ((now - activity.startTimeMillis) / 60000).coerceAtLeast(1).toInt()
                db.activityDao().update(
                    activity.copy(
                        status = "COMPLETED",
                        completedAtMillis = now,
                        actualDurationMinutes = actualDuration
                    )
                )
            }
        }
    }

    private fun handleExtendActivity(
        context: Context,
        notificationManager: NotificationManager,
        activityId: Long,
        extensionMinutes: Int
    ) {
        notificationManager.cancel(activityId.toInt())

        CoroutineScope(Dispatchers.IO).launch {
            val db = ZangaDatabase.getDatabase(context)
            val activity = db.activityDao().getActivityByIdDirect(activityId)
            if (activity != null) {
                val newEndTime = activity.endTimeMillis + (extensionMinutes * 60 * 1000L)
                val updatedActivity = activity.copy(
                    endTimeMillis = newEndTime,
                    allocatedDurationMinutes = activity.allocatedDurationMinutes + extensionMinutes,
                    timeExtendedMinutes = activity.timeExtendedMinutes + extensionMinutes,
                    status = "EXTENDED"
                )
                db.activityDao().update(updatedActivity)
                // Reschedule alarms for new end time
                ZangaAlarmScheduler.scheduleActivityAlarms(context, updatedActivity)
            }
        }
    }

    private fun triggerVibrationAndSound(context: Context, isExpired: Boolean) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager =
                    context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = if (isExpired) {
                    longArrayOf(0, 500, 200, 500, 200, 800)
                } else {
                    longArrayOf(0, 250, 150, 250)
                }
                vibrator.vibrate(VibrationEffect.createWaveform(timings, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(500)
            }

            val alertUri = RingtoneManager.getDefaultUri(
                if (isExpired) RingtoneManager.TYPE_ALARM else RingtoneManager.TYPE_NOTIFICATION
            )
            val ringtone = RingtoneManager.getRingtone(context, alertUri)
            ringtone?.play()
        } catch (_: Exception) {
            // Best effort vibration/sound
        }
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Intelligent alerts and countdown warnings for scheduled activities"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 800)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
}
