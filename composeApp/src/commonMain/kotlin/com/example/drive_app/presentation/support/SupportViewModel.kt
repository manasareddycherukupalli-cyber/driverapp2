package com.example.drive_app.presentation.support

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.drive_app.data.model.*
import com.example.drive_app.di.ServiceLocator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * SupportViewModel — Manages help center, support tickets, and chat.
 */
class SupportViewModel : ViewModel() {

    private val repository = ServiceLocator.supportRepository

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
            val message = ChatMessage(
                senderId = "DRV_001",
                message = messageText,
                isFromDriver = true
            )
            repository.sendMessage(ticketId, message)
                .onSuccess { loadMessages(ticketId) }
        }
    }

    fun triggerSos() {
        viewModelScope.launch {
            _sosState.value = UiState.Loading
            repository.triggerSos(12.9716, 77.5946) // Current location
                .onSuccess { _sosState.value = UiState.Success(true) }
                .onFailure { _sosState.value = UiState.Error(it.message ?: "SOS failed") }
        }
    }
}
