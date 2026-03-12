package com.company.carryon.data.network

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object HttpClientFactory {
    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    val client: HttpClient by lazy {
        HttpClient {
            install(ContentNegotiation) {
                json(json)
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 30_000
                connectTimeoutMillis = 15_000
                socketTimeoutMillis = 30_000
            }
            defaultRequest {
                url(apiBaseUrl())
                getToken()?.let { headers.append("Authorization", "Bearer $it") }
            }
            HttpResponseValidator {
                validateResponse { response ->
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
