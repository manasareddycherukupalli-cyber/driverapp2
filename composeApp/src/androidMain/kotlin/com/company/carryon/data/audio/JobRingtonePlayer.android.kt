package com.company.carryon.data.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import com.company.carryon.R

private const val TAG = "JobRingtonePlayer"
private var appContext: Context? = null

/** Wire the application context once at startup (called from MainActivity). */
fun initJobRingtonePlayer(context: Context) {
    appContext = context.applicationContext
}

/**
 * Foreground job-request ringtone (Android).
 *
 * Routed through [AudioAttributes.USAGE_ALARM] so it is audible even when the
 * phone is on vibrate (assertive posture). Loops until [stop]. Also drives a
 * repeating vibration pattern.
 */
actual object JobRingtonePlayer {
    private var player: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    actual fun start() {
        val ctx = appContext ?: run {
            Log.w(TAG, "start() called before init; ignoring")
            return
        }
        if (player != null) return
        try {
            val afd = ctx.resources.openRawResourceFd(R.raw.alert_sonar) ?: return
            val mp = MediaPlayer()
            mp.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            mp.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            afd.close()
            mp.isLooping = true
            mp.setOnPreparedListener { it.start() }
            mp.prepareAsync()
            player = mp
            startVibration(ctx)
        } catch (e: Exception) {
            Log.w(TAG, "start failed", e)
            stop()
        }
    }

    actual fun stop() {
        player?.let { mp ->
            runCatching { if (mp.isPlaying) mp.stop() }
            mp.reset()
            mp.release()
        }
        player = null
        vibrator?.let { runCatching { it.cancel() } }
        vibrator = null
    }

    private fun startVibration(ctx: Context) {
        val vib = resolveVibrator(ctx) ?: return
        if (!vib.hasVibrator()) return
        // 0.5s on, 0.5s off, repeating from index 0.
        val timings = longArrayOf(0, 500, 500)
        val effect = VibrationEffect.createWaveform(timings, 0)
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        runCatching { vib.vibrate(effect, attrs) }
            .onSuccess { vibrator = vib }
            .onFailure { Log.w(TAG, "vibrate failed (missing VIBRATE permission?)", it) }
    }

    private fun resolveVibrator(ctx: Context): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = ctx.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            ctx.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }
}
