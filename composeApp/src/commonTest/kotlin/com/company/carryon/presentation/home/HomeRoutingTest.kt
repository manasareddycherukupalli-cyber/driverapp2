package com.company.carryon.presentation.home

import com.company.carryon.data.model.DeliveryJob
import com.company.carryon.data.model.Driver
import com.company.carryon.data.model.JobStatus
import com.company.carryon.data.model.VerificationStatus
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

    @Test
    fun payoutSetupOpensBankDetailsStepDirectly() {
        assertEquals(
            Screen.DriverOnboarding(initialStep = 10),
            payoutSetupDestination()
        )
    }

    @Test
    fun submittedBankDetailsDoNotShowPayoutPromptWhileApprovalIsPending() {
        val driver = Driver(
            verificationStatus = VerificationStatus.APPROVED,
            bankName = "Maybank",
            bankAccountHolder = "Test Driver",
            bankAccountNumber = "1234567890",
            bankDetailsStatus = "PENDING"
        )

        assertEquals(false, shouldShowPayoutInterstitial(driver, skipped = false))
    }

    @Test
    fun rejectedOrMissingBankDetailsStillShowPayoutPrompt() {
        val missing = Driver(verificationStatus = VerificationStatus.APPROVED)
        val rejected = missing.copy(
            bankName = "Maybank",
            bankAccountHolder = "Test Driver",
            bankAccountNumber = "1234567890",
            bankDetailsStatus = "REJECTED"
        )

        assertEquals(true, shouldShowPayoutInterstitial(missing, skipped = false))
        assertEquals(true, shouldShowPayoutInterstitial(rejected, skipped = false))
    }

    @Test
    fun submittedStripeDetailsDoNotShowPayoutPrompt() {
        val driver = Driver(
            verificationStatus = VerificationStatus.APPROVED,
            stripeDetailsSubmitted = true
        )

        assertEquals(false, shouldShowPayoutInterstitial(driver, skipped = false))
    }
}
