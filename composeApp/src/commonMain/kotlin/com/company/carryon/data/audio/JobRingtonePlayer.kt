package com.company.carryon.data.audio

/**
 * Plays the incoming job-request ringtone while an offer is on screen.
 *
 * Assertive posture (see feature spec): the ring is routed through an
 * alarm/ringtone audio usage so it is audible on vibrate and — on iOS —
 * ignores the hardware mute switch. Looping is driven by the presence of
 * incoming offers; [start] begins a loop and [stop] silences it.
 *
 * This is the FOREGROUND path only. Background alerts ride on the FCM
 * notification channel (Android) / APNs sound (iOS).
 */
expect object JobRingtonePlayer {
    /** Begin looping the ringtone. Idempotent — calling while already ringing is a no-op. */
    fun start()

    /** Stop the ringtone and release the underlying player. Idempotent. */
    fun stop()
}
