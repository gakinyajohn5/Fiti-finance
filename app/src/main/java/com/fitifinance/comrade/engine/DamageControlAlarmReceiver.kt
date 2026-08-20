package com.fitifinance.comrade.engine

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.fitifinance.comrade.FitiApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

private const val CHANNEL_ID = "damage_control"
private const val NOTIFICATION_ID = 1001

/**
 * Morning-After "Damage Control" Summary: fires at 9:00 AM the day after a
 * night-out, presenting total spent, amounts owed back to the user from
 * split bills, and adjusted food caps for the rest of the week.
 */
class DamageControlAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as? FitiApplication ?: return
        val repo = app.repository

        CoroutineScope(Dispatchers.IO).launch {
            val today = LocalDate.now(ZoneId.systemDefault())
            val yesterdayStart = today.minusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val todayStart = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

            val summary = repo.buildDamageControlSummary(yesterdayStart, todayStart)
            postNotification(context, summary)
        }
    }

    private fun postNotification(context: Context, summary: DamageControlSummary) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Damage Control", NotificationManager.IMPORTANCE_DEFAULT)
            manager.createNotificationChannel(channel)
        }

        val text = "Spent KES ${summary.totalSpentKes} last night. " +
            "Owed back: KES ${summary.pendingReceivablesKes}. " +
            "This week's daily food cap adjusted to KES ${summary.adjustedDailyFoodCapKes}."

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Morning-After Damage Control")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setAutoCancel(true)
            .build()

        manager.notify(NOTIFICATION_ID, notification)
    }
}

data class DamageControlSummary(
    val totalSpentKes: Double,
    val pendingReceivablesKes: Double,
    val adjustedDailyFoodCapKes: Double
)
