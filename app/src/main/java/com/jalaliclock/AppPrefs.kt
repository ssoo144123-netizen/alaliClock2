package com.jalaliclock

import android.content.Context

object AppPrefs {
    const val TICK = "tick"
    const val CHIME = "chime"
    const val ALWAYS_ON = "always_on"
    const val SHOW_BATTERY = "show_battery"
    const val SHOW_COUNTS = "show_counts"

    private const val PREFS = "app_prefs"
    private val DEFAULTS = mapOf(
        TICK to false,
        CHIME to true,
        ALWAYS_ON to false,
        SHOW_BATTERY to true,
        SHOW_COUNTS to true
    )

    private fun prefs(ctx: Context) = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun isOn(ctx: Context, key: String): Boolean = prefs(ctx).getBoolean(key, DEFAULTS[key] ?: false)

    fun set(ctx: Context, key: String, value: Boolean) {
        prefs(ctx).edit().putBoolean(key, value).apply()
    }
}
