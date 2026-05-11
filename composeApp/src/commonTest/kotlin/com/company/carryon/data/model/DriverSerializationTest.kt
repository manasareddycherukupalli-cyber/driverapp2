package com.company.carryon.data.model

import com.company.carryon.data.network.HttpClientFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DriverSerializationTest {
    @Test
    fun authResponseTreatsEmptyNullableEnumsAsMissing() {
        val response = HttpClientFactory.json.decodeFromString<AuthResponse>(
            """
            {
              "success": true,
              "driver": {
                "id": "driver-1",
                "email": "driver@example.com",
                "nationality": "",
                "licenseClass": "",
                "state": "",
                "vehicle": {
                  "id": "vehicle-1",
                  "ownership": "",
                  "insuranceCoverageType": ""
                }
              },
              "isNewDriver": true
            }
            """.trimIndent()
        )

        val driver = response.driver
        assertEquals("driver-1", driver?.id)
        assertNull(driver?.nationality)
        assertNull(driver?.licenseClass)
        assertNull(driver?.state)
        assertNull(driver?.vehicleDetails?.ownership)
        assertNull(driver?.vehicleDetails?.insuranceCoverageType)
    }
}
