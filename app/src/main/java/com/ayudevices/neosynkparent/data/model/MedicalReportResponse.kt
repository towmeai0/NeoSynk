package com.ayudevices.neosynkparent.data.model

data class MedicalReportResponse(
    val id: String,
    val parent_id: String,
    val filename: String,
    val s3_key: String,
    val s3_url: String,
    val upload_time: String
) {
    // Helper properties for UI compatibility
    val name: String get() = filename
    val uploadDate: String get() = upload_time.split("T")[0] // Extract date part
    val fileType: String get() = getFileTypeFromName(filename)
    val size: String get() = "" // API doesn't provide size, could be enhanced

    private fun getFileTypeFromName(fileName: String): String {
        return when {
            fileName.endsWith(".pdf", ignoreCase = true) -> "PDF"
            fileName.endsWith(".jpg", ignoreCase = true) ||
                    fileName.endsWith(".jpeg", ignoreCase = true) ||
                    fileName.endsWith(".png", ignoreCase = true) -> "Image"
            fileName.endsWith(".doc", ignoreCase = true) ||
                    fileName.endsWith(".docx", ignoreCase = true) -> "Document"
            else -> "File"
        }
    }
}

