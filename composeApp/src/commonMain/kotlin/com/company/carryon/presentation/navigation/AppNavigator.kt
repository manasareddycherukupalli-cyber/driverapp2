package com.company.carryon.presentation.navigation

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
    data object DocumentUpload : Screen
    data object VehicleDetailsInput : Screen
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
    data object ProofOfDelivery : Screen
    data object MapNavigation : Screen
    data object Wallet : Screen
    data object TransactionHistory : Screen
    data object Ratings : Screen
    data object EditProfile : Screen
    data object Settings : Screen
    data object HelpCenter : Screen
    data object RaiseTicket : Screen
    data object SupportChat : Screen
    data object Sos : Screen
    data object Notifications : Screen
}

/** Screens that show the bottom navigation bar */
val mainTabScreens = setOf(Screen.Home, Screen.Jobs, Screen.Earnings, Screen.Profile)

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
    var selectedJobId by mutableStateOf<String?>(null)
    var selectedTicketId by mutableStateOf<String?>(null)
    var phoneNumber by mutableStateOf("")
    var countryCode by mutableStateOf("+60")
    var initialJobsTabIndex by mutableIntStateOf(0)

    /**
     * Navigate to a new screen, pushing current screen to back stack.
     */
    fun navigateTo(screen: Screen) {
        backStack.add(currentScreen)
        currentScreen = screen
    }

    /**
     * Navigate to a screen and clear the entire back stack.
     * Used for navigation resets (e.g., after login → home).
     */
    fun navigateAndClearStack(screen: Screen) {
        backStack.clear()
        currentScreen = screen
    }

    /**
     * Navigate back to the previous screen.
     * @return true if back navigation was performed, false if at root.
     */
    fun goBack(): Boolean {
        return if (backStack.isNotEmpty()) {
            currentScreen = backStack.removeLast()
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
        }
    }

    /** Check if back navigation is possible */
    val canGoBack: Boolean get() = backStack.isNotEmpty()
}
