package com.jalaliclock

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

object AlarmScheduler {
    private const val MAX_ALARMS = 50

    fun nextTrigger(hour: Int, minute: Int): Long {
        val now = System.currentTimeMillis()
        val c = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (c.timeInMillis <= now) c.add(Calendar.DAY_OF_MONTH, 1)
        return c.timeInMillis
    }

    private fun pending(ctx: Context, index: Int): PendingIntent = PendingIntent.getBroadcast(
        ctx, index, Intent(ctx, AlarmReceiver::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    fun scheduleAll(ctx: Context) {
        val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        for (i in 0 until MAX_ALARMS) am.cancel(pending(ctx, i))
        AlarmStore.load(ctx).forEachIndexed { i, a ->
            if (a.enabled && i < MAX_ALARMS) {
                // setExactAndAllowWhileIdle exists since API 23 (Android 6)
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextTrigger(a.hour, a.minute), pending(ctx, i))
            }
        }
    }
}
