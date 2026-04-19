package com.company.carryon.presentation.navigation

import com.company.carryon.data.model.JobStatus
import com.company.carryon.data.network.clearDeliveryResumeState
import com.company.carryon.data.network.getDeliveryResumeJobId
import com.company.carryon.data.network.getDeliveryResumeScreenKey
import com.company.carryon.data.network.saveDeliveryResumeState
import androidx.compose.runtime.*

// ============================================================
// SCREEN DEFINITIONS — All screens in the app
// ============================================================

sealed interface Screen {
    // ---- Splash ----
    data object Splash : Screen

    // ---- Auth Flow ----
    data object Onboarding : Screen
    data object Login : Screen
    data object OtpVerification : Screen
    data object Registration : Screen
    data object DriverOnboarding : Screen
    data object PersonalIdentity : Screen
    data object DocumentUpload : Screen
    data object VehicleDetailsInput : Screen
    data object ReadyToDrive : Screen
    data object VerificationStatus : Screen
    data object LocationPermission : Screen

    // ---- Main Tabs (Bottom Nav) ----
    data object Home : Screen
    data object Jobs : Screen
    data object Earnings : Screen
    data object Profile : Screen

    // ---- Sub Screens ----
    data object JobDetails : Screen
    data object ActiveDelivery : Screen
    data object PickupInstructions : Screen
    data object StartDelivery : Screen
    data object InTransitNavigation : Screen
    data object ArrivedAtDrop : Screen
    data object ProofOfDelivery : Screen
    data object DeliveryComplete : Screen
    data object JobReceipt : Screen
    data object MapNavigation : Screen
    data object Wallet : Screen
    data object TransactionHistory : Screen
    data object Ratings : Screen
    data object EditProfile : Screen
    data object Settings : Screen
    data object NotificationPreferences : Screen
    data object Language : Screen
    data object VehicleInfo : Screen
    data object DocumentsHub : Screen
    data object TermsOfService : Screen
    data object PrivacyPolicy : Screen
    data object HelpCenter : Screen
    data object HelpGettingStarted : Screen
    data object HelpPayments : Screen
    data object HelpSafety : Screen
    data object HelpAppIssues : Screen
    data object HelpContactSupport : Screen
    data object HelpDriverHandbook : Screen
    data object HelpHandbookAccountCompliance : Screen
    data object HelpHandbookDeliveryWorkflow : Screen
    data object HelpHandbookEarningsPayouts : Screen
    data object HelpHandbookSafetyIncident : Screen
    data object RaiseTicket : Screen
    data object SupportChat : Screen
    data object CustomerChat : Screen
    data object Sos : Screen
    data object Notifications : Screen
}

/** Screens that show the bottom navigation bar */
val mainTabScreens = setOf(Screen.Home, Screen.Jobs, Screen.Earnings, Screen.Profile)

private val resumableDeliveryScreens = setOf(
    Screen.JobDetails,
    Screen.MapNavigation,
    Screen.ActiveDelivery,
    Screen.PickupInstructions,
    Screen.StartDelivery,
    Screen.InTransitNavigation,
    Screen.ArrivedAtDrop,
    Screen.ProofOfDelivery,
    Screen.DeliveryComplete
)

// ============================================================
// APP NAVIGATOR — Manages navigation state & back stack
// ============================================================

@Stable
class AppNavigator {
    /** Current visible screen */
    var currentScreen by mutableStateOf<Screen>(Screen.Splash)
        private set

    /** Back stack for navigation history */
    private val backStack = mutableListOf<Screen>()

    // ---- Argument storage (shared between screens) ----
    private var selectedJobIdState by mutableStateOf<String?>(null)
    var selectedJobId: String?
        get() = selectedJobIdState
        set(value) {
            selectedJobIdState = value
            syncDeliveryResumeState()
        }
    var selectedTicketId by mutableStateOf<String?>(null)
    var selectedChatBookingId by mutableStateOf<String?>(null)
    var selectedChatCustomerName by mutableStateOf("")
    var phoneNumber by mutableStateOf("")
    var countryCode by mutableStateOf("+60")
    var initialJobsTabIndex by mutableIntStateOf(0)

    /**
     * Navigate to a new screen, pushing current screen to back stack.
     */
    fun navigateTo(screen: Screen) {
        backStack.add(currentScreen)
        currentScreen = screen
        syncDeliveryResumeState()
    }

    /**
     * Navigate to a screen and clear the entire back stack.
     * Used for navigation resets (e.g., after login → home).
     */
    fun navigateAndClearStack(screen: Screen) {
        backStack.clear()
        currentScreen = screen
        syncDeliveryResumeState()
    }

    /**
     * Navigate back to the previous screen.
     * @return true if back navigation was performed, false if at root.
     */
    fun goBack(): Boolean {
        return if (backStack.isNotEmpty()) {
            currentScreen = backStack.removeLast()
            syncDeliveryResumeState()
            true
        } else false
    }

    /**
     * Switch to a main tab (Home, Jobs, Earnings, Profile).
     * Clears the back stack to prevent deep nesting from tabs.
     */
    fun switchTab(screen: Screen) {
        if (screen in mainTabScreens && screen != currentScreen) {
            backStack.clear()
            backStack.add(Screen.Home) // Always allow going back to Home
            currentScreen = screen
            syncDeliveryResumeState()
        }
    }

    /**
     * Switch to Jobs tab and preselect a tab index (0=Active,1=Scheduled,2=Completed).
     */
    fun switchToJobsTab(tabIndex: Int = 0) {
        initialJobsTabIndex = tabIndex.coerceIn(0, 2)
        if (currentScreen != Screen.Jobs) {
            backStack.clear()
            backStack.add(Screen.Home)
            currentScreen = Screen.Jobs
            syncDeliveryResumeState()
        }
    }

    fun openCustomerChat(bookingId: String, customerName: String) {
        selectedChatBookingId = bookingId
        selectedChatCustomerName = customerName
        navigateTo(Screen.CustomerChat)
    }

    fun restorePersistedDeliveryState(): Boolean {
        val screen = getDeliveryResumeScreenKey()?.toScreenOrNull()
        val jobId = getDeliveryResumeJobId()?.takeIf { it.isNotBlank() }
        if (screen == null || jobId == null) {
            clearDeliveryResumeState()
            return false
        }

        selectedJobId = jobId
        backStack.clear()
        currentScreen = screen
        syncDeliveryResumeState()
        return true
    }

    fun resumeFromJobStatus(jobId: String, status: JobStatus): Boolean {
        val screen = mapJobStatusToResumeScreen(status) ?: return false
        selectedJobId = jobId
        navigateAndClearStack(screen)
        return true
    }

    fun clearPersistedDeliveryState() {
        clearDeliveryResumeState()
    }

    private fun syncDeliveryResumeState() {
        val jobId = selectedJobId?.takeIf { it.isNotBlank() }
        if (currentScreen in resumableDeliveryScreens && jobId != null) {
            saveDeliveryResumeState(currentScreen.toStorageKey(), jobId)
        }
    }

    /** Check if back navigation is possible */
    val canGoBack: Boolean get() = backStack.isNotEmpty()
}

fun mapJobStatusToResumeScreen(status: JobStatus): Screen? = when (status) {
    JobStatus.ACCEPTED, JobStatus.HEADING_TO_PICKUP -> Screen.MapNavigation
    JobStatus.ARRIVED_AT_PICKUP -> Screen.ActiveDelivery
    JobStatus.PICKED_UP, JobStatus.IN_TRANSIT -> Screen.InTransitNavigation
    JobStatus.ARRIVED_AT_DROP -> Screen.ArrivedAtDrop
    else -> null
}

fun resumePriorityForStatus(status: JobStatus): Int = when (status) {
    JobStatus.ARRIVED_AT_DROP -> 6
    JobStatus.IN_TRANSIT -> 5
    JobStatus.PICKED_UP -> 4
    JobStatus.ARRIVED_AT_PICKUP -> 3
    JobStatus.HEADING_TO_PICKUP -> 2
    JobStatus.ACCEPTED -> 1
    else -> 0
}

private fun Screen.toStorageKey(): String = when (this) {
    Screen.JobDetails -> "job_details"
    Screen.MapNavigation -> "map_navigation"
    Screen.ActiveDelivery -> "active_delivery"
    Screen.PickupInstructions -> "pickup_instructions"
    Screen.StartDelivery -> "start_delivery"
    Screen.InTransitNavigation -> "in_transit_navigation"
    Screen.ArrivedAtDrop -> "arrived_at_drop"
    Screen.ProofOfDelivery -> "proof_of_delivery"
    Screen.DeliveryComplete -> "delivery_complete"
    else -> ""
}

private fun String.toScreenOrNull(): Screen? = when (this) {
    "job_details" -> Screen.JobDetails
    "map_navigation" -> Screen.MapNavigation
    "active_delivery" -> Screen.ActiveDelivery
    "pickup_instructions" -> Screen.PickupInstructions
    "start_delivery" -> Screen.StartDelivery
    "in_transit_navigation" -> Screen.InTransitNavigation
    "arrived_at_drop" -> Screen.ArrivedAtDrop
    "proof_of_delivery" -> Screen.ProofOfDelivery
    "delivery_complete" -> Screen.DeliveryComplete
    else -> null
}
