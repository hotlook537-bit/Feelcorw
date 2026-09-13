package com.example.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

class ImageKitService(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()
) {
    private val authEndpoint = "https://lulugram-imagekit-auth.hotlook537.workers.dev/"
    private val uploadEndpoint = "https://upload.imagekit.io/api/v1/files/upload"

    data class AuthParameters(
        val token: String,
        val expire: Long,
        val signature: String,
        val publicKey: String
    )

    suspend fun fetchAuthParameters(): AuthParameters = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(authEndpoint)
            .header("User-Agent", "Lulugram/1.0 (Android)")
            .get()
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw ImageKitAuthException("Auth endpoint failed with code: ${response.code}")
                }
                val body = response.body?.string() ?: throw ImageKitAuthException("Empty auth response")
                val json = JSONObject(body)
                AuthParameters(
                    token = json.getString("token"),
                    expire = json.getLong("expire"),
                    signature = json.getString("signature"),
                    publicKey = json.optString("publicKey", "public_K1n0YXUAgzhLrt+zb/5UQhCus9o=")
                )
            }
        } catch (e: Exception) {
            throw ImageKitAuthException("Failed to fetch ImageKit auth params: ${e.message}", e)
        }
    }

    suspend fun uploadMedia(
        file: File,
        fileName: String,
        folder: String = "/uploads",
        tags: List<String> = emptyList()
    ): String = withContext(Dispatchers.IO) {
        val auth = fetchAuthParameters()

        val requestBodyBuilder = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", fileName, file.readBytes().toRequestBody("application/octet-stream".toMediaTypeOrNull()))
            .addFormDataPart("fileName", fileName)
            .addFormDataPart("publicKey", auth.publicKey)
            .addFormDataPart("token", auth.token)
            .addFormDataPart("expire", auth.expire.toString())
            .addFormDataPart("signature", auth.signature)
            .addFormDataPart("folder", folder)
            .addFormDataPart("useUniqueFileName", "true")

        if (tags.isNotEmpty()) {
            requestBodyBuilder.addFormDataPart("tags", tags.joinToString(","))
        }

        val request = Request.Builder()
            .url(uploadEndpoint)
            .header("User-Agent", "Lulugram/1.0 (Android)")
            .post(requestBodyBuilder.build())
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val respBody = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    throw IOException("Upload failed with HTTP ${response.code}: $respBody")
                }
                val json = JSONObject(respBody)
                json.getString("url")
            }
        } catch (e: Exception) {
            throw IOException("Failed to upload to ImageKit: ${e.message}", e)
        }
    }

    suspend fun uploadBytes(
        bytes: ByteArray,
        fileName: String,
        mimeType: String,
        folder: String = "/uploads"
    ): String = withContext(Dispatchers.IO) {
        val auth = fetchAuthParameters()

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", fileName, bytes.toRequestBody(mimeType.toMediaTypeOrNull()))
            .addFormDataPart("fileName", fileName)
            .addFormDataPart("publicKey", auth.publicKey)
            .addFormDataPart("token", auth.token)
            .addFormDataPart("expire", auth.expire.toString())
            .addFormDataPart("signature", auth.signature)
            .addFormDataPart("folder", folder)
            .addFormDataPart("useUniqueFileName", "true")
            .build()

        val request = Request.Builder()
            .url(uploadEndpoint)
            .header("User-Agent", "Lulugram/1.0 (Android)")
            .post(requestBody)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val respBody = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    throw IOException("Upload failed with HTTP ${response.code}: $respBody")
                }
                val json = JSONObject(respBody)
                json.getString("url")
            }
        } catch (e: Exception) {
            throw IOException("Failed to upload bytes to ImageKit: ${e.message}", e)
        }
    }
}
