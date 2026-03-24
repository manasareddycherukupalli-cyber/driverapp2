package com.company.carryon.presentation.support

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.company.carryon.data.model.*
import com.company.carryon.data.network.RealtimeChatService
import com.company.carryon.data.network.getLastKnownLocation
import com.company.carryon.di.ServiceLocator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * SupportViewModel — Manages help center, support tickets, and chat.
 */
class SupportViewModel : ViewModel() {

    private val repository = ServiceLocator.supportRepository
    private val authRepository = ServiceLocator.authRepository

    private val currentDriver: StateFlow<Driver?> = authRepository.currentDriver
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Transient error for toast
    private val _toastError = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val toastError: SharedFlow<String> = _toastError.asSharedFlow()

    // Help articles
    private val _helpArticles = MutableStateFlow<UiState<List<HelpArticle>>>(UiState.Idle)
    val helpArticles: StateFlow<UiState<List<HelpArticle>>> = _helpArticles.asStateFlow()

    // Tickets
    private val _tickets = MutableStateFlow<UiState<List<SupportTicket>>>(UiState.Idle)
    val tickets: StateFlow<UiState<List<SupportTicket>>> = _tickets.asStateFlow()

    // Chat messages
    private val _messages = MutableStateFlow<UiState<List<ChatMessage>>>(UiState.Idle)
    val messages: StateFlow<UiState<List<ChatMessage>>> = _messages.asStateFlow()

    // Ticket creation state
    private val _createTicketState = MutableStateFlow<UiState<SupportTicket>>(UiState.Idle)
    val createTicketState: StateFlow<UiState<SupportTicket>> = _createTicketState.asStateFlow()

    // SOS state
    private val _sosState = MutableStateFlow<UiState<Boolean>>(UiState.Idle)
    val sosState: StateFlow<UiState<Boolean>> = _sosState.asStateFlow()

    init {
        loadHelpArticles()
        loadTickets()
    }

    fun loadHelpArticles() {
        viewModelScope.launch {
            _helpArticles.value = UiState.Loading
            repository.getHelpArticles()
                .onSuccess { _helpArticles.value = UiState.Success(it) }
                .onFailure { _helpArticles.value = UiState.Error(it.message ?: "Failed to load") }
        }
    }

    fun loadTickets() {
        viewModelScope.launch {
            _tickets.value = UiState.Loading
            repository.getTickets()
                .onSuccess { _tickets.value = UiState.Success(it) }
                .onFailure { _tickets.value = UiState.Error(it.message ?: "Failed to load") }
        }
    }

    fun loadMessages(ticketId: String) {
        viewModelScope.launch {
            _messages.value = UiState.Loading
            repository.getMessages(ticketId)
                .onSuccess { _messages.value = UiState.Success(it) }
                .onFailure { _messages.value = UiState.Error(it.message ?: "Failed to load") }
        }
    }

    /** Subscribe to realtime updates for a ticket's chat messages */
    fun startRealtimeChat(ticketId: String) {
        viewModelScope.launch {
            RealtimeChatService.startListening(ticketId, viewModelScope)
        }
        // Collect realtime signals and refresh messages
        viewModelScope.launch {
            RealtimeChatService.newMessageSignal.collect { signalTicketId ->
                if (signalTicketId == ticketId) {
                    repository.getMessages(ticketId)
                        .onSuccess { _messages.value = UiState.Success(it) }
                }
            }
        }
    }

    fun stopRealtimeChat() {
        viewModelScope.launch {
            RealtimeChatService.stopListening()
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopRealtimeChat()
    }

    fun createTicket(subject: String, category: TicketCategory, description: String) {
        viewModelScope.launch {
            _createTicketState.value = UiState.Loading
            val ticket = SupportTicket(
                subject = subject,
                category = category,
                description = description
            )
            repository.createTicket(ticket)
                .onSuccess {
                    _createTicketState.value = UiState.Success(it)
                    loadTickets()
                }
                .onFailure { _createTicketState.value = UiState.Error(it.message ?: "Failed to create") }
        }
    }

    fun sendMessage(ticketId: String, messageText: String) {
        viewModelScope.launch {
            val driverId = currentDriver.value?.id ?: "unknown"
            val message = ChatMessage(
                senderId = driverId,
                message = messageText,
                isFromDriver = true
            )
            repository.sendMessage(ticketId, message)
                .onSuccess { loadMessages(ticketId) }
                .onFailure { _toastError.tryEmit(it.message ?: "Failed to send message") }
        }
    }

    fun triggerSos() {
        viewModelScope.launch {
            _sosState.value = UiState.Loading
            val location = getLastKnownLocation()
            val lat = location?.first ?: 0.0
            val lng = location?.second ?: 0.0
            repository.triggerSos(lat, lng)
                .onSuccess { _sosState.value = UiState.Success(true) }
                .onFailure { _sosState.value = UiState.Error(it.message ?: "SOS failed") }
        }
    }
}
