package com.jalaliclock

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.IntentFilter
import android.database.ContentObserver
import android.os.BatteryManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.CallLog
import android.provider.Telephony
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import java.util.Calendar

class MainActivity : Activity() {
    private lateinit var clock: ClockView
    private lateinit var tvDate: TextView
    private lateinit var tvAlarm: TextView
    private lateinit var tvCounts: TextView
    private lateinit var tvBattery: TextView

    private val handler = Handler(Looper.getMainLooper())
    private var lastAnnouncedHour = -1

    private val observer = object : ContentObserver(handler) {
        override fun onChange(selfChange: Boolean) {
            refreshCounts()
        }
    }

    private val ticker = object : Runnable {
        override fun run() {
            val c = Calendar.getInstance()
            val h = c.get(Calendar.HOUR_OF_DAY)
            val m = c.get(Calendar.MINUTE)
            val s = c.get(Calendar.SECOND)
            val ctx = applicationContext

            clock.setTime(h, m, s)
            tvDate.text = Jalali.dateString(c)

            val next = AlarmStore.nextAlarm(this@MainActivity)
            tvAlarm.text = if (next != null) "آلارم: " + Jalali.toPersianDigits(next.label()) else ""

            tvBattery.visibility = if (AppPrefs.isOn(ctx, AppPrefs.SHOW_BATTERY)) View.VISIBLE else View.GONE
            tvBattery.text = "باتری: " + batteryText()
            tvCounts.visibility = if (AppPrefs.isOn(ctx, AppPrefs.SHOW_COUNTS)) View.VISIBLE else View.GONE

            if (AppPrefs.isOn(ctx, AppPrefs.TICK)) Sounds.playTick(ctx)

            if (m == 0 && s == 0 && lastAnnouncedHour != h) {
                lastAnnouncedHour = h
                if (AppPrefs.isOn(ctx, AppPrefs.CHIME)) Sounds.playChime(ctx)
            }
            if (s == 0) refreshCounts()

            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_main)

        clock = findViewById(R.id.clock)
        tvDate = findViewById(R.id.tvDate)
        tvAlarm = findViewById(R.id.tvAlarm)
        tvCounts = findViewById(R.id.tvCounts)
        tvBattery = findViewById(R.id.tvBattery)

        tvDate.setOnClickListener { startActivity(Intent(this, AlarmsActivity::class.java)) }
        tvAlarm.setOnClickListener { startActivity(Intent(this, AlarmsActivity::class.java)) }
        clock.setOnLongClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
            true
        }

        if (!NotifCounter.hasPermissions(this)) {
            requestPermissions(arrayOf(Manifest.permission.READ_CALL_LOG, Manifest.permission.READ_SMS), 1)
        }
    }

    override fun onStart() {
        super.onStart()
        contentResolver.registerContentObserver(CallLog.Calls.CONTENT_URI, true, observer)
        contentResolver.registerContentObserver(Telephony.Sms.CONTENT_URI, true, observer)
    }

    override fun onStop() {
        super.onStop()
        contentResolver.unregisterContentObserver(observer)
    }

    override fun onResume() {
        super.onResume()
        applyBrightness()
        handler.post(ticker)
        refreshCounts()
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(ticker)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        refreshCounts()
    }

    /** Always-on mode keeps the screen awake but dims it. */
    private fun applyBrightness() {
        val lp = window.attributes
        lp.screenBrightness = if (AppPrefs.isOn(this, AppPrefs.ALWAYS_ON)) 0.05f else -1f
        window.attributes = lp
    }

    private fun batteryText(): String {
        val intent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED)) ?: return "—"
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        if (level < 0 || scale <= 0) return "—"
        return Jalali.toPersianDigits("${level * 100 / scale}") + "٪"
    }

    private fun refreshCounts() {
        if (!NotifCounter.hasPermissions(this)) {
            tvCounts.text = "برای نمایش تماس و پیام، دسترسی بدهید"
            return
        }
        val app = applicationContext
        Thread {
            val missed = NotifCounter.missedCalls(app)
            val unread = NotifCounter.unreadSms(app)
            runOnUiThread {
                tvCounts.text = "تماس بی‌پاسخ: ${fmt(missed)}     پیام خوانده‌نشده: ${fmt(unread)}"
            }
        }.start()
    }

    private fun fmt(n: Int): String = if (n < 0) "—" else Jalali.toPersianDigits(n.toString())
}
