package com.example.drive_app.presentation.util

import androidx.compose.runtime.Composable

/**
 * Platform-specific image picker launcher.
 * Call [launch] to open the system image picker.
 */
expect class ImagePickerLauncher {
    fun launch()
}

/**
 * Returns a remembered [ImagePickerLauncher] that calls [onImagePicked]
 * with the raw JPEG bytes of the selected image.
 */
@Composable
expect fun rememberImagePickerLauncher(
    onImagePicked: (ByteArray) -> Unit
): ImagePickerLauncher
