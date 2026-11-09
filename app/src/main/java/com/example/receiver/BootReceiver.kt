package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.alarm.ZangaAlarmScheduler
import com.example.data.local.ZangaDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "ZangaBootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            Log.d(TAG, "Device rebooted or package replaced. Restoring ZANGA alarms...")
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = ZangaDatabase.getDatabase(context)
                    val now = System.currentTimeMillis()
                    val upcomingActivities = db.activityDao().getUpcomingPendingActivities(now)
                    for (activity in upcomingActivities) {
                        ZangaAlarmScheduler.scheduleActivityAlarms(context, activity)
                    }
                    Log.d(TAG, "Successfully restored alarms for ${upcomingActivities.size} upcoming activities.")
                } catch (e: Exception) {
                    Log.e(TAG, "Error restoring alarms: ${e.message}")
                }
            }
        }
    }
}
