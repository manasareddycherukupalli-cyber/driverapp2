package com.company.carryon.presentation.navigation

import com.company.carryon.data.model.JobStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DeliveryResumeRoutingTest {
    @Test
    fun activeDeliveryStatusesResolveToSingleResumeScreen() {
        assertEquals(Screen.MapNavigation, mapJobStatusToResumeScreen(JobStatus.ACCEPTED))
        assertEquals(Screen.MapNavigation, mapJobStatusToResumeScreen(JobStatus.HEADING_TO_PICKUP))
        assertEquals(Screen.ActiveDelivery, mapJobStatusToResumeScreen(JobStatus.ARRIVED_AT_PICKUP))
        assertEquals(Screen.StartDelivery, mapJobStatusToResumeScreen(JobStatus.PICKED_UP))
        assertEquals(Screen.InTransitNavigation, mapJobStatusToResumeScreen(JobStatus.IN_TRANSIT))
        assertEquals(Screen.ArrivedAtDrop, mapJobStatusToResumeScreen(JobStatus.ARRIVED_AT_DROP))
    }

    @Test
    fun deliveredJobResolvesToCompletionInsteadOfInTransitLoop() {
        assertEquals(Screen.DeliveryComplete, mapJobStatusToResumeScreen(JobStatus.DELIVERED))
    }

    @Test
    fun nonDeliveryStatusesDoNotResumeDeliveryFlow() {
        assertNull(mapJobStatusToResumeScreen(JobStatus.PENDING))
        assertNull(mapJobStatusToResumeScreen(JobStatus.CANCELLED))
    }
}
