package com.company.carryon.data.network

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
     * Gets the current access token through the auth session module.
     * Request builders should not know about Supabase session/storage fallbacks.
     */
    fun getCurrentAccessToken(): String? {
        return AuthSessionManager.currentAccessToken()
    }

    val client: HttpClient by lazy {
        HttpClient {
            install(ContentNegotiation) {
                json(json)
            }
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        // Redact Authorization headers and tokens from logs
                        val redacted = message
                            .replace(Regex("(?i)(Authorization:\\s*)\\S+"), "$1[REDACTED]")
                            .replace(Regex("(?i)(Bearer\\s+)\\S+"), "$1[REDACTED]")
                        println("[HTTP] $redacted")
                    }
                }
                level = LogLevel.INFO
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
                    if (response.status.value == 401) {
                        throw AuthenticationException("Authentication required. Please log in again.")
                    }
                    if (response.status.value >= 400) {
                        val body = response.bodyAsText()
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
    if (token.isNullOrBlank()) {
        throw AuthenticationException("Authentication required. Please log in again.")
    }
    headers.append("Authorization", "Bearer $token")
}
