package com.company.carryon.data.network

import io.github.jan.supabase.auth.auth
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Thrown when the server returns 401 Unauthorized.
 * UI layers should catch this to redirect the user to the login screen.
 */
class AuthenticationException(message: String = "Authentication required") : Exception(message)

object HttpClientFactory {
    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true  // Required to serialize fields with default values (e.g., Document.type)
    }

    /**
     * Gets the current valid access token from Supabase.
     * Supabase SDK handles token refresh automatically.
     * Falls back to stored token if no active session.
     */
    fun getCurrentAccessToken(): String? {
        return try {
            var session = SupabaseConfig.client.auth.currentSessionOrNull()
            val token = session?.accessToken
            if (token != null) {
                println("[Auth] Got fresh token from Supabase session")
                // Keep stored token in sync with the latest session token
                saveToken(token)
                token
            } else {
                println("[Auth] No Supabase session, falling back to stored token")
                getToken()
            }
        } catch (e: Exception) {
            println("[Auth] Error getting Supabase session: ${e.message}, using stored token")
            getToken()
        }
    }

    val client: HttpClient by lazy {
        HttpClient {
            install(ContentNegotiation) {
                json(json)
            }
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        println("[HTTP] $message")
                    }
                }
                level = LogLevel.BODY
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 30_000
                connectTimeoutMillis = 15_000
                socketTimeoutMillis = 30_000
            }
            // Set base URL in defaultRequest, but NOT the token
            defaultRequest {
                url(apiBaseUrl())
            }
            HttpResponseValidator {
                validateResponse { response ->
                    println("[HTTP] Response status: ${response.status.value}")
                    if (response.status.value == 401) {
                        val body = response.bodyAsText()
                        println("[HTTP] 401 Unauthorized: $body")
                        // Soft-fail: don't clear token immediately.
                        // Callers should attempt a one-time session sync/recovery first.
                        throw AuthenticationException("Authentication required. Please log in again.")
                    }
                    if (response.status.value >= 400) {
                        val body = response.bodyAsText()
                        println("[HTTP] Error response body: $body")
                        val message = try {
                            json.parseToJsonElement(body).jsonObject["message"]?.jsonPrimitive?.content
                                ?: "Request failed"
                        } catch (_: Exception) {
                            "Request failed (${response.status.value})"
                        }
                        throw Exception(message)
                    }
                }
            }
        }
    }
}

/**
 * Extension function to add auth header with fresh token to any request.
 * Use this instead of relying on defaultRequest for auth.
 */
fun HttpRequestBuilder.withAuth() {
    val token = HttpClientFactory.getCurrentAccessToken()
    if (token != null) {
        println("[HTTP] Adding auth token: ${token.take(20)}...")
        headers.append("Authorization", "Bearer $token")
    } else {
        println("[HTTP] WARNING: No auth token available!")
    }
}
