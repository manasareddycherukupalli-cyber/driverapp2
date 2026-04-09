package com.company.carryon.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import drive_app.composeapp.generated.resources.*
import com.company.carryon.data.model.Driver
import com.company.carryon.data.model.UiState
import com.company.carryon.data.model.VerificationStatus
import com.company.carryon.presentation.navigation.AppNavigator
import com.company.carryon.presentation.navigation.Screen
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

private val VerifyBlue = Color(0xFF2F80ED)
private val VerifyBg = Color(0xFFF3F4F9)
private val SoftBlue = Color(0xFFAECFF3)
private val SoftBlue20 = Color(0x2EAECFF3)
private val SoftBlue30 = Color(0x40AFC7E7)
private val PureBlack = Color(0xFF000000)

@Composable
fun VerificationStatusScreen(navigator: AppNavigator, viewModel: AuthViewModel) {
    val verificationState by viewModel.verificationState.collectAsState()

    LaunchedEffect(Unit) { viewModel.checkVerificationStatus() }

    when (val state = verificationState) {
        is UiState.Loading, UiState.Idle -> {
            Box(modifier = Modifier.fillMaxSize().background(VerifyBg), contentAlignment = Alignment.Center) {
                Text("Checking verification status...", color = PureBlack)
            }
        }
        is UiState.Error -> {
            Box(modifier = Modifier.fillMaxSize().background(VerifyBg), contentAlignment = Alignment.Center) {
                Text(state.message, color = MaterialTheme.colorScheme.error)
            }
        }
        is UiState.Success -> {
            val driver = state.data
            if (driver.verificationStatus == VerificationStatus.APPROVED) {
                ApprovedFlow(navigator)
            } else {
                VerificationSummary(driver = driver, navigator = navigator)
            }
        }
    }
}

@Composable
private fun VerificationSummary(driver: Driver, navigator: AppNavigator) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(VerifyBg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = VerifyBlue, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("Precision Navigator", fontWeight = FontWeight.Bold, color = PureBlack, fontSize = 18.sp)
                }
                Icon(Icons.Filled.AccountCircle, contentDescription = null, tint = VerifyBlue, modifier = Modifier.size(22.dp))
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .background(SoftBlue, RoundedCornerShape(999.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(Res.drawable.verify_safety_icon),
                            contentDescription = null,
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(Modifier.width(5.dp))
                        Text("SAFETY PROTOCOL", color = VerifyBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Text("verification", fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = PureBlack)
                Text(
                    "Please complete all steps to unlock full navigation and route dispatch features.",
                    color = PureBlack.copy(alpha = 0.65f),
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )

                StatusCard(
                    iconRes = Res.drawable.verify_personal_identity,
                    title = "Personal Identity",
                    desc = "Government-issued ID and personal profile data successfully validated.",
                    pill = "Verified",
                    pillIconRes = Res.drawable.verify_badge_verified,
                    progress = 1f,
                    onCardClick = { navigator.navigateTo(Screen.PersonalIdentity) }
                )
                StatusCard(
                    iconRes = Res.drawable.verify_vehicle_details,
                    title = "Vehicle Details",
                    desc = "Registration and insurance documents are being processed by our compliance team.",
                    pill = "Under Review",
                    pillIconRes = Res.drawable.verify_badge_under_review,
                    progress = 0.66f,
                    onCardClick = { navigator.navigateTo(Screen.VehicleDetailsInput) }
                )
                StatusCard(
                    iconRes = Res.drawable.verify_identity_check,
                    title = "Identity Verification",
                    desc = "Biometric selfie check to confirm live identity matches your provided documentation.",
                    pill = "Not Started",
                    progress = 0f,
                    primary = "Resume Verification",
                    onPrimary = { navigator.navigateTo(Screen.DocumentUpload) },
                    onCardClick = { navigator.navigateTo(Screen.DocumentUpload) }
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SoftBlue30)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Need assistance?", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PureBlack)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Our support team is available 24/7 to help with documentation issues.",
                            fontSize = 15.sp,
                            lineHeight = 21.sp,
                            color = PureBlack.copy(alpha = 0.65f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(Res.drawable.verify_support_icon),
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Contact Support Desk", color = VerifyBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
            ) {
                NavigationBar(
                    containerColor = Color.White,
                    contentColor = PureBlack
                ) {
                    NavigationBarItem(
                        selected = true,
                        onClick = { navigator.switchTab(Screen.Home) },
                        icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = VerifyBlue,
                            selectedTextColor = VerifyBlue,
                            unselectedIconColor = PureBlack.copy(alpha = 0.45f),
                            unselectedTextColor = PureBlack.copy(alpha = 0.45f),
                            indicatorColor = Color(0x33A6D2F3)
                        )
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { navigator.switchTab(Screen.Jobs) },
                        icon = { Icon(Icons.Filled.LocalShipping, contentDescription = "Jobs") },
                        label = { Text("Jobs") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = VerifyBlue,
                            selectedTextColor = VerifyBlue,
                            unselectedIconColor = PureBlack.copy(alpha = 0.45f),
                            unselectedTextColor = PureBlack.copy(alpha = 0.45f),
                            indicatorColor = Color(0x33A6D2F3)
                        )
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { navigator.switchTab(Screen.Earnings) },
                        icon = { Icon(Icons.Filled.AccountBalanceWallet, contentDescription = "Earnings") },
                        label = { Text("Earnings") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = VerifyBlue,
                            selectedTextColor = VerifyBlue,
                            unselectedIconColor = PureBlack.copy(alpha = 0.45f),
                            unselectedTextColor = PureBlack.copy(alpha = 0.45f),
                            indicatorColor = Color(0x33A6D2F3)
                        )
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { navigator.switchTab(Screen.Profile) },
                        icon = { Icon(Icons.Filled.Person, contentDescription = "Profile") },
                        label = { Text("Profile") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = VerifyBlue,
                            selectedTextColor = VerifyBlue,
                            unselectedIconColor = PureBlack.copy(alpha = 0.45f),
                            unselectedTextColor = PureBlack.copy(alpha = 0.45f),
                            indicatorColor = Color(0x33A6D2F3)
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusCard(
    iconRes: DrawableResource,
    title: String,
    desc: String,
    pill: String,
    pillIconRes: DrawableResource? = null,
    progress: Float,
    primary: String? = null,
    onPrimary: (() -> Unit)? = null,
    onCardClick: (() -> Unit)? = null
) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
            .then(
                if (onCardClick != null) Modifier.clickable { onCardClick() } else Modifier
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(40.dp).background(SoftBlue20, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                    Image(painter = painterResource(iconRes), contentDescription = null, modifier = Modifier.size(18.dp))
                }
                Box(modifier = Modifier.background(SoftBlue, RoundedCornerShape(999.dp)).padding(horizontal = 12.dp, vertical = 4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (pillIconRes != null) {
                            Image(
                                painter = painterResource(pillIconRes),
                                contentDescription = null,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                        } else {
                            Icon(
                                Icons.Filled.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = VerifyBlue,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                        }
                        Text(pill, color = VerifyBlue, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = PureBlack)
            Text(desc, color = PureBlack.copy(alpha = 0.65f), fontSize = 12.sp, lineHeight = 16.sp)
            if (primary != null && onPrimary != null) {
                Button(
                    onClick = onPrimary,
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VerifyBlue)
                ) {
                    Text(primary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
            Box(modifier = Modifier.fillMaxWidth().height(3.dp).background(SoftBlue20, RoundedCornerShape(999.dp))) {
                if (progress > 0f) {
                    Box(modifier = Modifier.fillMaxWidth(progress).height(3.dp).background(VerifyBlue, RoundedCornerShape(999.dp)))
                }
            }
        }
    }
}

@Composable
private fun ApprovedFlow(navigator: AppNavigator) {
    var readyStep by remember { mutableStateOf(false) }

    if (!readyStep) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(VerifyBg)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Spacer(Modifier.height(50.dp))
            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = VerifyBlue, modifier = Modifier.size(70.dp))
            Text("Verification Successful", fontWeight = FontWeight.ExtraBold, fontSize = 32.sp)
            Text("Congratulations! Your account is now active and you can start accepting jobs.", textAlign = TextAlign.Center, color = Color(0xFF414755))

            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0x33A6D2F3)), shape = RoundedCornerShape(12.dp)) {
                Column(Modifier.padding(14.dp)) {
                    Text("NEXT STEP", color = VerifyBlue, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    Spacer(Modifier.height(6.dp))
                    Text("Complete your first delivery", fontWeight = FontWeight.SemiBold)
                }
            }

            Button(
                onClick = { readyStep = true },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VerifyBlue),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Go to Dashboard") }
        }
    } else {
        var accepted by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(VerifyBg)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(40.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.align(Alignment.CenterHorizontally)) {
                repeat(3) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = VerifyBlue)
                    if (it < 2) Box(modifier = Modifier.width(56.dp).height(2.dp).background(VerifyBlue).align(Alignment.CenterVertically))
                }
            }
            Text("Step 3 of 3", color = Color(0x99000000), fontSize = 12.sp)
            Text("You are Ready to Drive", fontWeight = FontWeight.ExtraBold, fontSize = 36.sp)

            Box(modifier = Modifier.fillMaxWidth().height(240.dp).background(Color(0xFFD4D9E6), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.DirectionsCar, contentDescription = null, tint = VerifyBlue, modifier = Modifier.size(72.dp))
            }

            Text("Your account is now activated. Let's book your load.", fontSize = 17.sp)
            Spacer(Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = accepted, onCheckedChange = { accepted = it })
                Text("I Accept this ")
                Text("Terms and Conditions", color = VerifyBlue, modifier = Modifier.clickable { accepted = true })
            }

            Button(
                onClick = { navigator.navigateAndClearStack(Screen.Home) },
                enabled = accepted,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VerifyBlue),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Continue") }
        }
    }
}
