package com.company.carryon.presentation.earnings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.carryon.presentation.components.DriveAppTopBar
import com.company.carryon.presentation.navigation.AppNavigator
import com.company.carryon.presentation.navigation.Screen
import kotlin.math.round

private val BrandBlue = Color(0xFF2F80ED)
private val LightBlue = Color(0xFFA6D2F3)
private val TextPrimary = Color(0xFF181C23)
private val TextMuted = Color(0xFF414755)

@Composable
fun EarningsDashboardScreen(navigator: AppNavigator) {
    val viewModel = remember { EarningsViewModel() }
    val dashboardUi by viewModel.dashboardUi.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        DriveAppTopBar(
            title = "Earning",
            onBackClick = { navigator.switchTab(Screen.Home) },
            leadingIcon = Icons.Filled.Menu,
            onNotificationClick = { navigator.navigateTo(Screen.Notifications) }
        )

        TabSelector(
            selectedPeriod = dashboardUi.selectedPeriod,
            onTodayClick = { viewModel.setSelectedPeriod(EarningsPeriod.TODAY) },
            onWeeklyClick = { viewModel.setSelectedPeriod(EarningsPeriod.THIS_WEEK) }
        )

        BalanceCard(
            balance = dashboardUi.displayBalance,
            onWithdraw = { navigator.navigateTo(Screen.Wallet) }
        )

        WeeklySection(
            weekRangeLabel = dashboardUi.weekRangeLabel,
            netProfit = dashboardUi.weeklyAmount,
            chart = dashboardUi.chart
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            StatChip(
                label = "Deliveries",
                value = dashboardUi.deliveriesCount?.toString() ?: "--",
                icon = { Icon(Icons.Filled.LocalShipping, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.weight(1f)
            )
            StatChip(
                label = "Active Hours",
                value = dashboardUi.activeHours?.let { "${formatSingleDecimal(it)}h" } ?: "--",
                icon = { Icon(Icons.Filled.Schedule, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Recent Trips", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 34.sp / 1.4f)
            Text(
                "View All",
                color = BrandBlue,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                modifier = Modifier.clickable { navigator.navigateTo(Screen.Wallet) }
            )
        }

        when {
            dashboardUi.trips.isNotEmpty() -> dashboardUi.trips.forEach { trip ->
                TripCard(trip = trip)
            }

            dashboardUi.isLoading -> LoadingCard("Loading recent trips...")
            else -> EmptyCard("No completed trips yet")
        }

        dashboardUi.errorMessage?.let { message ->
            MessageCard(
                message = message,
                actionLabel = "Retry",
                onAction = { viewModel.loadAll() }
            )
        }

        if (dashboardUi.isLoading) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BrandBlue, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            }
        }

        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun TabSelector(
    selectedPeriod: EarningsPeriod,
    onTodayClick: () -> Unit,
    onWeeklyClick: () -> Unit
) {
    val weeklySelected = selectedPeriod != EarningsPeriod.TODAY
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                "TODAY",
                color = if (weeklySelected) Color(0xFFDADFE7) else BrandBlue,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onTodayClick() }
                    .padding(vertical = 8.dp),
                textAlign = TextAlign.Center
            )
            Text(
                "WEEKLY",
                color = if (weeklySelected) BrandBlue else Color(0xFFDADFE7),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onWeeklyClick() }
                    .padding(vertical = 8.dp),
                textAlign = TextAlign.Center
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(Color(0xFFEFF2F8))
        ) {
            Box(
                modifier = Modifier
                    .align(if (weeklySelected) Alignment.CenterEnd else Alignment.CenterStart)
                    .fillMaxWidth(0.5f)
                    .height(2.dp)
                    .background(BrandBlue)
            )
        }
    }
}

@Composable
private fun BalanceCard(balance: Double?, onWithdraw: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(colors = listOf(BrandBlue, BrandBlue)))
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("AVAILABLE BALANCE", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Text(
                    balance?.let { "$${formatMoney(it)}" } ?: "--",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 33.sp / 1.5f
                )
                Spacer(Modifier.height(4.dp))
                Button(
                    onClick = onWithdraw,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Withdraw Funds", color = BrandBlue, fontWeight = FontWeight.SemiBold, fontSize = 14.sp / 1.1f)
                }
            }
        }
    }
}

@Composable
private fun WeeklySection(
    weekRangeLabel: String,
    netProfit: Double?,
    chart: List<EarningsChartPoint>
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text("Weekly Earnings", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 24.sp / 1.15f)
                Text(weekRangeLabel, color = TextMuted, fontSize = 14.sp / 1.05f)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    netProfit?.let { "+$${formatMoney(it)}" } ?: "--",
                    color = BrandBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Text("NET PROFIT", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            WeeklyBarChart(chart = chart, modifier = Modifier.padding(horizontal = 12.dp, vertical = 16.dp))
        }
    }
}

@Composable
private fun WeeklyBarChart(chart: List<EarningsChartPoint>, modifier: Modifier = Modifier) {
    val maxHeight = 150.dp
    val maxAmount = chart.maxOfOrNull { it.amount }?.takeIf { it > 0.0 } ?: 1.0

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        chart.forEach { point ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(
                            if (point.amount > 0.0) {
                                maxHeight * (point.amount / maxAmount).toFloat()
                            } else {
                                0.dp
                            }
                        )
                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                        .background(if (point.isHighlighted) BrandBlue else BrandBlue.copy(alpha = 0.28f))
                )
                Text(
                    point.dayLabel,
                    color = if (point.isHighlighted) BrandBlue else Color(0xFF94A3B8),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun StatChip(label: String, value: String, icon: @Composable () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = LightBlue),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            icon()
            Text(label, color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Text(value, color = BrandBlue, fontSize = 28.sp / 1.4f, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun TripCard(trip: EarningsTripUi) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(LightBlue),
                    contentAlignment = Alignment.Center
                ) {
                    when (trip.iconType) {
                        "store" -> Icon(Icons.Filled.Store, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(18.dp))
                        "warehouse" -> Icon(Icons.Filled.Warehouse, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(18.dp))
                        else -> Icon(Icons.Filled.LocalShipping, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(18.dp))
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(trip.title, color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp / 1.05f)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Icon(Icons.Filled.CalendarToday, contentDescription = null, tint = TextMuted, modifier = Modifier.size(10.dp))
                        Text(trip.date, color = TextMuted, fontSize = 12.sp, lineHeight = 14.sp)
                        Text("${trip.miles} mi", color = TextMuted, fontSize = 12.sp)
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("+$${formatMoney(trip.amount)}", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 18.sp / 1.05f)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(LightBlue)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("COMPLETED", color = BrandBlue, fontWeight = FontWeight.SemiBold, fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
private fun EmptyCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFD)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Text(
            message,
            color = TextMuted,
            fontSize = 14.sp,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
private fun LoadingCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFD)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(color = BrandBlue, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            Text(message, color = TextMuted, fontSize = 14.sp)
        }
    }
}

@Composable
private fun MessageCard(message: String, actionLabel: String, onAction: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFD)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(message, color = TextMuted, fontSize = 14.sp)
            Text(
                actionLabel,
                color = BrandBlue,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable(onClick = onAction)
            )
        }
    }
}

private fun formatMoney(value: Double): String {
    val cents = round(value * 100.0).toLong()
    val whole = cents / 100
    val fraction = (cents % 100).toString().padStart(2, '0')
    return "$whole.$fraction"
}

private fun formatSingleDecimal(value: Double): String {
    val scaled = round(value * 10.0) / 10.0
    return if (scaled % 1.0 == 0.0) "${scaled.toInt()}.0" else scaled.toString()
}
