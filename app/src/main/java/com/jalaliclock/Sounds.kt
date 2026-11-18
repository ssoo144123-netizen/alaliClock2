package com.jalaliclock

import android.content.Context
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/** Generated tick and chime sounds (no tick clip exists in the original APK). */
object Sounds {
    private const val RATE = 22050

    private val tickData: ShortArray by lazy { generate(0.03, 2000.0, 250.0) }
    private val chimeData: ShortArray by lazy { generate(1.2, 880.0, 3.0) }

    fun busy(ctx: Context): Boolean {
        val am = ctx.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return am.mode == AudioManager.MODE_IN_CALL || am.mode == AudioManager.MODE_RINGTONE
    }

    fun playTick(ctx: Context) {
        if (!busy(ctx)) play(tickData)
    }

    fun playChime(ctx: Context) {
        if (!busy(ctx)) play(chimeData)
    }

    private fun generate(seconds: Double, freq: Double, decay: Double): ShortArray {
        val n = (RATE * seconds).toInt()
        return ShortArray(n) { i ->
            val t = i.toDouble() / RATE
            (sin(2 * PI * freq * t) * exp(-t * decay) * Short.MAX_VALUE * 0.6).toInt().toShort()
        }
    }

    private fun play(data: ShortArray) {
        Thread {
            val track = AudioTrack(
                AudioManager.STREAM_MUSIC, RATE, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, data.size * 2, AudioTrack.MODE_STATIC
            )
            track.write(data, 0, data.size)
            track.play()
            Thread.sleep(data.size * 1000L / RATE + 100)
            track.release()
        }.start()
    }
}
