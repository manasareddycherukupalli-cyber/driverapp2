package com.company.carryon.presentation.auth

import com.company.carryon.data.network.ApiException
import com.company.carryon.data.network.AuthenticationException
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AuthSessionFailurePolicyTest {
    @Test
    fun backendUnauthorizedFailureClearsStoredAuth() {
        assertTrue(
            shouldClearStoredAuthAfterSessionSyncFailure(AuthenticationException())
        )
    }

    @Test
    fun transientSyncFailureDoesNotClearStoredAuth() {
        assertFalse(
            shouldClearStoredAuthAfterSessionSyncFailure(Exception("Request timeout"))
        )
    }

    @Test
    fun serverSyncFailureDoesNotClearStoredAuth() {
        assertFalse(
            shouldClearStoredAuthAfterSessionSyncFailure(
                ApiException(statusCode = 500, message = "Request failed")
            )
        )
    }
}
