package com.company.carryon.data.network

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlin.time.Clock

sealed class DeepLinkEvent {
    data object OnboardingReturn : DeepLinkEvent()
    data object Refresh : DeepLinkEvent()
    data class PayoutUpdate(
        val notificationType: String? = null,
        val payoutId: String? = null,
        val transactionId: String? = null
    ) : DeepLinkEvent()
}

object DeepLinkBus {
    val events = MutableSharedFlow<DeepLinkEvent>(extraBufferCapacity = 16)

    fun emitOnboardingReturn() {
        events.tryEmit(DeepLinkEvent.OnboardingReturn)
    }

    fun emitRefresh() {
        events.tryEmit(DeepLinkEvent.Refresh)
    }

    fun emitPayoutUpdate(notificationType: String?, payoutId: String?, transactionId: String?) {
        events.tryEmit(
            DeepLinkEvent.PayoutUpdate(
                notificationType = notificationType,
                payoutId = payoutId,
                transactionId = transactionId
            )
        )
    }
}

object PayoutRefreshGuard {
    private const val WINDOW_MS = 60_000L
    private const val MAX_REFRESHES = 2
    private val refreshEvents = mutableListOf<Long>()

    fun reset() {
        refreshEvents.clear()
    }

    fun tryRecord(nowMillis: Long = Clock.System.now().toEpochMilliseconds()): Boolean {
        refreshEvents.removeAll { nowMillis - it > WINDOW_MS }
        if (refreshEvents.size >= MAX_REFRESHES) return false
        refreshEvents.add(nowMillis)
        return true
    }
}

internal fun emitStripeConnectDeepLink(uri: String) {
    val normalized = uri.substringBefore("?").substringBefore("#").trimEnd('/')
    when (normalized) {
        "carryon-driver://stripe-connect/return" -> DeepLinkBus.emitOnboardingReturn()
        "carryon-driver://stripe-connect/refresh" -> DeepLinkBus.emitRefresh()
    }
}
