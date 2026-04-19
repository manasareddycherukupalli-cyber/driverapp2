package com.company.carryon.data.network

import com.company.carryon.data.model.DocumentType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.storage.storage
import kotlin.time.Clock

class DriverDocumentsStorage {
    suspend fun uploadDocument(
        driverId: String,
        type: DocumentType,
        bytes: ByteArray,
        extension: String = "jpg"
    ): Result<String> = runCatching {
        val timestamp = Clock.System.now().toEpochMilliseconds()
        val normalizedExt = extension.lowercase().ifBlank { "jpg" }
        val session = SupabaseConfig.client.auth.currentSessionOrNull()
        val ownerSegment = session?.user?.id
            ?: session?.user?.email
                ?.lowercase()
                ?.replace("@", "_")
                ?.replace(".", "_")
            ?: driverId
        val objectPath = "drivers/$ownerSegment/${type.name}_${timestamp}.$normalizedExt"
        val bucket = SupabaseConfig.client.storage.from("driver-documents")
        bucket.upload(objectPath, bytes)
        bucket.publicUrl(objectPath)
    }
}
