package com.company.carryon.data.repository

import com.company.carryon.data.model.DeliveryJob
import com.company.carryon.presentation.util.telUriFor

fun DeliveryJob.withNormalizedContactPhones(): DeliveryJob {
    val senderPhone = firstDialablePhone(pickup.contactPhone, customerPhone)
    val recipientPhone = firstDialablePhone(dropoff.contactPhone)

    return copy(
        customerPhone = customerPhone.ifBlank { senderPhone.orEmpty() },
        pickup = pickup.copy(contactPhone = pickup.contactPhone?.ifBlank { senderPhone.orEmpty() } ?: senderPhone),
        dropoff = dropoff.copy(contactPhone = dropoff.contactPhone?.ifBlank { recipientPhone.orEmpty() } ?: recipientPhone)
    )
}

private fun firstDialablePhone(vararg phones: String?): String? =
    phones.firstOrNull { telUriFor(it) != null }?.trim()
