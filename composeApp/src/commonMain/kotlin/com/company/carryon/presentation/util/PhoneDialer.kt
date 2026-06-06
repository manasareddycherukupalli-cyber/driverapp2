package com.company.carryon.presentation.util

import com.company.carryon.data.model.DeliveryJob
import com.company.carryon.data.model.JobStatus

fun telUriFor(rawPhone: String?): String? {
    val raw = rawPhone?.trim().orEmpty()
    if (raw.isBlank()) return null

    val normalized = buildString {
        raw.forEachIndexed { index, char ->
            when {
                char.isDigit() -> append(char)
                char == '+' && index == 0 -> append(char)
            }
        }
    }
    val digitCount = normalized.count { it.isDigit() }
    if (digitCount < 7) return null
    return "tel:$normalized"
}

fun DeliveryJob.senderPhoneForPickup(): String? =
    firstDialablePhone(pickup.contactPhone, customerPhone)

fun DeliveryJob.recipientPhoneForDropoff(): String? =
    firstDialablePhone(dropoff.contactPhone)

fun DeliveryJob.contactPhoneForCurrentDestination(): String? =
    if (status.ordinal <= JobStatus.ARRIVED_AT_PICKUP.ordinal) {
        senderPhoneForPickup()
    } else {
        recipientPhoneForDropoff()
    }

private fun firstDialablePhone(vararg phones: String?): String? =
    phones.firstOrNull { telUriFor(it) != null }?.trim()
