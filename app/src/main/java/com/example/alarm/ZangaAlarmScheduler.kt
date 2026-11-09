package com.example.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.entity.ActivityEntity
import com.example.receiver.AlarmReceiver

object ZangaAlarmScheduler {
    private const val TAG = "ZangaAlarmScheduler"

    const val WARNING_30_MIN = 30
    const val WARNING_15_MIN = 15
    const val WARNING_5_MIN = 5
    const val WARNING_0_EXPIRED = 0

    fun scheduleActivityAlarms(context: Context, activity: ActivityEntity) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val now = System.currentTimeMillis()

        if (activity.status == "COMPLETED" || activity.status == "CANCELLED") {
            cancelActivityAlarms(context, activity.id)
            return
        }

        // Cancel existing first to prevent duplicates
        cancelActivityAlarms(context, activity.id)

        // Android 12+ check
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.w(TAG, "Exact alarm permission not granted yet. Using best effort.")
            }
        }

        val endTime = activity.endTimeMillis

        // 30 minutes warning
        if (activity.isWarning30mEnabled) {
            val triggerTime = endTime - (WARNING_30_MIN * 60 * 1000L)
            if (triggerTime > now) {
                scheduleSingleAlarm(context, alarmManager, activity, triggerTime, WARNING_30_MIN)
            }
        }

        // 15 minutes warning
        if (activity.isWarning15mEnabled) {
            val triggerTime = endTime - (WARNING_15_MIN * 60 * 1000L)
            if (triggerTime > now) {
                scheduleSingleAlarm(context, alarmManager, activity, triggerTime, WARNING_15_MIN)
            }
        }

        // 5 minutes warning
        if (activity.isWarning5mEnabled) {
            val triggerTime = endTime - (WARNING_5_MIN * 60 * 1000L)
            if (triggerTime > now) {
                scheduleSingleAlarm(context, alarmManager, activity, triggerTime, WARNING_5_MIN)
            }
        }

        // 0 minutes - Deadline Alarm
        if (activity.isDeadlineAlarmEnabled) {
            if (endTime > now) {
                scheduleSingleAlarm(context, alarmManager, activity, endTime, WARNING_0_EXPIRED)
            }
        }
    }

    private fun scheduleSingleAlarm(
        context: Context,
        alarmManager: AlarmManager,
        activity: ActivityEntity,
        triggerAtMillis: Long,
        minutesRemaining: Int
    ) {
        val action = if (minutesRemaining == 0) {
            AlarmReceiver.ACTION_ALARM_EXPIRED
        } else {
            AlarmReceiver.ACTION_ALARM_WARNING
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            this.action = action
            putExtra(AlarmReceiver.EXTRA_ACTIVITY_ID, activity.id)
            putExtra(AlarmReceiver.EXTRA_TITLE, activity.title)
            putExtra(AlarmReceiver.EXTRA_MINUTES_REMAINING, minutesRemaining)
            putExtra(AlarmReceiver.EXTRA_CATEGORY, activity.category)
        }

        val requestCode = getRequestCode(activity.id, minutesRemaining)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
            Log.d(TAG, "Scheduled alarm for ${activity.title} in $minutesRemaining min at $triggerAtMillis")
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException scheduling exact alarm: ${e.message}")
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule alarm: ${e.message}")
        }
    }

    fun cancelActivityAlarms(context: Context, activityId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val warnings = listOf(WARNING_30_MIN, WARNING_15_MIN, WARNING_5_MIN, WARNING_0_EXPIRED)

        for (minutes in warnings) {
            val requestCode = getRequestCode(activityId, minutes)
            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }
    }

    private fun getRequestCode(activityId: Long, minutesRemaining: Int): Int {
        val suffix = when (minutesRemaining) {
            30 -> 1
            15 -> 2
            5 -> 3
            0 -> 4
            else -> 9
        }
        return ((activityId % 100000).toInt() * 10) + suffix
    }
}
