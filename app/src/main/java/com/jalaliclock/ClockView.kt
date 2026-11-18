package com.jalaliclock

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/** Red analog dial with Persian numerals, hands, digital time and a blue seconds trail. */
class ClockView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val red = Color.parseColor("#FF3B3B")
    private val dimRed = Color.parseColor("#3A1414")
    private val blue = Color.parseColor("#2F5BFF")
    private val teal = Color.parseColor("#1E8A8A")
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = red
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }

    private var hour = 0
    private var minute = 0
    private var second = 0

    fun setTime(h: Int, m: Int, s: Int) {
        hour = h; minute = m; second = s
        invalidate()
    }

    /** Angle in degrees, 0 = 12 o'clock, clockwise. Returns screen point. */
    private fun polar(cx: Float, cy: Float, rad: Float, deg: Double): Pair<Float, Float> {
        val a = Math.toRadians(deg - 90.0)
        return Pair(cx + rad * cos(a).toFloat(), cy + rad * sin(a).toFloat())
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = width / 2f
        val cy = height / 2f
        val r = min(width, height) * 0.40f

        // Dot ring
        paint.style = Paint.Style.FILL
        for (i in 0 until 60) {
            val (x, y) = polar(cx, cy, r, i * 6.0)
            paint.color = dimRed
            canvas.drawCircle(x, y, if (i % 5 == 0) r * 0.03f else r * 0.018f, paint)
        }

        // Hour ticks
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.color = red
        paint.strokeWidth = r * 0.03f
        for (i in 0 until 12) {
            val (x1, y1) = polar(cx, cy, r * 0.80f, i * 30.0)
            val (x2, y2) = polar(cx, cy, r * 0.92f, i * 30.0)
            canvas.drawLine(x1, y1, x2, y2, paint)
        }

        // Persian numerals
        textPaint.textSize = r * 0.14f
        for (i in 1..12) {
            val (x, y) = polar(cx, cy, r * 0.66f, i * 30.0)
            canvas.drawText(Jalali.toPersianDigits(i.toString()), x, y + textPaint.textSize * 0.35f, textPaint)
        }

        // Hands
        val hourDeg = (hour % 12 + minute / 60.0) * 30.0
        val minDeg = (minute + second / 60.0) * 6.0
        paint.color = red
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = r * 0.05f
        val (hx, hy) = polar(cx, cy, r * 0.45f, hourDeg)
        canvas.drawLine(cx, cy, hx, hy, paint)
        paint.strokeWidth = r * 0.025f
        val (mx, my) = polar(cx, cy, r * 0.62f, minDeg)
        canvas.drawLine(cx, cy, mx, my, paint)
        paint.style = Paint.Style.FILL
        canvas.drawCircle(cx, cy, r * 0.05f, paint)

        // Digital time
        textPaint.textSize = r * 0.16f
        canvas.drawText(
            Jalali.toPersianDigits(String.format("%02d:%02d", hour, minute)),
            cx, cy + r * 0.45f, textPaint
        )

        // Blue seconds trail along the ring
        paint.style = Paint.Style.FILL
        for (k in 0 until 12) {
            val sec = (second - k + 60) % 60
            val (x, y) = polar(cx, cy, r, sec * 6.0)
            paint.color = if (k == 0) blue else teal
            paint.alpha = (255 * (1f - k / 12f)).toInt()
            canvas.drawCircle(x, y, r * (0.03f - k * 0.001f), paint)
        }
        paint.alpha = 255
    }
}
