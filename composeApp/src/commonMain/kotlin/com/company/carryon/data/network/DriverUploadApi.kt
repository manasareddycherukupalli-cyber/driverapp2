package com.company.carryon.data.network

import io.ktor.client.call.body
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable

@Serializable
private data class DriverUploadResponse(
    val success: Boolean = false,
    val data: DriverUploadData? = null
)

@Serializable
private data class DriverUploadData(
    val url: String = ""
)

@Serializable
private data class ExtraChargeProofResponse(
    val success: Boolean = false,
    val data: ExtraChargeProofData? = null
)

@Serializable
private data class ExtraChargeProofData(
    val proofPath: String = ""
)

object DriverUploadApi {
    private val client get() = HttpClientFactory.client
    private val retryDelaysMillis = listOf(600L, 1_500L)

    suspend fun uploadProofImage(imageBytes: ByteArray): Result<String> = runCatching {
        uploadProofImageWithRetry(imageBytes)
    }

    /** Upload extra-charge receipt proof to the dedicated endpoint. Returns the object path. */
    suspend fun uploadExtraChargeProof(jobId: String, imageBytes: ByteArray): Result<String> = runCatching {
        var lastError: Throwable? = null
        repeat(retryDelaysMillis.size + 1) { attempt ->
            try {
                return@runCatching uploadExtraChargeProofOnce(jobId, imageBytes)
            } catch (error: Throwable) {
                if (error is CancellationException) throw error
                lastError = error
                if (!error.isTransientUploadFailure() || attempt == retryDelaysMillis.size) {
                    throw error.toDriverUploadException()
                }
                println("[driver-upload] transient extra-charge proof upload failure, retrying attempt ${attempt + 2}")
                delay(retryDelaysMillis[attempt])
            }
        }
        throw (lastError ?: Exception("Upload failed"))
    }

    private suspend fun uploadExtraChargeProofOnce(jobId: String, imageBytes: ByteArray): String {
        val response = client.submitFormWithBinaryData(
            url = "/api/driver/jobs/$jobId/extra-charges/upload-proof",
            formData = formData {
                append("proof", imageBytes, Headers.build {
                    append(HttpHeaders.ContentType, "image/jpeg")
                    append(HttpHeaders.ContentDisposition, "filename=\"extra-charge-receipt.jpg\"")
                })
            }
        ) {
            withAuth()
        }.body<ExtraChargeProofResponse>()

        return response.data?.proofPath?.takeIf { it.isNotBlank() }
            ?: throw Exception("Upload failed: no proof path returned")
    }

    private suspend fun uploadProofImageWithRetry(imageBytes: ByteArray): String {
        var lastError: Throwable? = null
        repeat(retryDelaysMillis.size + 1) { attempt ->
            try {
                return uploadProofImageOnce(imageBytes)
            } catch (error: Throwable) {
                if (error is CancellationException) throw error
                lastError = error
                if (!error.isTransientUploadFailure() || attempt == retryDelaysMillis.size) {
                    throw error.toDriverUploadException()
                }
                println("[driver-upload] transient upload failure, retrying attempt ${attempt + 2}")
                delay(retryDelaysMillis[attempt])
            }
        }
        throw (lastError ?: Exception("Upload failed"))
    }

    private suspend fun uploadProofImageOnce(imageBytes: ByteArray): String {
        val response = client.submitFormWithBinaryData(
            url = "/api/driver/upload/package-image",
            formData = formData {
                append("image", imageBytes, Headers.build {
                    append(HttpHeaders.ContentType, "image/jpeg")
                    append(HttpHeaders.ContentDisposition, "filename=\"proof-of-delivery.jpg\"")
                })
            }
        ) {
            withAuth()
        }.body<DriverUploadResponse>()

        return response.data?.url?.takeIf { it.isNotBlank() }
            ?: throw Exception("Upload failed: no proof photo URL returned")
    }

    private fun Throwable.isTransientUploadFailure(): Boolean {
        val text = listOfNotNull(message, cause?.message)
            .joinToString(" ")
            .lowercase()
        return text.contains("software caused connection abort") ||
            text.contains("connection reset") ||
            text.contains("broken pipe") ||
            text.contains("socket closed") ||
            text.contains("connection closed") ||
            text.contains("network is unreachable") ||
            text.contains("timed out")
    }

    private fun Throwable.toDriverUploadException(): Exception {
        if (isUploadTooLargeFailure()) {
            return Exception(
                "Photo is too large. Please retake the photo and try again.",
                this
            )
        }
        if (!isTransientUploadFailure()) {
            return this as? Exception ?: Exception(message ?: "Failed to upload photo", this)
        }
        return Exception(
            "Photo upload connection failed. Check your network and tap the photo area to retry.",
            this
        )
    }

    private fun Throwable.isUploadTooLargeFailure(): Boolean {
        val text = listOfNotNull(message, cause?.message)
            .joinToString(" ")
            .lowercase()
        return text.contains("413") ||
            text.contains("payload too large") ||
            text.contains("request entity too large")
    }
}
