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
import com.company.carryon.presentation.delivery.*
import com.company.carryon.presentation.earnings.*
import com.company.carryon.presentation.home.*
import com.company.carryon.presentation.jobs.*
import com.company.carryon.presentation.map.*
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
    val strings = LocalStrings.current
    val currentScreen = navigator.currentScreen
    val showBottomBar = currentScreen in mainTabScreens
    val authViewModel = remember { AuthViewModel() }

    val bottomNavItems = listOf(
        BottomNavItem(Screen.Home, strings.navHome, Icons.Filled.Home, Icons.Filled.Home),
        BottomNavItem(Screen.Jobs, strings.navJobs, Icons.Filled.LocalShipping, Icons.Filled.LocalShipping),
        BottomNavItem(Screen.Earnings, strings.navEarnings, Icons.Filled.AccountBalanceWallet, Icons.Filled.AccountBalanceWallet),
        BottomNavItem(Screen.Profile, strings.navProfile, Icons.Filled.Person, Icons.Filled.Person),
    )

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
                    Screen.PersonalIdentity -> PersonalIdentityScreen(navigator, authViewModel)
                    Screen.DocumentUpload -> DocumentUploadScreen(navigator, authViewModel)
                    Screen.VehicleDetailsInput -> VehicleDetailsScreen(navigator, authViewModel)
                    Screen.VerificationStatus -> VerificationStatusScreen(navigator, authViewModel)
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
                    Screen.RaiseTicket -> RaiseTicketScreen(navigator)
                    Screen.SupportChat -> SupportChatScreen(navigator)
                    Screen.Sos -> SosScreen(navigator)
                    Screen.Notifications -> NotificationsScreen(navigator)
                }
            }
        }
    }
}

// ============================================================
// BOTTOM NAVIGATION BAR
// ============================================================

@Composable
private fun DriveAppBottomBar(navigator: AppNavigator, items: List<BottomNavItem>) {
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
