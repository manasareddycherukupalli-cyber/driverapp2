package com.company.carryon.data.network

import com.company.carryon.data.model.DocumentType

fun buildDriverDocumentObjectPath(
    driverId: String,
    type: DocumentType,
    timestampMillis: Long,
    extension: String = "jpg"
): String {
    val normalizedExt = extension.lowercase().trim().ifBlank { "jpg" }
    return "${driverId.trim()}/${type.name}_${timestampMillis}.$normalizedExt"
}

fun buildDriverDocumentStoragePath(objectPath: String): String {
    return "driver-documents/$objectPath"
}
