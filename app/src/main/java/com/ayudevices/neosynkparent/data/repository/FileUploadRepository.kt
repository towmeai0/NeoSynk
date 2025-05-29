package com.ayudevices.neosynkparent.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class FileUploadRepository {

    private val client = OkHttpClient()

    suspend fun uploadFile(
        uri: Uri,
        context: Context,
        uploadUrl: String,
        onProgress: (Float) -> Unit = {},
        onSuccess: (String) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        withContext(Dispatchers.IO) {
            try {
                // Get file name
                val fileName = getFileName(uri, context) ?: "uploaded_file"

                // Create temporary file
                val tempFile = createTempFile(uri, context, fileName)

                if (tempFile != null) {
                    // Create request body
                    val requestBody = MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart(
                            "file",
                            fileName,
                            tempFile.asRequestBody("application/octet-stream".toMediaType())
                        )
                        .build()

                    // Create request
                    val request = Request.Builder()
                        .url(uploadUrl)
                        .post(requestBody)
                        .build()

                    // Execute request
                    client.newCall(request).execute().use { response ->
                        if (response.isSuccessful) {
                            withContext(Dispatchers.Main) {
                                onSuccess(response.body?.string() ?: "Upload successful")
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                onError("Upload failed: ${response.code}")
                            }
                        }
                    }

                    // Clean up temp file
                    tempFile.delete()
                } else {
                    withContext(Dispatchers.Main) {
                        onError("Failed to create temporary file")
                    }
                }
            } catch (e: Exception) {
                Log.e("FileUpload", "Upload error", e)
                withContext(Dispatchers.Main) {
                    onError("Upload error: ${e.message}")
                }
            }
        }
    }

    private fun getFileName(uri: Uri, context: Context): String? {
        var fileName: String? = null
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val displayNameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (displayNameIndex != -1) {
                    fileName = it.getString(displayNameIndex)
                }
            }
        }
        return fileName ?: uri.lastPathSegment
    }

    private fun createTempFile(uri: Uri, context: Context, fileName: String): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val tempFile = File(context.cacheDir, fileName)

            inputStream?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }
            tempFile
        } catch (e: Exception) {
            Log.e("FileUpload", "Error creating temp file", e)
            null
        }
    }
}