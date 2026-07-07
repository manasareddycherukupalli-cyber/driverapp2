package com.company.carryon.data.audio

import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFAudio.AVAudioPlayer
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.setActive
import platform.Foundation.NSBundle
import platform.Foundation.NSURL

/**
 * Foreground job-request ringtone (iOS).
 *
 * Uses the `.playback` audio-session category so the ring sounds even when the
 * hardware mute switch is on (assertive posture). Loops until [stop].
 * The asset is bundled as `alert_sonar.caf` (falls back to `.mp3`).
 */
@OptIn(ExperimentalForeignApi::class)
actual object JobRingtonePlayer {
    private var player: AVAudioPlayer? = null

    actual fun start() {
        if (player != null) return
        val path = NSBundle.mainBundle.pathForResource("alert_sonar", "caf")
            ?: NSBundle.mainBundle.pathForResource("alert_sonar", "mp3")
            ?: return
        val url = NSURL.fileURLWithPath(path)
        runCatching {
            val session = AVAudioSession.sharedInstance()
            session.setCategory(AVAudioSessionCategoryPlayback, null)
            session.setActive(true, null)
            val p = AVAudioPlayer(contentsOfURL = url, error = null)
            p.numberOfLoops = -1  // loop indefinitely
            p.prepareToPlay()
            p.play()
            player = p
        }.onFailure { stop() }
    }

    actual fun stop() {
        player?.stop()
        player = null
        runCatching {
            AVAudioSession.sharedInstance().setActive(false, null)
        }
    }
}
