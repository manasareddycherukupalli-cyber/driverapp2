package com.company.carryon.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readBytes
import platform.Foundation.NSData
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerCameraDevice
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerEditedImage
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.darwin.NSObject

private const val MaxUploadImageBytes = 900 * 1024

actual class ImagePickerLauncher(
    private val onImagePicked: (ByteArray) -> Unit,
    private val onImagePickFailed: (String) -> Unit
) {
    // IMPORTANT: Retain delegate as a class property to prevent deallocation
    // while the UIImagePickerController is still presented.
    // Without this, the delegate gets garbage collected and causes SIGABRT crash.
    private var currentDelegate: NSObject? = null

    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    actual fun launch() {
        val picker = UIImagePickerController()
        val cameraSource = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
        val librarySource = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary
        val hasCamera = UIImagePickerController.isSourceTypeAvailable(cameraSource)
        val hasLibrary = UIImagePickerController.isSourceTypeAvailable(librarySource)
        if (!hasCamera && !hasLibrary) {
            onImagePickFailed("Camera is unavailable on this device.")
            return
        }
        picker.sourceType = if (hasCamera) cameraSource else librarySource
        if (hasCamera) {
            picker.cameraDevice = UIImagePickerControllerCameraDevice.UIImagePickerControllerCameraDeviceFront
        }

        val launcher = this
        val delegate = object : NSObject(),
            UIImagePickerControllerDelegateProtocol,
            UINavigationControllerDelegateProtocol {

            override fun imagePickerController(
                picker: UIImagePickerController,
                didFinishPickingMediaWithInfo: Map<Any?, *>
            ) {
                @Suppress("UNCHECKED_CAST")
                val image = (didFinishPickingMediaWithInfo[UIImagePickerControllerEditedImage] as? UIImage)
                    ?: (didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage)
                if (image == null) {
                    onImagePickFailed("No photo was captured. Please try again.")
                } else {
                    val data: NSData? = image.jpegDataUnderLimit()
                    if (data == null || data.length.toInt() <= 0) {
                        onImagePickFailed("Could not prepare the captured photo. Please try again.")
                    } else {
                        val nsData = data
                        val bytes = nsData.bytes?.readBytes(nsData.length.toInt()) ?: ByteArray(0)
                        if (bytes.isEmpty()) {
                            onImagePickFailed("Could not prepare the captured photo. Please try again.")
                        } else {
                            onImagePicked(bytes)
                        }
                    }
                }
                picker.dismissViewControllerAnimated(true, completion = null)
                launcher.currentDelegate = null  // Release delegate after dismissal
            }

            override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
                onImagePickFailed("No photo was captured. Please try again.")
                picker.dismissViewControllerAnimated(true, completion = null)
                launcher.currentDelegate = null  // Release delegate after dismissal
            }
        }

        currentDelegate = delegate  // Retain the delegate to prevent GC
        picker.delegate = delegate

        val rootViewController = UIApplication.sharedApplication.keyWindow?.rootViewController
        if (rootViewController == null) {
            onImagePickFailed("Could not open the camera. Please try again.")
            launcher.currentDelegate = null
            return
        }
        rootViewController.presentViewController(picker, animated = true, completion = null)
    }
}

@Composable
actual fun rememberImagePickerLauncher(
    onImagePickFailed: (String) -> Unit,
    onImagePicked: (ByteArray) -> Unit
): ImagePickerLauncher {
    val latestOnImagePicked = rememberUpdatedState(onImagePicked)
    val latestOnImagePickFailed = rememberUpdatedState(onImagePickFailed)
    return remember {
        ImagePickerLauncher(
            onImagePicked = { bytes -> latestOnImagePicked.value(bytes) },
            onImagePickFailed = { message -> latestOnImagePickFailed.value(message) }
        )
    }
}

private fun UIImage.jpegDataUnderLimit(): NSData? {
    var quality = 0.8
    var data = UIImageJPEGRepresentation(this, quality)
    while (data != null && data.length > MaxUploadImageBytes.toULong() && quality > 0.35) {
        quality -= 0.1
        data = UIImageJPEGRepresentation(this, quality)
    }
    println("[image-picker] ios uploadBytes=${data?.length ?: 0uL}")
    return data
}
