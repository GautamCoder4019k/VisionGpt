package com.example.visiongpt.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

interface ApiService {
    @POST("image")
    suspend fun uploadImage(
        @Body request: ImageUploadRequest
    ): Response<ResponseBody>
}

data class ImageUploadRequest(
    val text: String,
    val image: String // Base64 encoded image
)

fun encodeImageToBase64(imageFile: File): String {
    val inputStream = FileInputStream(imageFile)
    val bytes = inputStream.readBytes()
    inputStream.close()
    return Base64.encodeToString(bytes, Base64.DEFAULT)
}