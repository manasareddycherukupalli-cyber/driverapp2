package com.company.carryon.presentation.home

import com.company.carryon.data.model.DeliveryJob
import com.company.carryon.data.model.JobStatus
import com.company.carryon.presentation.navigation.Screen
import kotlin.test.Test
import kotlin.test.assertEquals

class HomeRoutingTest {
    @Test
    fun acceptedIncomingJobEntersDeliveryFlowNavigation() {
        val job = DeliveryJob(id = "job-1", status = JobStatus.ACCEPTED)

        assertEquals(Screen.MapNavigation, acceptedJobDeliveryEntryScreen(job))
    }

    @Test
    fun unknownAcceptedJobStatusFallsBackToDeliveryMap() {
        val job = DeliveryJob(id = "job-1", status = JobStatus.PENDING)

        assertEquals(Screen.MapNavigation, acceptedJobDeliveryEntryScreen(job))
    }
}
