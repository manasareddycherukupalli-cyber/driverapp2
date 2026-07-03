package com.company.carryon.data.model

sealed class OnlineGateBlocker(open val message: String) {
    data class AdminApprovalRequired(
        override val message: String = "Admin approval is required before you can go online."
    ) : OnlineGateBlocker(message)

    data class DocumentMissing(
        val type: String,
        override val message: String
    ) : OnlineGateBlocker(message)

    data class DocumentExpired(
        val type: String,
        override val message: String
    ) : OnlineGateBlocker(message)

    data class StripePayoutsDisabled(
        val requiresSetup: Boolean,
        override val message: String = "Set up Stripe payouts before going online."
    ) : OnlineGateBlocker(message)
}

class OnlineGateException(
    val blockers: List<OnlineGateBlocker>,
    val payoutRequirements: PayoutRequirements?,
    message: String
) : Exception(message)
