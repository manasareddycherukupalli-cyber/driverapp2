package com.company.carryon.presentation.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.math.max
import kotlin.math.roundToInt

private const val MaxUploadImageBytes = 900 * 1024
private const val MaxUploadImageDimensionPx = 1280
private const val ImagePickerLogTag = "[image-picker]"

actual class ImagePickerLauncher(
    private val onLaunch: () -> Unit
) {
    actual fun launch() {
        onLaunch()
    }
}

@Composable
actual fun rememberImagePickerLauncher(
    onImagePickFailed: (String) -> Unit,
    onImagePicked: (ByteArray) -> Unit
): ImagePickerLauncher {
    val context = LocalContext.current
    val latestOnImagePicked = rememberUpdatedState(onImagePicked)
    val latestOnImagePickFailed = rememberUpdatedState(onImagePickFailed)
    val pendingCaptureUri = remember { mutableStateOf<Uri?>(null) }
    val captureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { saved ->
        val uri = pendingCaptureUri.value
        println("$ImagePickerLogTag TakePicture result saved=$saved")
        if (!saved || uri == null) {
            pendingCaptureUri.value = null
            latestOnImagePickFailed.value("No photo was saved. Please retake and confirm the photo.")
            return@rememberLauncherForActivityResult
        }
        try {
            val bytes = context.contentResolver.openInputStream(uri)?.use { stream ->
                val originalBytes = stream.readBytes()
                originalBytes.compressedForUpload()
            }
            println("$ImagePickerLogTag captured uploadBytes=${bytes?.size ?: 0}")
            if (bytes == null || bytes.isEmpty()) {
                latestOnImagePickFailed.value("No photo was saved. Please retake and confirm the photo.")
            } else {
                latestOnImagePicked.value(bytes)
            }
        } catch (error: Exception) {
            println("$ImagePickerLogTag read captured photo failed: ${error.message}")
            latestOnImagePickFailed.value("Could not read the captured photo. Please try again.")
        } finally {
            pendingCaptureUri.value = null
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        println("$ImagePickerLogTag camera permission granted=$granted")
        if (granted) {
            launchCameraCapture(
                context = context,
                setPendingUri = { pendingCaptureUri.value = it },
                launch = { captureLauncher.launch(it) },
                onFailure = { latestOnImagePickFailed.value(it) }
            )
        } else {
            latestOnImagePickFailed.value("Camera permission is required to capture proof of delivery.")
        }
    }

    return remember(context, captureLauncher, permissionLauncher) {
        ImagePickerLauncher {
            val hasCameraPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED

            if (hasCameraPermission) {
                launchCameraCapture(
                    context = context,
                    setPendingUri = { pendingCaptureUri.value = it },
                    launch = { captureLauncher.launch(it) },
                    onFailure = { latestOnImagePickFailed.value(it) }
                )
            } else {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }
}

private fun launchCameraCapture(
    context: Context,
    setPendingUri: (Uri?) -> Unit,
    launch: (Uri) -> Unit,
    onFailure: (String) -> Unit
) {
    val uri = try {
        context.createCameraImageUri()
    } catch (error: Exception) {
        println("$ImagePickerLogTag create uri failed: ${error.message}")
        onFailure("Could not prepare the camera. Please try again.")
        return
    }
    println("$ImagePickerLogTag created capture uri=$uri")
    setPendingUri(uri)
    try {
        launch(uri)
    } catch (error: Exception) {
        println("$ImagePickerLogTag launch camera failed: ${error.message}")
        setPendingUri(null)
        onFailure("Could not open the camera. Please try again.")
    }
}

private fun Context.createCameraImageUri(): Uri {
    val imageFile = File.createTempFile(
        "carryon_proof_photo_",
        ".jpg",
        cacheDir
    )
    val authority = "$packageName.fileprovider"
    return FileProvider.getUriForFile(this, authority, imageFile)
}

private fun ByteArray.compressedForUpload(): ByteArray {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(this, 0, size, bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
        println("$ImagePickerLogTag compression skipped undecodable originalBytes=$size")
        return this
    }

    val largestBound = max(bounds.outWidth, bounds.outHeight)
    var sampleSize = 1
    while (largestBound / sampleSize > MaxUploadImageDimensionPx) {
        sampleSize *= 2
    }

    val bitmap = BitmapFactory.decodeByteArray(
        this,
        0,
        size,
        BitmapFactory.Options().apply { inSampleSize = sampleSize }
    ) ?: return this

    return try {
        val compressed = compressBitmapUnderLimit(bitmap)
        println("$ImagePickerLogTag compressed originalBytes=$size uploadBytes=${compressed.size}")
        compressed
    } finally {
        bitmap.recycle()
    }
}

private fun compressBitmapUnderLimit(source: Bitmap): ByteArray {
    var working = scaleBitmapToMaxDimension(source, MaxUploadImageDimensionPx)
    var shouldRecycleWorking = working !== source

    try {
        var quality = 85
        while (quality >= 45) {
            val bytes = working.toJpegBytes(quality)
            if (bytes.size <= MaxUploadImageBytes) {
                return bytes
            }
            quality -= 10
        }

        repeat(6) {
            val nextWidth = (working.width * 0.85f).roundToInt().coerceAtLeast(640)
            val nextHeight = (working.height * 0.85f).roundToInt().coerceAtLeast(640)
            if (nextWidth == working.width && nextHeight == working.height) {
                return working.toJpegBytes(45)
            }

            val scaled = Bitmap.createScaledBitmap(working, nextWidth, nextHeight, true)
            if (shouldRecycleWorking) working.recycle()
            working = scaled
            shouldRecycleWorking = true

            quality = 75
            while (quality >= 45) {
                val bytes = working.toJpegBytes(quality)
                if (bytes.size <= MaxUploadImageBytes) {
                    return bytes
                }
                quality -= 10
            }
        }

        return working.toJpegBytes(45)
    } finally {
        if (shouldRecycleWorking) working.recycle()
    }
}

private fun scaleBitmapToMaxDimension(bitmap: Bitmap, maxDimension: Int): Bitmap {
    val largestSide = max(bitmap.width, bitmap.height)
    if (largestSide <= maxDimension) return bitmap

    val scale = maxDimension.toFloat() / largestSide.toFloat()
    val targetWidth = (bitmap.width * scale).roundToInt().coerceAtLeast(1)
    val targetHeight = (bitmap.height * scale).roundToInt().coerceAtLeast(1)
    return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
}

private fun Bitmap.toJpegBytes(quality: Int): ByteArray =
    ByteArrayOutputStream().use { output ->
        compress(Bitmap.CompressFormat.JPEG, quality.coerceIn(1, 100), output)
        output.toByteArray()
    }
