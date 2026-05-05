package com.company.carryon.presentation.util

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
 * with upload-ready JPEG bytes of the selected image, or [onImagePickFailed]
 * when camera permission, capture, or image decoding fails.
 */
@Composable
expect fun rememberImagePickerLauncher(
    onImagePickFailed: (String) -> Unit = {},
    onImagePicked: (ByteArray) -> Unit
): ImagePickerLauncher
