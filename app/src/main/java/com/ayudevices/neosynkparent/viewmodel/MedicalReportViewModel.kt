package com.ayudevices.neosynkparent.viewmodel

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ayudevices.neosynkparent.data.model.MedicalReportResponse
import com.ayudevices.neosynkparent.data.model.UploadStatus
import com.ayudevices.neosynkparent.data.repository.MedicalReportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

@HiltViewModel
class MedicalReportViewModel @Inject constructor(
    private val medicalReportRepository: MedicalReportRepository
) : ViewModel() {

    private val _medicalReports = MutableLiveData<List<MedicalReportResponse>>(emptyList())
    val medicalReports: LiveData<List<MedicalReportResponse>> = _medicalReports

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val _uploadStatus = MutableLiveData<UploadStatus>(UploadStatus.Idle)
    val uploadStatus: LiveData<UploadStatus> = _uploadStatus

    fun loadMedicalReports(parentId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            medicalReportRepository.getMedicalReports(parentId)
                .onSuccess { reports ->
                    _medicalReports.value = reports
                    _isLoading.value = false
                }
                .onFailure { exception ->
                    _error.value = exception.message
                    _isLoading.value = false
                    Log.e("MedicalReportVM", "Failed to load reports: ${exception.message}")
                }
        }
    }

    fun uploadMedicalReport(
        parentId: String,
        uri: Uri,
        context: Context
    ) {
        viewModelScope.launch {
            _uploadStatus.value = UploadStatus.Loading

            try {
                // Create multipart file from URI
                val contentResolver = context.contentResolver
                val inputStream = contentResolver.openInputStream(uri)

                if (inputStream == null) {
                    _uploadStatus.value = UploadStatus.Error("Failed to read file")
                    return@launch
                }

                // Get filename
                val fileName = getFileNameFromUri(uri, context) ?: "unknown_file"

                // Create RequestBody from InputStream
                val requestBody = inputStream.readBytes().toRequestBody("*/*".toMediaTypeOrNull())
                val filePart = MultipartBody.Part.createFormData("file", fileName, requestBody)

                // Upload file
                medicalReportRepository.uploadMedicalReport(parentId, filePart)
                    .onSuccess { uploadResponse ->
                        _uploadStatus.value = UploadStatus.Success(uploadResponse.message)
                        // Refresh the reports list after successful upload
                        loadMedicalReports(parentId)
                    }
                    .onFailure { exception ->
                        _uploadStatus.value = UploadStatus.Error(exception.message ?: "Upload failed")
                        Log.e("MedicalReportVM", "Upload failed: ${exception.message}")
                    }

            } catch (e: Exception) {
                _uploadStatus.value = UploadStatus.Error("Failed to process file: ${e.message}")
                Log.e("MedicalReportVM", "File processing failed", e)
            }
        }
    }

    private fun getFileNameFromUri(uri: Uri, context: Context): String? {
        var fileName: String? = null
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use { c ->
            if (c.moveToFirst()) {
                val displayNameIndex = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (displayNameIndex != -1) {
                    fileName = c.getString(displayNameIndex)
                }
            }
        }
        return fileName ?: uri.lastPathSegment
    }

    fun clearUploadStatus() {
        _uploadStatus.value = UploadStatus.Idle
    }

    fun clearError() {
        _error.value = null
    }
}