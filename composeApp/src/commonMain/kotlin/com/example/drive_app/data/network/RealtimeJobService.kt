package com.example.drive_app.data.network

import com.example.drive_app.data.model.DeliveryJob
import com.example.drive_app.di.ServiceLocator
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
 * Listens to Supabase Realtime Postgres changes on the Booking table.
 * When a booking enters SEARCHING_DRIVER status, fetches the full job
 * details via the existing API and emits it on [incomingJobs].
 */
object RealtimeJobService {

    private val _incomingJobs = MutableSharedFlow<DeliveryJob>(extraBufferCapacity = 1)
    val incomingJobs: SharedFlow<DeliveryJob> = _incomingJobs

    private var channel: RealtimeChannel? = null
    private var collectJob: Job? = null

    suspend fun startListening(scope: CoroutineScope) {
        if (channel != null) return // already listening

        val supabase = SupabaseConfig.client
        val ch = supabase.channel("incoming-jobs")

        // Listen for INSERTs on the Booking table
        val inserts = ch.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
            table = "Booking"
        }

        // Listen for UPDATEs on the Booking table (e.g. PENDING → SEARCHING_DRIVER)
        val updates = ch.postgresChangeFlow<PostgresAction.Update>(schema = "public") {
            table = "Booking"
        }

        ch.subscribe()
        channel = ch

        // On any matching change, fetch the latest incoming job via REST API.
        // This avoids parsing raw Supabase records and reuses the existing
        // toDeliveryJob() mapping on the backend.
        collectJob = scope.launch {
            launch {
                inserts.collect {
                    fetchAndEmitIncomingJob()
                }
            }
            launch {
                updates.collect {
                    fetchAndEmitIncomingJob()
                }
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
    }

    private suspend fun fetchAndEmitIncomingJob() {
        ServiceLocator.jobRepository.getIncomingRequest()
            .onSuccess { job ->
                if (job != null) {
                    _incomingJobs.emit(job)
                }
            }
    }
}
