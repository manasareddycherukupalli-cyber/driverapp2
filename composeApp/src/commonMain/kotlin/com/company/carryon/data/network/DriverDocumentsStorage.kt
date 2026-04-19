package com.company.carryon.data.network

import com.company.carryon.data.model.DocumentType
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
        val objectPath = "driver-documents/$driverId/${type.name}_${timestamp}.$normalizedExt"
        val bucket = SupabaseConfig.client.storage.from("driver-documents")
        bucket.upload(objectPath, bytes) {
            upsert = true
        }
        bucket.publicUrl(objectPath)
    }
}
