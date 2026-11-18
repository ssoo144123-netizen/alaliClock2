package com.jalaliclock

import android.content.Context

data class Alarm(val hour: Int, val minute: Int, var enabled: Boolean) {
    fun label(): String = String.format("%02d:%02d", hour, minute)
}

object AlarmStore {
    private const val PREFS = "alarms"
    private const val KEY = "list"

    fun load(ctx: Context): MutableList<Alarm> {
        val raw = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, "") ?: ""
        val out = mutableListOf<Alarm>()
        if (raw.isEmpty()) return out
        for (item in raw.split(";")) {
            val parts = item.split("|")
            if (parts.size != 2) continue
            val hm = parts[0].split(":")
            if (hm.size != 2) continue
            out += Alarm(hm[0].toInt(), hm[1].toInt(), parts[1] == "1")
        }
        return out
    }

    fun save(ctx: Context, list: List<Alarm>) {
        val raw = list.joinToString(";") { "${it.label()}|${if (it.enabled) 1 else 0}" }
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY, raw).apply()
    }

    fun nextAlarm(ctx: Context): Alarm? =
        load(ctx).filter { it.enabled }.minByOrNull { AlarmScheduler.nextTrigger(it.hour, it.minute) }
}
