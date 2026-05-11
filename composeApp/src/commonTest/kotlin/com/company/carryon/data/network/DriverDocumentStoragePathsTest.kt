package com.company.carryon.data.network

import com.company.carryon.data.model.DocumentType
import kotlin.test.Test
import kotlin.test.assertEquals

class DriverDocumentStoragePathsTest {
    @Test
    fun objectPathUsesDriverIdAsOwnerSegment() {
        val objectPath = buildDriverDocumentObjectPath(
            driverId = "driver-123",
            type = DocumentType.MYKAD_BACK,
            timestampMillis = 1778400603816,
            extension = "JPG"
        )

        assertEquals("driver-123/MYKAD_BACK_1778400603816.jpg", objectPath)
    }

    @Test
    fun storagePathIncludesBucketPrefixForBackendMetadata() {
        assertEquals(
            "driver-documents/driver-123/SELFIE_1778400953808.jpg",
            buildDriverDocumentStoragePath("driver-123/SELFIE_1778400953808.jpg")
        )
    }
}
