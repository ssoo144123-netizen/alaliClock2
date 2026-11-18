package com.jalaliclock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        ctx.startActivity(
            Intent(ctx, AlarmRingActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
        AlarmScheduler.scheduleAll(ctx)
    }
}
