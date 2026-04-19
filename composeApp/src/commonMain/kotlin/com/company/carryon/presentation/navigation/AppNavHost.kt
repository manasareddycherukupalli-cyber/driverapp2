package com.company.carryon.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.company.carryon.i18n.LocalStrings
import com.company.carryon.presentation.auth.*
import com.company.carryon.presentation.chat.*
import com.company.carryon.presentation.delivery.*
import com.company.carryon.presentation.earnings.*
import com.company.carryon.presentation.home.*
import com.company.carryon.presentation.jobs.*
import com.company.carryon.presentation.map.*
import com.company.carryon.presentation.onboarding.*
import com.company.carryon.presentation.profile.*
import com.company.carryon.presentation.ratings.*
import com.company.carryon.presentation.support.*

// ============================================================
// BOTTOM NAV ITEMS
// ============================================================

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector
)

// ============================================================
// APP NAV HOST — Renders current screen based on navigator state
// ============================================================

@Composable
fun AppNavHost(
    navigator: AppNavigator,
    currentLanguage: String = "en",
    onLanguageChanged: (String) -> Unit = {}
) {
    val currentScreen = navigator.currentScreen
    val showBottomBar = currentScreen in mainTabScreens
    val authViewModel = remember { AuthViewModel() }
    val onboardingViewModel = remember(authViewModel) { DriverOnboardingViewModel(authViewModel) }
    val bottomNavItems = rememberDriveBottomNavItems()

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                DriveAppBottomBar(navigator, bottomNavItems)
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() }
            ) { screen ->
                when (screen) {
                    // ---- Splash ----
                    Screen.Splash -> SplashScreen(navigator, authViewModel)

                    // ---- Auth Flow ----
                    Screen.Onboarding -> OnboardingScreen(navigator)
                    Screen.Login -> LoginScreen(navigator, authViewModel)
                    Screen.OtpVerification -> OtpVerificationScreen(navigator, authViewModel)
                    Screen.Registration -> RegistrationScreen(navigator, authViewModel)
                    Screen.DriverOnboarding -> DriverOnboardingFlowScreen(navigator, onboardingViewModel)
                    Screen.PersonalIdentity -> DriverOnboardingFlowScreen(navigator, onboardingViewModel)
                    Screen.DocumentUpload -> DriverOnboardingFlowScreen(navigator, onboardingViewModel)
                    Screen.VehicleDetailsInput -> DriverOnboardingFlowScreen(navigator, onboardingViewModel)
                    Screen.ReadyToDrive -> ReadyToDriveScreen(navigator)
                    Screen.VerificationStatus -> DriverVerificationStatusScreen(navigator, onboardingViewModel)
                    Screen.LocationPermission -> LocationPermissionScreen(navigator, authViewModel)

                    // ---- Main Tabs ----
                    Screen.Home -> {
                        val homeViewModel = remember { HomeViewModel() }
                        HomeScreen(navigator, homeViewModel)
                    }
                    Screen.Jobs -> JobsListScreen(navigator)
                    Screen.Earnings -> EarningsDashboardScreen(navigator)
                    Screen.Profile -> SettingsScreen(navigator, onLanguageChanged)

                    // ---- Sub Screens ----
                    Screen.JobDetails -> JobDetailsScreen(navigator)
                    Screen.ActiveDelivery -> ActiveDeliveryScreen(navigator)
                    Screen.PickupInstructions -> PickupInstructionsScreen(navigator)
                    Screen.StartDelivery -> StartDeliveryScreen(navigator)
                    Screen.InTransitNavigation -> InTransitScreen(navigator)
                    Screen.ArrivedAtDrop -> ArrivedAtDropScreen(navigator)
                    Screen.ProofOfDelivery -> ProofOfDeliveryScreen(navigator)
                    Screen.DeliveryComplete -> DeliveryCompleteScreen(navigator)
                    Screen.JobReceipt -> JobReceiptScreen(navigator)
                    Screen.MapNavigation -> MapScreen(navigator)
                    Screen.Wallet -> WalletScreen(navigator)
                    Screen.TransactionHistory -> WalletScreen(navigator)
                    Screen.Ratings -> RatingsScreen(navigator)
                    Screen.EditProfile -> EditProfileScreen(navigator)
                    Screen.Settings -> SettingsScreen(navigator, onLanguageChanged)
                    Screen.NotificationPreferences -> NotificationPreferencesScreen(navigator)
                    Screen.Language -> LanguageScreen(navigator, currentLanguage, onLanguageChanged)
                    Screen.VehicleInfo -> VehicleInfoScreen(navigator)
                    Screen.DocumentsHub -> DocumentsHubScreen(navigator)
                    Screen.TermsOfService -> TermsOfServiceScreen(navigator)
                    Screen.PrivacyPolicy -> PrivacyPolicyScreen(navigator)
                    Screen.HelpCenter -> HelpCenterScreen(navigator)
                    Screen.HelpGettingStarted -> HelpTopicScreen(
                        navigator = navigator,
                        title = "Getting Started",
                        summary = "Account setup and first steps for new drivers.",
                        tips = listOf(
                            "Complete profile, vehicle, and document verification.",
                            "Enable location permission for live dispatch updates.",
                            "Keep banking details updated for payout processing."
                        )
                    )
                    Screen.HelpPayments -> HelpTopicScreen(
                        navigator = navigator,
                        title = "Payments",
                        summary = "Direct deposit, earnings, and tax reimbursement.",
                        tips = listOf(
                            "Weekly earnings are visible in the Earnings tab.",
                            "Use Wallet to request withdrawals to linked bank.",
                            "Download payout records for monthly reconciliation."
                        )
                    )
                    Screen.HelpSafety -> HelpTopicScreen(
                        navigator = navigator,
                        title = "Safety",
                        summary = "Incident reporting and safety guidelines.",
                        tips = listOf(
                            "Use SOS for emergency escalation.",
                            "Report incidents with clear notes and timestamps.",
                            "Follow route and delivery verification protocols."
                        )
                    )
                    Screen.HelpAppIssues -> HelpTopicScreen(
                        navigator = navigator,
                        title = "App Issues",
                        summary = "Technical glitches and app navigation help.",
                        tips = listOf(
                            "Check connection and restart the app if stuck.",
                            "Update to the latest app version.",
                            "Raise a support ticket if issue persists."
                        )
                    )
                    Screen.HelpContactSupport -> ContactSupportScreen(navigator)
                    Screen.HelpDriverHandbook -> DriverHandbookScreen(navigator)
                    Screen.HelpHandbookAccountCompliance -> HandbookDetailScreen(
                        navigator = navigator,
                        title = "Account & Compliance",
                        points = listOf(
                            "Complete profile, identity, and vehicle details accurately.",
                            "Keep license and insurance documents up to date.",
                            "Rejected documents must be re-uploaded with clear images.",
                            "Non-compliance can pause dispatch eligibility."
                        )
                    )
                    Screen.HelpHandbookDeliveryWorkflow -> HandbookDetailScreen(
                        navigator = navigator,
                        title = "Delivery Workflow",
                        points = listOf(
                            "Accept assigned job and confirm pickup instructions.",
                            "Mark status updates at each milestone (pickup, in-transit, drop).",
                            "Capture proof-of-delivery before completing the order.",
                            "Use in-app support for customer/location exceptions."
                        )
                    )
                    Screen.HelpHandbookEarningsPayouts -> HandbookDetailScreen(
                        navigator = navigator,
                        title = "Earnings & Payouts",
                        points = listOf(
                            "Earnings include base trip amount and eligible bonuses.",
                            "Completed deliveries are reflected in Earnings and Wallet.",
                            "Withdrawals are available to linked bank accounts.",
                            "Review transaction history for payout reconciliation."
                        )
                    )
                    Screen.HelpHandbookSafetyIncident -> HandbookDetailScreen(
                        navigator = navigator,
                        title = "Safety & Incident Response",
                        points = listOf(
                            "Prioritize personal safety and follow road regulations.",
                            "Use SOS immediately for emergency situations.",
                            "Report incidents with time, location, and summary details.",
                            "Follow support guidance for escalation and resolution."
                        )
                    )
                    Screen.RaiseTicket -> RaiseTicketScreen(navigator)
                    Screen.SupportChat -> SupportChatScreen(navigator)
                    Screen.CustomerChat -> CustomerChatScreen(navigator)
                    Screen.Sos -> SosScreen(navigator)
                    Screen.Notifications -> NotificationsScreen(navigator)
                }
            }
        }
    }
}

@Composable
fun rememberDriveBottomNavItems(): List<BottomNavItem> {
    val strings = LocalStrings.current
    return remember(strings) {
        listOf(
            BottomNavItem(Screen.Home, strings.navHome, Icons.Filled.Home, Icons.Filled.Home),
            BottomNavItem(Screen.Jobs, strings.navJobs, Icons.Filled.LocalShipping, Icons.Filled.LocalShipping),
            BottomNavItem(Screen.Earnings, strings.navEarnings, Icons.Filled.AccountBalanceWallet, Icons.Filled.AccountBalanceWallet),
            BottomNavItem(Screen.Profile, strings.navProfile, Icons.Filled.Person, Icons.Filled.Person),
        )
    }
}

// ============================================================
// BOTTOM NAVIGATION BAR
// ============================================================

@Composable
fun DriveAppBottomBar(navigator: AppNavigator, items: List<BottomNavItem>) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        items.forEach { item ->
            val isSelected = navigator.currentScreen == item.screen
            NavigationBarItem(
                selected = isSelected,
                onClick = { navigator.switchTab(item.screen) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.icon,
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}
