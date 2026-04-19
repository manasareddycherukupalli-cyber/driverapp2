package com.company.carryon.presentation.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.carryon.data.model.BookingChatMessage
import com.company.carryon.data.network.BookingChatApi
import com.company.carryon.data.network.BookingRealtimeChatService
import com.company.carryon.presentation.components.DriveAppTopBar
import com.company.carryon.presentation.components.EmptyState
import com.company.carryon.presentation.navigation.AppNavigator
import com.company.carryon.presentation.theme.Gray400
import com.company.carryon.presentation.theme.Gray500
import com.company.carryon.presentation.theme.Gray700
import com.company.carryon.presentation.theme.Orange100
import com.company.carryon.presentation.theme.Orange500
import kotlinx.coroutines.launch

@Composable
fun CustomerChatScreen(navigator: AppNavigator) {
    val bookingId = navigator.selectedChatBookingId.orEmpty()
    val customerName = navigator.selectedChatCustomerName.ifBlank { "Customer" }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var messages by remember(bookingId) { mutableStateOf<List<BookingChatMessage>>(emptyList()) }
    var messageText by remember { mutableStateOf("") }
    var quickMessages by remember(bookingId) { mutableStateOf<List<String>>(emptyList()) }
    var showQuickMessages by remember { mutableStateOf(false) }

    LaunchedEffect(bookingId) {
        if (bookingId.isBlank()) return@LaunchedEffect

        BookingChatApi.getQuickMessages(bookingId).onSuccess { response ->
            quickMessages = response.data ?: emptyList()
        }
        BookingChatApi.getMessages(bookingId).onSuccess { response ->
            messages = response.data ?: emptyList()
        }
        BookingRealtimeChatService.startListening(bookingId, this)
    }

    LaunchedEffect(bookingId) {
        if (bookingId.isBlank()) return@LaunchedEffect

        BookingRealtimeChatService.newMessageSignal.collect { updatedBookingId ->
            if (updatedBookingId == bookingId) {
                BookingChatApi.getMessages(bookingId).onSuccess { response ->
                    messages = response.data ?: emptyList()
                }
            }
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    DisposableEffect(bookingId) {
        onDispose {
            scope.launch { BookingRealtimeChatService.stopListening() }
        }
    }

    Scaffold(
        topBar = {
            DriveAppTopBar(
                title = customerName,
                onBackClick = { navigator.goBack() }
            )
        },
        bottomBar = {
            if (bookingId.isNotBlank()) {
                Column(modifier = Modifier.background(Color.White)) {
                    if (showQuickMessages && quickMessages.isNotEmpty()) {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(quickMessages) { quickMessage ->
                                Box(
                                    modifier = Modifier
                                        .background(Orange100, RoundedCornerShape(16.dp))
                                        .clickable {
                                            messageText = quickMessage
                                            showQuickMessages = false
                                        }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(quickMessage, fontSize = 12.sp, color = Orange500)
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "...",
                            color = Orange500,
                            fontSize = 20.sp,
                            modifier = Modifier
                                .clickable { showQuickMessages = !showQuickMessages }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        )

                        OutlinedTextField(
                            value = messageText,
                            onValueChange = { messageText = it },
                            placeholder = { Text("Type a message...", fontSize = 14.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Orange500,
                                unfocusedBorderColor = Gray400,
                                focusedContainerColor = Color(0xFFF8F8F8),
                                unfocusedContainerColor = Color(0xFFF8F8F8)
                            ),
                            singleLine = true,
                            maxLines = 1
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                if (messageText.isBlank()) return@Button

                                val outgoingMessage = messageText.trim()
                                messageText = ""
                                scope.launch {
                                    BookingChatApi.sendMessage(bookingId, outgoingMessage).onSuccess { response ->
                                        response.data?.let { newMessage ->
                                            messages = messages + newMessage
                                        }
                                    }
                                }
                            },
                            shape = RoundedCornerShape(24.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Orange500)
                        ) {
                            Text("Send", fontSize = 14.sp)
                        }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        when {
            bookingId.isBlank() -> {
                EmptyState(
                    title = "Chat unavailable",
                    subtitle = "No booking is linked to this conversation yet.",
                    emoji = "💬"
                )
            }

            messages.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No messages yet", color = Gray700, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Send a message to start the conversation", color = Gray500, fontSize = 13.sp)
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    state = listState,
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messages) { message ->
                        BookingMessageBubble(message = message)
                    }
                }
            }
        }
    }
}

@Composable
private fun BookingMessageBubble(message: BookingChatMessage) {
    val isOwnMessage = message.senderType.equals("DRIVER", ignoreCase = true)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isOwnMessage) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.widthIn(max = 280.dp),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isOwnMessage) 16.dp else 4.dp,
                bottomEnd = if (isOwnMessage) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isOwnMessage) Orange500 else MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = message.message,
                    fontSize = 14.sp,
                    color = if (isOwnMessage) Color.White else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message.createdAt.takeLast(8).take(5).ifBlank { "--:--" },
                    fontSize = 10.sp,
                    color = if (isOwnMessage) Color.White.copy(alpha = 0.75f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
