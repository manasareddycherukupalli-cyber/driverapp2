package com.company.carryon.data.network

import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

object BookingRealtimeChatService {
    private val _newMessageSignal = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val newMessageSignal: SharedFlow<String> = _newMessageSignal

    private var channel: RealtimeChannel? = null
    private var collectJob: Job? = null
    private var currentBookingId: String? = null

    suspend fun startListening(bookingId: String, scope: CoroutineScope) {
        if (channel != null && currentBookingId == bookingId) return
        if (channel != null) stopListening()

        currentBookingId = bookingId
        val channelInstance = SupabaseConfig.client.channel("chat-$bookingId")
        val inserts = channelInstance.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
            table = "ChatMessage"
        }

        channelInstance.subscribe()
        channel = channelInstance

        collectJob = scope.launch {
            inserts.collect {
                _newMessageSignal.emit(bookingId)
            }
        }
    }

    suspend fun stopListening() {
        collectJob?.cancel()
        collectJob = null
        channel?.let { SupabaseConfig.client.realtime.removeChannel(it) }
        channel = null
        currentBookingId = null
    }
}
