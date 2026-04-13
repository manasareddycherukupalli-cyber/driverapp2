package com.company.carryon.presentation.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File

actual class ImagePickerLauncher(
    private val launcher: ActivityResultLauncher<Unit>
) {
    actual fun launch() {
        launcher.launch(Unit)
    }
}

private class CaptureFromCameraContract : ActivityResultContract<Unit, Uri?>() {
    private var outputUri: Uri? = null

    override fun createIntent(context: Context, input: Unit): Intent {
        val imageFile = File.createTempFile(
            "carryon_profile_photo_",
            ".jpg",
            context.cacheDir
        )
        val authority = "${context.packageName}.fileprovider"
        outputUri = FileProvider.getUriForFile(context, authority, imageFile)

        return Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            // Hint supported camera apps to open selfie camera first.
            putExtra("android.intent.extra.USE_FRONT_CAMERA", true)
            putExtra("android.intent.extras.CAMERA_FACING", 1)
            putExtra("camerafacing", "front")
            putExtra("previous_mode", "front")
            putExtra(MediaStore.EXTRA_OUTPUT, outputUri)
            addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        if (resultCode != Activity.RESULT_OK) return null
        return outputUri
    }
}

@Composable
actual fun rememberImagePickerLauncher(
    onImagePicked: (ByteArray) -> Unit
): ImagePickerLauncher {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = CaptureFromCameraContract()
    ) { uri ->
        uri?.let {
            context.contentResolver.openInputStream(it)?.use { stream ->
                onImagePicked(stream.readBytes())
            }
        }
    }
    return remember { ImagePickerLauncher(launcher) }
}
