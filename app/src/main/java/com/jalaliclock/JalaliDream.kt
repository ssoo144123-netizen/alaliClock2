package com.jalaliclock

import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.service.dreams.DreamService
import android.view.ViewGroup
import android.widget.FrameLayout
import java.util.Calendar

/** Screensaver: the same analog clock, full screen. */
class JalaliDream : DreamService() {
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var clock: ClockView

    private val ticker = object : Runnable {
        override fun run() {
            val c = Calendar.getInstance()
            clock.setTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), c.get(Calendar.SECOND))
            handler.postDelayed(this, 1000)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isFullscreen = true
        isInteractive = false
        clock = ClockView(this)
        setContentView(FrameLayout(this).apply {
            setBackgroundColor(Color.BLACK)
            addView(clock, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        })
    }

    override fun onDreamingStarted() {
        super.onDreamingStarted()
        handler.post(ticker)
    }

    override fun onDetachedFromWindow() {
        handler.removeCallbacks(ticker)
        super.onDetachedFromWindow()
    }
}
