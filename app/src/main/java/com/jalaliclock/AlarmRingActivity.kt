package com.jalaliclock

import android.app.Activity
import android.graphics.Color
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import java.util.Calendar

class AlarmRingActivity : Activity() {
    private var player: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        )
        val c = Calendar.getInstance()
        val time = TextView(this).apply {
            text = Jalali.toPersianDigits(String.format("%02d:%02d", c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE)))
            textSize = 72f
            gravity = Gravity.CENTER
            setTextColor(Color.parseColor("#FF3B3B"))
        }
        val dismiss = Button(this).apply {
            text = "خاموش کردن"
            setOnClickListener { stopAndFinish() }
        }
        setContentView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.BLACK)
            addView(time)
            addView(dismiss)
        })
        player = MediaPlayer.create(this, R.raw.alarm)?.apply {
            isLooping = true
            setAudioStreamType(AudioManager.STREAM_ALARM)
            start()
        }
    }

    private fun stopAndFinish() {
        player?.release()
        player = null
        finish()
    }

    override fun onDestroy() {
        player?.release()
        player = null
        super.onDestroy()
    }
}
