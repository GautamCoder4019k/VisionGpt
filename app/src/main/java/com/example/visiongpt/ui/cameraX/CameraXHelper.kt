package com.example.visiongpt.ui.cameraX

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.os.Environment
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale

class CameraXHelper(private val context: Context) {
    private var imageCapture: ImageCapture? = null

    fun startCamera(lifecycleOwner: LifecycleOwner, onImageCaptured: (File) -> Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build()

            imageCapture =
                ImageCapture.Builder().setTargetResolution(android.util.Size(1280, 720)).build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )

                takePhoto(onImageCaptured)
            } catch (exc: Exception) {
                Log.e("CameraXHelper", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    private fun takePhoto(onImageCaptured: (File) -> Unit) {
        val photoFile = File.createTempFile(
            "temp.png", ".png", context.getExternalFilesDir(Environment.DIRECTORY_DCIM)
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture?.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e("CameraXHelper", "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val rotatedFile = correctImageOrientation(photoFile)
                    onImageCaptured(rotatedFile)
                }
            }
        )
    }

    private fun correctImageOrientation(imageFile: File): File {
        try {
            val exif = ExifInterface(imageFile.absolutePath)
            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
            val rotatedBitmap = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(bitmap, 90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(bitmap, 180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(bitmap, 270f)
                else -> bitmap
            }

            // Save the rotated bitmap back to the file
            val fileOutputStream = FileOutputStream(imageFile)
            rotatedBitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()
        } catch (e: IOException) {
            Log.e("CameraXHelper", "Failed to correct image orientation", e)
        }
        return imageFile
    }

    private fun rotateImage(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}
