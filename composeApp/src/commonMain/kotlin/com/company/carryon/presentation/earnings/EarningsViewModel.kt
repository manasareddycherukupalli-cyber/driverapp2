package com.company.carryon.presentation.earnings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.company.carryon.data.model.*
import com.company.carryon.di.ServiceLocator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.round
import kotlin.time.Clock

/**
 * EarningsViewModel — Manages earnings dashboard, wallet, and transaction data.
 */
class EarningsViewModel : ViewModel() {

    private val repository = ServiceLocator.earningsRepository
    private val jobRepository = ServiceLocator.jobRepository

    // Earnings summary
    private val _earningsSummary = MutableStateFlow<UiState<EarningsSummary>>(UiState.Idle)
    val earningsSummary: StateFlow<UiState<EarningsSummary>> = _earningsSummary.asStateFlow()

    // Transaction history
    private val _transactions = MutableStateFlow<UiState<List<Transaction>>>(UiState.Idle)
    val transactions: StateFlow<UiState<List<Transaction>>> = _transactions.asStateFlow()

    // Wallet info
    private val _walletInfo = MutableStateFlow<UiState<WalletInfo>>(UiState.Idle)
    val walletInfo: StateFlow<UiState<WalletInfo>> = _walletInfo.asStateFlow()

    // Completed jobs for recent trips and weekly grouping
    private val _completedJobs = MutableStateFlow<UiState<List<DeliveryJob>>>(UiState.Idle)
    val completedJobs: StateFlow<UiState<List<DeliveryJob>>> = _completedJobs.asStateFlow()

    // Withdrawal state
    private val _withdrawalState = MutableStateFlow<UiState<Transaction>>(UiState.Idle)
    val withdrawalState: StateFlow<UiState<Transaction>> = _withdrawalState.asStateFlow()

    private val _payoutStatus = MutableStateFlow<UiState<PayoutStatus>>(UiState.Idle)
    val payoutStatus: StateFlow<UiState<PayoutStatus>> = _payoutStatus.asStateFlow()

    private val _onboardingLink = MutableStateFlow<UiState<PayoutOnboardingLink>>(UiState.Idle)
    val onboardingLink: StateFlow<UiState<PayoutOnboardingLink>> = _onboardingLink.asStateFlow()

    // Selected time period
    private val _selectedPeriod = MutableStateFlow(EarningsPeriod.THIS_WEEK)
    val selectedPeriod: StateFlow<EarningsPeriod> = _selectedPeriod.asStateFlow()

    val dashboardUi: StateFlow<EarningsDashboardUiModel> = combine(
        _selectedPeriod,
        _earningsSummary,
        _transactions,
        _walletInfo,
        _completedJobs
    ) { period, earningsState, transactionsState, walletState, completedJobsState ->
        buildDashboardUi(
            period = period,
            earningsState = earningsState,
            transactionsState = transactionsState,
            walletState = walletState,
            completedJobsState = completedJobsState
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = EarningsDashboardUiModel(
            selectedPeriod = EarningsPeriod.THIS_WEEK,
            chart = defaultWeeklyChart(),
            trips = emptyList()
        )
    )

    init {
        loadAll()
    }

    fun loadAll() {
        loadEarnings()
        loadTransactions()
        loadWallet()
        loadPayoutStatus()
        loadCompletedJobs()
    }

    fun loadEarnings() {
        viewModelScope.launch {
            _earningsSummary.value = UiState.Loading
            repository.getEarningsSummary()
                .onSuccess { _earningsSummary.value = UiState.Success(it) }
                .onFailure { _earningsSummary.value = UiState.Error(it.message ?: "Failed to load earnings") }
        }
    }

    fun loadTransactions() {
        viewModelScope.launch {
            _transactions.value = UiState.Loading
            repository.getTransactions()
                .onSuccess { _transactions.value = UiState.Success(it) }
                .onFailure { _transactions.value = UiState.Error(it.message ?: "Failed to load transactions") }
        }
    }

    fun loadWallet() {
        viewModelScope.launch {
            _walletInfo.value = UiState.Loading
            repository.getWalletInfo()
                .onSuccess { _walletInfo.value = UiState.Success(it) }
                .onFailure { _walletInfo.value = UiState.Error(it.message ?: "Failed to load wallet") }
        }
    }

    fun loadCompletedJobs() {
        viewModelScope.launch {
            _completedJobs.value = UiState.Loading
            jobRepository.getCompletedJobs()
                .onSuccess { jobs ->
                    _completedJobs.value = UiState.Success(
                        jobs.filter { it.isSettlementEligible }
                    )
                }
                .onFailure { _completedJobs.value = UiState.Error(it.message ?: "Failed to load recent trips") }
        }
    }

    fun requestWithdrawal(amount: Double) {
        viewModelScope.launch {
            _withdrawalState.value = UiState.Loading
            repository.requestWithdrawal(amount)
                .onSuccess {
                    _withdrawalState.value = UiState.Success(it)
                    loadWallet() // Refresh wallet
                    loadTransactions() // Refresh transactions
                }
                .onFailure { _withdrawalState.value = UiState.Error(it.message ?: "Withdrawal failed") }
        }
    }

    fun loadPayoutStatus() {
        viewModelScope.launch {
            _payoutStatus.value = UiState.Loading
            repository.getPayoutStatus()
                .onSuccess { _payoutStatus.value = UiState.Success(it) }
                .onFailure { _payoutStatus.value = UiState.Error(it.message ?: "Failed to load payout status") }
        }
    }

    fun createPayoutOnboardingLink() {
        viewModelScope.launch {
            _onboardingLink.value = UiState.Loading
            repository.createPayoutOnboardingLink()
                .onSuccess { _onboardingLink.value = UiState.Success(it) }
                .onFailure { _onboardingLink.value = UiState.Error(it.message ?: "Failed to start payout setup") }
        }
    }

    fun setSelectedPeriod(period: EarningsPeriod) {
        _selectedPeriod.value = period
    }
}

enum class EarningsPeriod(val displayName: String) {
    TODAY("Today"),
    THIS_WEEK("This Week"),
    THIS_MONTH("This Month")
}

data class EarningsDashboardUiModel(
    val selectedPeriod: EarningsPeriod,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val displayBalance: Double? = null,
    val weeklyAmount: Double? = null,
    val deliveriesCount: Int? = null,
    val activeHours: Double? = null,
    val weekRangeLabel: String = "No weekly data",
    val chart: List<EarningsChartPoint>,
    val trips: List<EarningsTripUi>
)

data class EarningsChartPoint(
    val dayLabel: String,
    val amount: Double,
    val isHighlighted: Boolean
)

data class EarningsTripUi(
    val title: String,
    val date: String,
    val miles: String,
    val amount: Double,
    val iconType: String
)

private fun buildDashboardUi(
    period: EarningsPeriod,
    earningsState: UiState<EarningsSummary>,
    transactionsState: UiState<List<Transaction>>,
    walletState: UiState<WalletInfo>,
    completedJobsState: UiState<List<DeliveryJob>>
): EarningsDashboardUiModel {
    val summary = (earningsState as? UiState.Success)?.data
    val transactions = (transactionsState as? UiState.Success)?.data.orEmpty()
    val wallet = (walletState as? UiState.Success)?.data
    val completedJobs = (completedJobsState as? UiState.Success)?.data.orEmpty()

    val chart = buildWeeklyChart(transactions, completedJobs)
    val weeklyAmount = summary?.weeklyEarnings
        ?.takeIf { it > 0.0 }
        ?: chart.sumOf { it.amount }.takeIf { it > 0.0 }
    val displayBalance = when (period) {
        EarningsPeriod.TODAY -> summary?.todayEarnings
        EarningsPeriod.THIS_WEEK -> wallet?.balance
        EarningsPeriod.THIS_MONTH -> summary?.monthlyEarnings
    }

    val loading = earningsState is UiState.Loading ||
        transactionsState is UiState.Loading ||
        walletState is UiState.Loading ||
        completedJobsState is UiState.Loading

    val errorMessage = listOfNotNull(
        (earningsState as? UiState.Error)?.message,
        (transactionsState as? UiState.Error)?.message,
        (walletState as? UiState.Error)?.message,
        (completedJobsState as? UiState.Error)?.message
    ).firstOrNull()

    return EarningsDashboardUiModel(
        selectedPeriod = period,
        isLoading = loading,
        errorMessage = errorMessage,
        displayBalance = displayBalance,
        weeklyAmount = weeklyAmount,
        deliveriesCount = when (period) {
            EarningsPeriod.TODAY -> summary?.todayDeliveries
            EarningsPeriod.THIS_WEEK -> summary?.totalDeliveries
            EarningsPeriod.THIS_MONTH -> summary?.totalDeliveries
        },
        activeHours = summary?.onlineHours,
        weekRangeLabel = buildWeekRangeLabel(chart),
        chart = chart,
        trips = buildRecentTrips(completedJobs, transactions)
    )
}

private fun buildRecentTrips(
    completedJobs: List<DeliveryJob>,
    transactions: List<Transaction>
): List<EarningsTripUi> {
    val transactionAmountsByJobId = transactions
        .filter { it.jobId != null }
        .associateBy({ it.jobId!! }, { abs(it.amount) })

    return completedJobs
        .sortedByDescending { job ->
            job.deliveredAt ?: job.completedAt ?: job.createdAt ?: ""
        }
        .take(3)
        .map { job ->
            val amount = transactionAmountsByJobId[job.id]
                ?.takeIf { it > 0.0 }
                ?: job.estimatedEarnings
            EarningsTripUi(
                title = buildTripTitle(job),
                date = formatTripDate(job.deliveredAt ?: job.completedAt ?: job.createdAt),
                miles = formatMiles(job.distance),
                amount = amount,
                iconType = when {
                    job.pickup.address.contains("warehouse", ignoreCase = true) -> "warehouse"
                    job.pickup.address.contains("store", ignoreCase = true) ||
                        job.dropoff.address.contains("store", ignoreCase = true) -> "store"
                    else -> "truck"
                }
            )
        }
}

private fun buildTripTitle(job: DeliveryJob): String {
    val pickup = job.pickup.shortAddress.ifBlank { job.pickup.address }
    val dropoff = job.dropoff.shortAddress.ifBlank { job.dropoff.address }
    return when {
        pickup.isNotBlank() -> pickup
        dropoff.isNotBlank() -> dropoff
        job.customerName.isNotBlank() -> "${job.customerName} Delivery"
        else -> "Completed Delivery"
    }
}

private fun buildWeeklyChart(
    transactions: List<Transaction>,
    completedJobs: List<DeliveryJob>
): List<EarningsChartPoint> {
    val today = SimpleDate.parse(Clock.System.now().toString().take(10))
    val dates = (0..6).map { offset ->
        today.plusDays(offset - 6)
    }

    val amountsByDate = mutableMapOf<String, Double>()
    transactions.forEach { transaction ->
        if (transaction.type == TransactionType.WITHDRAWAL) return@forEach
        val dateKey = transaction.timestamp?.take(10) ?: return@forEach
        amountsByDate[dateKey] = (amountsByDate[dateKey] ?: 0.0) + abs(transaction.amount)
    }

    if (amountsByDate.values.none { it > 0.0 }) {
        completedJobs.forEach { job ->
            val dateKey = (job.deliveredAt ?: job.completedAt ?: job.createdAt)?.take(10) ?: return@forEach
            amountsByDate[dateKey] = (amountsByDate[dateKey] ?: 0.0) + job.estimatedEarnings
        }
    }

    return dates.map { date ->
        EarningsChartPoint(
            dayLabel = date.dayLabel,
            amount = amountsByDate[date.isoDate] ?: 0.0,
            isHighlighted = date.isoDate == today.isoDate
        )
    }
}

private fun defaultWeeklyChart(): List<EarningsChartPoint> {
    val today = SimpleDate.parse(Clock.System.now().toString().take(10))
    return (0..6).map { offset ->
        val date = today.plusDays(offset - 6)
        EarningsChartPoint(
            dayLabel = date.dayLabel,
            amount = 0.0,
            isHighlighted = date.isoDate == today.isoDate
        )
    }
}

private fun buildWeekRangeLabel(chartDates: List<EarningsChartPoint>): String {
    val today = SimpleDate.parse(Clock.System.now().toString().take(10))
    val start = today.plusDays(-6)
    return "${start.monthLabel} ${start.dayOfMonth} - ${today.monthLabel} ${today.dayOfMonth}, ${today.year}"
}

private fun formatTripDate(timestamp: String?): String {
    val date = timestamp?.take(10)?.let(SimpleDate::parse) ?: return "Unknown date"
    return "${date.monthLabel} ${date.dayOfMonth},\n${date.year}"
}

private fun formatMiles(distance: Double): String {
    if (distance <= 0.0) return "--"
    val rounded = round(distance * 10.0) / 10.0
    return if (rounded % 1.0 == 0.0) rounded.toInt().toString() else rounded.toString()
}

private data class SimpleDate(
    val year: Int,
    val month: Int,
    val dayOfMonth: Int
) {
    val isoDate: String = "${year.toString().padStart(4, '0')}-${month.toString().padStart(2, '0')}-${dayOfMonth.toString().padStart(2, '0')}"
    val monthLabel: String = MONTHS[month - 1]
    val dayLabel: String = DAYS[dayOfWeekIndex]

    private val dayOfWeekIndex: Int
        get() {
            val adjustment = (14 - month) / 12
            val y = year - adjustment
            val m = month + 12 * adjustment - 2
            return (dayOfMonth + y + y / 4 - y / 100 + y / 400 + (31 * m) / 12) % 7
        }

    fun plusDays(days: Int): SimpleDate {
        if (days == 0) return this
        var y = year
        var m = month
        var d = dayOfMonth + days

        while (d > daysInMonth(y, m)) {
            d -= daysInMonth(y, m)
            m += 1
            if (m > 12) {
                m = 1
                y += 1
            }
        }

        while (d <= 0) {
            m -= 1
            if (m <= 0) {
                m = 12
                y -= 1
            }
            d += daysInMonth(y, m)
        }

        return SimpleDate(y, m, d)
    }

    companion object {
        fun parse(isoDate: String): SimpleDate {
            val parts = isoDate.split("-")
            return if (parts.size == 3) {
                SimpleDate(
                    year = parts[0].toIntOrNull() ?: 1970,
                    month = parts[1].toIntOrNull() ?: 1,
                    dayOfMonth = parts[2].toIntOrNull() ?: 1
                )
            } else {
                SimpleDate(1970, 1, 1)
            }
        }

        private fun daysInMonth(year: Int, month: Int): Int = when (month) {
            1, 3, 5, 7, 8, 10, 12 -> 31
            4, 6, 9, 11 -> 30
            2 -> if (isLeapYear(year)) 29 else 28
            else -> 30
        }

        private fun isLeapYear(year: Int): Boolean {
            return (year % 4 == 0 && year % 100 != 0) || year % 400 == 0
        }

        private val MONTHS = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        private val DAYS = listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")
    }
}
