package com.company.carryon.presentation.earnings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.NotificationsNone
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.carryon.data.model.EarningsSummary
import com.company.carryon.data.model.Transaction
import com.company.carryon.data.model.UiState
import com.company.carryon.data.model.WalletInfo
import com.company.carryon.presentation.navigation.AppNavigator
import com.company.carryon.presentation.navigation.Screen
import kotlin.math.abs
import kotlin.math.round

private val BrandBlue = Color(0xFF2F80ED)
private val BrandDarkBlue = Color(0xFF034094)
private val LightBlue = Color(0xFFA6D2F3)
private val TextPrimary = Color(0xFF181C23)
private val TextMuted = Color(0xFF414755)

private data class TripUi(
    val title: String,
    val date: String,
    val miles: String,
    val amount: Double,
    val iconType: String
)

@Composable
fun EarningsDashboardScreen(navigator: AppNavigator) {
    val viewModel = remember { EarningsViewModel() }
    val earningsState by viewModel.earningsSummary.collectAsState()
    val transactionsState by viewModel.transactions.collectAsState()
    val walletState by viewModel.walletInfo.collectAsState()

    var weeklySelected by remember { mutableStateOf(true) }

    val summary = (earningsState as? UiState.Success<EarningsSummary>)?.data ?: EarningsSummary(
        todayEarnings = 420.0,
        weeklyEarnings = 842.2,
        totalDeliveries = 114,
        onlineHours = 38.5
    )
    val wallet = (walletState as? UiState.Success<WalletInfo>)?.data ?: WalletInfo(balance = 2485.5)

    val netProfit = if (summary.weeklyEarnings > 0.0) summary.weeklyEarnings else 842.2
    val displayBalance = if (weeklySelected) {
        if (wallet.balance > 0.0) wallet.balance else netProfit
    } else {
        if (summary.todayEarnings > 0.0) summary.todayEarnings else 248.55
    }

    val trips = buildTrips((transactionsState as? UiState.Success<List<Transaction>>)?.data)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        HeaderRow(
            onMenuClick = { navigator.switchTab(Screen.Home) },
            onNotificationClick = { navigator.navigateTo(Screen.Notifications) }
        )

        Text(
            text = "Earning",
            color = TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        TabSelector(
            weeklySelected = weeklySelected,
            onTodayClick = { weeklySelected = false },
            onWeeklyClick = { weeklySelected = true }
        )

        BalanceCard(
            balance = displayBalance,
            onWithdraw = { navigator.navigateTo(Screen.Wallet) }
        )

        WeeklySection(netProfit = netProfit)

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            StatChip(
                label = "Deliveries",
                value = (if (summary.totalDeliveries > 0) summary.totalDeliveries else 114).toString(),
                icon = { Icon(Icons.Filled.LocalShipping, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.weight(1f)
            )
            StatChip(
                label = "Active Hours",
                value = "${formatSingleDecimal(if (summary.onlineHours > 0.0) summary.onlineHours else 38.5)}h",
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

        trips.forEach { trip ->
            TripCard(trip = trip)
        }

        if (earningsState is UiState.Loading || walletState is UiState.Loading) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BrandBlue, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            }
        }

        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun HeaderRow(onMenuClick: () -> Unit, onNotificationClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Icon(
            Icons.Filled.Menu,
            contentDescription = "Menu",
            tint = Color(0xFF5E6470),
            modifier = Modifier.size(22.dp).clickable { onMenuClick() }
        )
        Text(
            buildAnnotatedString {
                withStyle(SpanStyle(color = BrandBlue, fontWeight = FontWeight.Bold)) { append("Carry") }
                append(" ")
                withStyle(SpanStyle(color = BrandDarkBlue, fontWeight = FontWeight.Bold)) { append("On") }
            },
            fontSize = 36.sp / 1.6f
        )
        Icon(
            Icons.Filled.NotificationsNone,
            contentDescription = "Notifications",
            tint = Color(0xFF5E6470),
            modifier = Modifier.size(22.dp).clickable { onNotificationClick() }
        )
    }
}

@Composable
private fun TabSelector(weeklySelected: Boolean, onTodayClick: () -> Unit, onWeeklyClick: () -> Unit) {
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
                maxLines = 1
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
                maxLines = 1
            )
        }
        Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(Color(0xFFEFF2F8))) {
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
private fun BalanceCard(balance: Double, onWithdraw: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF0058BC), Color(0xFF2F80ED))
                    )
                )
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("AVAILABLE BALANCE", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Text("$${formatMoney(balance)}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 33.sp / 1.5f)
                Spacer(Modifier.height(4.dp))
                Button(
                    onClick = onWithdraw,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Withdraw Funds", color = Color(0xFF0058BC), fontWeight = FontWeight.SemiBold, fontSize = 14.sp / 1.1f)
                }
            }
        }
    }
}

@Composable
private fun WeeklySection(netProfit: Double) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text("Weekly Earnings", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 24.sp / 1.15f)
                Text("Oct 21 - Oct 27, 2023", color = TextMuted, fontSize = 14.sp / 1.05f)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("+$${formatMoney(netProfit)}", color = Color(0xFF0058BC), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text("NET PROFIT", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            WeeklyBarChart(modifier = Modifier.padding(horizontal = 12.dp, vertical = 16.dp))
        }
    }
}

@Composable
private fun WeeklyBarChart(modifier: Modifier = Modifier) {
    val bars = listOf(128, 160, 96, 192, 144, 64, 80)
    val days = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
    val maxHeight = 150.dp

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        bars.forEachIndexed { index, heightValue ->
            val alpha = when (index) {
                3 -> 1f
                1, 4 -> 0.45f
                5, 6 -> 0.2f
                else -> 0.25f
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(maxHeight * (heightValue / 192f))
                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                        .background(Color(0xFFF1F3FE))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(if (index == 3) Color(0xFF0058BC) else Color(0xFF0058BC).copy(alpha = alpha))
                    )
                }
                Text(
                    days[index],
                    color = if (index == 3) Color(0xFF0058BC) else Color(0xFF94A3B8),
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
        colors = CardDefaults.cardColors(containerColor = LightBlue)
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
private fun TripCard(trip: TripUi) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
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
                        Box(modifier = Modifier.size(3.dp).clip(CircleShape).background(Color(0xFFC1C6D7)))
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
                        .border(0.dp, Color.Transparent)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("COMPLETED", color = BrandBlue, fontWeight = FontWeight.SemiBold, fontSize = 10.sp)
                }
            }
        }
    }
}

private fun buildTrips(transactions: List<Transaction>?): List<TripUi> {
    if (transactions.isNullOrEmpty()) {
        return listOf(
            TripUi("Regional Hub\nDelivery", "Oct 24,\n2023", "142", 312.40, "truck"),
            TripUi("Last Mile Express", "Oct 24,\n2023", "18", 45.00, "store"),
            TripUi("Warehouse Return", "Oct 23,\n2023", "32", 88.15, "warehouse")
        )
    }

    return transactions.take(3).mapIndexed { index, t ->
        val fallbackTitle = when (index) {
            0 -> "Regional Hub\nDelivery"
            1 -> "Last Mile Express"
            else -> "Warehouse Return"
        }
        val fallbackMiles = when (index) {
            0 -> "142"
            1 -> "18"
            else -> "32"
        }
        val icon = when (index) {
            1 -> "store"
            2 -> "warehouse"
            else -> "truck"
        }

        TripUi(
            title = t.description.ifBlank { fallbackTitle },
            date = formatTripDate(t.timestamp),
            miles = fallbackMiles,
            amount = abs(t.amount).takeIf { it > 0.0 } ?: listOf(312.40, 45.00, 88.15).getOrElse(index) { 42.0 },
            iconType = icon
        )
    }
}

private fun formatMoney(value: Double): String {
    val cents = round(value * 100.0).toLong()
    val absCents = abs(cents)
    val whole = absCents / 100
    val fraction = (absCents % 100).toString().padStart(2, '0')
    return "$whole.$fraction"
}

private fun formatSingleDecimal(value: Double): String {
    val scaled = round(value * 10.0) / 10.0
    return if (scaled % 1.0 == 0.0) "${scaled.toInt()}.0" else scaled.toString()
}

private fun formatTripDate(timestamp: String?): String {
    if (timestamp.isNullOrBlank()) return "Oct 24,\n2023"
    val date = timestamp.take(10)
    val parts = date.split("-")
    if (parts.size != 3) return "Oct 24,\n2023"

    val month = when (parts[1]) {
        "01" -> "Jan"
        "02" -> "Feb"
        "03" -> "Mar"
        "04" -> "Apr"
        "05" -> "May"
        "06" -> "Jun"
        "07" -> "Jul"
        "08" -> "Aug"
        "09" -> "Sep"
        "10" -> "Oct"
        "11" -> "Nov"
        "12" -> "Dec"
        else -> "Oct"
    }

    return "$month ${parts[2]},\n${parts[0]}"
}
