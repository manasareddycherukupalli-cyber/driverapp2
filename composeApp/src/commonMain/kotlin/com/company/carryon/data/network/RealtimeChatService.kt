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

/**
 * Listens to Supabase Realtime Postgres changes on the SupportMessage table.
 * When a new message is inserted for the subscribed ticket, emits a signal
 * so the UI can refresh messages via the REST API.
 */
object RealtimeChatService {

    private val _newMessageSignal = MutableSharedFlow<String>(extraBufferCapacity = 1)
    /** Emits the ticketId whenever a new message arrives */
    val newMessageSignal: SharedFlow<String> = _newMessageSignal

    private var channel: RealtimeChannel? = null
    private var collectJob: Job? = null
    private var currentTicketId: String? = null

    suspend fun startListening(ticketId: String, scope: CoroutineScope) {
        if (channel != null && currentTicketId == ticketId) return
        if (channel != null) stopListening()

        currentTicketId = ticketId
        val supabase = SupabaseConfig.client
        val ch = supabase.channel("support-chat-$ticketId")

        val inserts = ch.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
            table = "SupportMessage"
        }

        ch.subscribe()
        channel = ch

        collectJob = scope.launch {
            inserts.collect {
                _newMessageSignal.emit(ticketId)
            }
        }
    }

    suspend fun stopListening() {
        collectJob?.cancel()
        collectJob = null
        channel?.let {
            SupabaseConfig.client.realtime.removeChannel(it)
        }
        channel = null
        currentTicketId = null
    }
}
