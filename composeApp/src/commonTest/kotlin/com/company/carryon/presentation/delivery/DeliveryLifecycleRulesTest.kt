package com.company.carryon.presentation.delivery

import com.company.carryon.data.model.DeliveryLifecycleCommand
import com.company.carryon.data.model.JobStatus
import com.company.carryon.presentation.navigation.Screen
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DeliveryLifecycleRulesTest {
    @Test
    fun cancelBeforePickupIsAvailableUntilPickupOtpIsVerified() {
        assertTrue(canRunDeliveryCommand(DeliveryLifecycleCommand.CANCEL_BEFORE_PICKUP, JobStatus.ACCEPTED))
        assertTrue(canRunDeliveryCommand(DeliveryLifecycleCommand.CANCEL_BEFORE_PICKUP, JobStatus.HEADING_TO_PICKUP))
        assertTrue(canRunDeliveryCommand(DeliveryLifecycleCommand.CANCEL_BEFORE_PICKUP, JobStatus.ARRIVED_AT_PICKUP))
    }

    @Test
    fun cancelBeforePickupIsUnavailableAfterPickupOtpIsVerified() {
        assertFalse(canRunDeliveryCommand(DeliveryLifecycleCommand.CANCEL_BEFORE_PICKUP, JobStatus.PICKED_UP))
        assertFalse(canRunDeliveryCommand(DeliveryLifecycleCommand.CANCEL_BEFORE_PICKUP, JobStatus.IN_TRANSIT))
        assertFalse(canRunDeliveryCommand(DeliveryLifecycleCommand.CANCEL_BEFORE_PICKUP, JobStatus.ARRIVED_AT_DROP))
        assertFalse(canRunDeliveryCommand(DeliveryLifecycleCommand.CANCEL_BEFORE_PICKUP, JobStatus.DELIVERED))
        assertFalse(canRunDeliveryCommand(DeliveryLifecycleCommand.CANCEL_BEFORE_PICKUP, JobStatus.CANCELLED))
    }

    @Test
    fun arrivedJobsCannotRemainOnMapNavigation() {
        assertFalse(Screen.MapNavigation in validDeliveryScreensForStatus(JobStatus.ARRIVED_AT_PICKUP))
        assertTrue(Screen.ActiveDelivery in validDeliveryScreensForStatus(JobStatus.ARRIVED_AT_PICKUP))
    }
}
