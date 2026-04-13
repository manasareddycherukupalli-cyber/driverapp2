package com.company.carryon.data.network

import com.company.carryon.data.model.DeliveryJob
import com.company.carryon.di.ServiceLocator
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.decodeRecordOrNull
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

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
    private var subscribedDriverId: String? = null

    suspend fun startListening(driverId: String, scope: CoroutineScope) {
        if (channel != null && subscribedDriverId == driverId) return
        if (channel != null) {
            stopListening()
        }

        val supabase = SupabaseConfig.client
        val ch = supabase.channel("incoming-jobs-$driverId")

        val inserts = ch.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
            table = "Booking"
        }

        val updates = ch.postgresChangeFlow<PostgresAction.Update>(schema = "public") {
            table = "Booking"
        }

        val notificationInserts = ch.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
            table = "DriverNotification"
            filter("driverId", FilterOperator.EQ, driverId)
        }

        ch.subscribe()
        channel = ch
        subscribedDriverId = driverId

        collectJob = scope.launch {
            launch {
                inserts.collect { action ->
                    val record = action.decodeRecordOrNull<BookingRealtimeRecord>() ?: return@collect
                    if (record.status == "SEARCHING_DRIVER") {
                        fetchAndEmitIncomingJobs()
                    }
                }
            }
            launch {
                updates.collect { action ->
                    val record = action.decodeRecordOrNull<BookingRealtimeRecord>() ?: return@collect
                    if (record.status == "SEARCHING_DRIVER") {
                        fetchAndEmitIncomingJobs()
                    }
                }
            }
            launch {
                notificationInserts.collect { action ->
                    val record = action.decodeRecordOrNull<DriverNotificationRealtimeRecord>() ?: return@collect
                    if (record.type == "JOB_REQUEST") {
                        fetchAndEmitIncomingJobs()
                    }
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
        subscribedDriverId = null
    }

    private suspend fun fetchAndEmitIncomingJobs() {
        ServiceLocator.jobRepository.getIncomingRequests()
            .onSuccess { jobs ->
                jobs.forEach { job ->
                    _incomingJobs.emit(job)
                }
            }
    }
}

@Serializable
private data class BookingRealtimeRecord(
    val status: String? = null
)

@Serializable
private data class DriverNotificationRealtimeRecord(
    val type: String? = null
)
