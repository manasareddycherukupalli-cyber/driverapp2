package com.company.carryon.data.network

fun mapUploadErrorMessage(error: Throwable, fallback: String = "Upload failed. Please try again."): String {
    val text = listOfNotNull(error.message, error.cause?.message)
        .joinToString(" ")

    return when {
        text.contains("No image file provided", ignoreCase = true) ->
            "No photo was attached. Please retake the photo and try again."

        text.contains("Only image files are allowed", ignoreCase = true) ||
            text.contains("File is not a valid image", ignoreCase = true) ->
            "This photo could not be read as an image. Please retake it and try again."

        text.contains("Image must be 10MB or smaller", ignoreCase = true) ->
            "Photo is too large. Please retake the photo and try again."

        text.contains("row-level security", ignoreCase = true) ||
            text.contains("storage/v1/object", ignoreCase = true) ||
            text.contains("Headers:", ignoreCase = true) ||
            text.contains("Authorization=", ignoreCase = true) ||
            text.contains("apikey=", ignoreCase = true) ->
            "We could not upload this image because your secure document storage is not ready. Please try again."

        text.contains("413", ignoreCase = true) ||
            text.contains("payload too large", ignoreCase = true) ||
            text.contains("request entity too large", ignoreCase = true) ->
            "Photo is too large. Please retake the photo and try again."

        text.contains("timeout", ignoreCase = true) ||
            text.contains("connection", ignoreCase = true) ||
            text.contains("network", ignoreCase = true) ->
            "Photo upload connection failed. Check your network and try again."

        else -> fallback
    }
}
