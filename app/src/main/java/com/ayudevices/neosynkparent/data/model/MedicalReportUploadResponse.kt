package com.ayudevices.neosynkparent.data.model

data class MedicalReportUploadResponse(
    val message: String,
    val url: String
)


sealed class UploadStatus {
    object Idle : UploadStatus()
    object Loading : UploadStatus()
    data class Success(val message: String) : UploadStatus()
    data class Error(val message: String) : UploadStatus()
}

