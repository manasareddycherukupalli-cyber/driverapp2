package com.company.carryon.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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

actual class ImagePickerLauncher(
    private val onImagePicked: (ByteArray) -> Unit
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
                image?.let {
                    val data: NSData? = UIImageJPEGRepresentation(it, 0.8)
                    data?.let { nsData ->
                        val bytes = nsData.bytes?.readBytes(nsData.length.toInt()) ?: ByteArray(0)
                        onImagePicked(bytes)
                    }
                }
                picker.dismissViewControllerAnimated(true, completion = null)
                launcher.currentDelegate = null  // Release delegate after dismissal
            }

            override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
                picker.dismissViewControllerAnimated(true, completion = null)
                launcher.currentDelegate = null  // Release delegate after dismissal
            }
        }

        currentDelegate = delegate  // Retain the delegate to prevent GC
        picker.delegate = delegate

        UIApplication.sharedApplication.keyWindow
            ?.rootViewController
            ?.presentViewController(picker, animated = true, completion = null)
    }
}

@Composable
actual fun rememberImagePickerLauncher(
    onImagePicked: (ByteArray) -> Unit
): ImagePickerLauncher {
    return remember { ImagePickerLauncher(onImagePicked) }
}
