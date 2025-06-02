package com.ayudevices.neosynkparent.data.repository

import android.util.Log
import com.ayudevices.neosynkparent.data.model.MedicalReportResponse
import com.ayudevices.neosynkparent.data.model.MedicalReportUploadResponse
import com.ayudevices.neosynkparent.data.network.ApiService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MedicalReportRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun uploadMedicalReport(
        parentId: String,
        file: MultipartBody.Part
    ): Result<MedicalReportUploadResponse> {
        return try {
            Log.d("MEDICAL_UPLOAD", "Uploading file for parent_id: $parentId")

            val parentIdPart = RequestBody.create("text/plain".toMediaTypeOrNull(), parentId)
            val response = apiService.uploadMedicalReport(parentIdPart, file)

            Log.d("MEDICAL_UPLOAD", "Upload response: $response")

            if (response.isSuccessful) {
                response.body()?.let { uploadResponse: MedicalReportUploadResponse ->
                    Result.success<MedicalReportUploadResponse>(uploadResponse)
                } ?: Result.failure<MedicalReportUploadResponse>(Exception("Empty response body"))
            } else {
                Result.failure<MedicalReportUploadResponse>(Exception("Upload Error: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("MEDICAL_UPLOAD", "Upload failed", e)
            Result.failure<MedicalReportUploadResponse>(e)
        }
    }

    suspend fun getMedicalReports(parentId: String): Result<List<MedicalReportResponse>> {
        return try {
            Log.d("MEDICAL_REPORTS", "Fetching reports for parent_id: $parentId")
            val response = apiService.getMedicalReports(parentId)
            Log.d("MEDICAL_REPORTS", "Reports response: $response")

            if (response.isSuccessful) {
                response.body()?.let { reports ->
                    Result.success(reports)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                Result.failure(Exception("API Error: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("MEDICAL_REPORTS", "Failed to fetch reports", e)
            Result.failure(e)
        }
    }
}