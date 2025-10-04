package com.example.visiongpt.data

import android.content.ContentValues.TAG
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody
import java.io.File
import javax.inject.Inject

class ImageRepository @Inject constructor(
    private val client: OkHttpClient,
    private val gson: Gson // Inject Gson for JSON parsing
) {

    suspend fun uploadImageAndGetText(imageFile: File, text: String): String {
        // Create multipart request body
        val imageBase64 = encodeImageToBase64(imageFile)

        // Create JSON object
        val imageTextRequest = ImageTextRequest(atext = text, image = imageBase64)
        val json = gson.toJson(imageTextRequest)

        // Create request body
        val requestBody = json.toRequestBody("application/json".toMediaTypeOrNull())
        Log.d(TAG, "uploadImageAndGetText: ${json}")
        // Create request
        val request = Request.Builder()
            .url("https://40c4-34-124-135-20.ngrok-free.app/req")
            .post(requestBody)
            .build()

        // Execute request and handle response
        val response = client.newCall(request).execute()
        val responseBody = response.body?.string()


        if (!response.isSuccessful || responseBody.isNullOrEmpty()) {
            throw Exception("Failed to upload image and text: ${response.message}")
        }

        return try {
            // Try to parse the response as JSON
            val textResponse = gson.fromJson(responseBody, TextResponse::class.java)
            textResponse.text
        } catch (e: JsonSyntaxException) {
            // If parsing fails, assume the response is a plain string
            responseBody
        }
    }
}


data class ImageTextRequest(
    val atext: String,
    val image: String="",
)


data class TextResponse(val text: String)

