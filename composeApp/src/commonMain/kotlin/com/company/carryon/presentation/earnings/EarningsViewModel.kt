package com.company.carryon.presentation.earnings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.company.carryon.data.model.*
import com.company.carryon.di.ServiceLocator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * EarningsViewModel — Manages earnings dashboard, wallet, and transaction data.
 */
class EarningsViewModel : ViewModel() {

    private val repository = ServiceLocator.earningsRepository

    // Earnings summary
    private val _earningsSummary = MutableStateFlow<UiState<EarningsSummary>>(UiState.Idle)
    val earningsSummary: StateFlow<UiState<EarningsSummary>> = _earningsSummary.asStateFlow()

    // Transaction history
    private val _transactions = MutableStateFlow<UiState<List<Transaction>>>(UiState.Idle)
    val transactions: StateFlow<UiState<List<Transaction>>> = _transactions.asStateFlow()

    // Wallet info
    private val _walletInfo = MutableStateFlow<UiState<WalletInfo>>(UiState.Idle)
    val walletInfo: StateFlow<UiState<WalletInfo>> = _walletInfo.asStateFlow()

    // Withdrawal state
    private val _withdrawalState = MutableStateFlow<UiState<Transaction>>(UiState.Idle)
    val withdrawalState: StateFlow<UiState<Transaction>> = _withdrawalState.asStateFlow()

    // Selected time period
    private val _selectedPeriod = MutableStateFlow(EarningsPeriod.TODAY)
    val selectedPeriod: StateFlow<EarningsPeriod> = _selectedPeriod.asStateFlow()

    init {
        loadAll()
    }

    fun loadAll() {
        loadEarnings()
        loadTransactions()
        loadWallet()
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

    fun setSelectedPeriod(period: EarningsPeriod) {
        _selectedPeriod.value = period
    }
}

enum class EarningsPeriod(val displayName: String) {
    TODAY("Today"),
    THIS_WEEK("This Week"),
    THIS_MONTH("This Month")
}
