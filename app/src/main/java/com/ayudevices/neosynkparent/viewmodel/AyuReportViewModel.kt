package com.ayudevices.neosynkparent.viewmodel

import android.app.DownloadManager
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ayudevices.neosynkparent.data.model.AyuReportResponse
import com.ayudevices.neosynkparent.data.repository.AyuReportRepository
import com.ayudevices.neosynkparent.ui.screen.dashboard.AyuReport
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class AyuReportViewModel @Inject constructor(
    private val ayuReportRepository: AyuReportRepository
) : ViewModel() {

    private val _ayuReports = MutableLiveData<List<AyuReport>>()
    val ayuReports: LiveData<List<AyuReport>> = _ayuReports

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _downloadStatus = MutableLiveData<DownloadStatus>()
    val downloadStatus: LiveData<DownloadStatus> = _downloadStatus

    // Removed init block - no automatic API call

    fun loadAyuReports() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            FirebaseAuth.getInstance().currentUser?.uid?.let {
                ayuReportRepository.getAllAyuReports(it)
                    .onSuccess { response ->
                        val report = AyuReport(
                            id = "1",
                            name = generateReportName(response),
                            generatedDate = getLatestDate(response),
                            reportType = response.intent.replaceFirstChar {
                                if (it.isLowerCase()) it.titlecase() else it.toString()
                            },
                            downloadUrl = "", // Not needed since we generate PDF locally
                            responseData = response
                        )
                        _ayuReports.value = listOf(report)
                    }
                    .onFailure { exception ->
                        _error.value = exception.message
                    }
            } ?: run {
                _error.value = "User not authenticated"
            }

            _isLoading.value = false
        }
    }

    fun downloadPdf(report: AyuReport, context: Context) {
        viewModelScope.launch {
            _downloadStatus.value = DownloadStatus.Loading(report.id)

            try {
                // Always generate PDF locally since no direct URL is available
                generateAndDownloadPdf(report, context)
                _downloadStatus.value = DownloadStatus.Success(report.id)
            } catch (e: Exception) {
                _downloadStatus.value = DownloadStatus.Error(report.id, e.message ?: "Download failed")
            }
        }
    }

    private suspend fun generateAndDownloadPdf(report: AyuReport, context: Context) {
        withContext(Dispatchers.IO) {
            // Generate PDF from AyuReportResponse data
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
            val page = pdfDocument.startPage(pageInfo)

            drawReportContent(page.canvas, report.responseData!!)

            pdfDocument.finishPage(page)

            // Save PDF to downloads folder
            val fileName = "${report.name}.pdf"
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileName)

            try {
                pdfDocument.writeTo(FileOutputStream(file))

                // Notify user of successful download
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "PDF downloaded to Downloads folder", Toast.LENGTH_SHORT).show()
                }
            } finally {
                pdfDocument.close()
            }
        }
    }

    private fun drawReportContent(canvas: Canvas, response: AyuReportResponse) {
        val paint = Paint().apply {
            color = Color.BLACK
            textSize = 14f
        }

        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
        }

        var yPosition = 50f
        val margin = 50f

        // Title
        canvas.drawText("Ayu Health Report", margin, yPosition, titlePaint)
        yPosition += 40f

        // Age
        canvas.drawText("Age: ${response.response_text.Age} years", margin, yPosition, paint)
        yPosition += 30f

        // Vitals Section
        canvas.drawText("VITALS", margin, yPosition, titlePaint)
        yPosition += 30f

        // Height
        response.response_text.Vitals.height_cm.lastOrNull()?.let {
            canvas.drawText("Height: ${it.value} cm (${it.date})", margin + 20f, yPosition, paint)
            yPosition += 25f
        }

        // Weight
        response.response_text.Vitals.weight_kg.lastOrNull()?.let {
            canvas.drawText("Weight: ${it.value} kg (${it.date})", margin + 20f, yPosition, paint)
            yPosition += 25f
        }

        // Heart Rate
        response.response_text.Vitals.heart_rate.lastOrNull()?.let {
            canvas.drawText("Heart Rate: ${it.value} bpm (${it.date})", margin + 20f, yPosition, paint)
            yPosition += 25f
        }

        // SpO2
        response.response_text.Vitals.spo2.lastOrNull()?.let {
            canvas.drawText("SpO2: ${it.value}% (${it.date})", margin + 20f, yPosition, paint)
            yPosition += 35f
        }

        // Milestones Section
        canvas.drawText("DEVELOPMENTAL MILESTONES", margin, yPosition, titlePaint)
        yPosition += 30f

        val milestones = response.response_text.Milestones
        canvas.drawText("Motor: ${milestones.Completion_Percentage.Motor}%", margin + 20f, yPosition, paint)
        yPosition += 25f
        canvas.drawText("Sensory: ${milestones.Completion_Percentage.Sensory}%", margin + 20f, yPosition, paint)
        yPosition += 25f
        canvas.drawText("Cognitive: ${milestones.Completion_Percentage.Cognitive}%", margin + 20f, yPosition, paint)
        yPosition += 25f
        canvas.drawText("Feeding: ${milestones.Completion_Percentage.Feeding}%", margin + 20f, yPosition, paint)
        yPosition += 35f

        // Health Events
        if (response.response_text.Health_Events.isNotEmpty()) {
            canvas.drawText("HEALTH EVENTS", margin, yPosition, titlePaint)
            yPosition += 30f

            response.response_text.Health_Events.forEach { event ->
                canvas.drawText("Date: ${event.date}", margin + 20f, yPosition, paint)
                yPosition += 25f
                canvas.drawText("Condition: ${event.condition}", margin + 20f, yPosition, paint)
                yPosition += 25f
                canvas.drawText("Symptoms: ${event.symptoms}", margin + 20f, yPosition, paint)
                yPosition += 25f
                canvas.drawText("Diagnosis: ${event.diagnosis_summary}", margin + 20f, yPosition, paint)
                yPosition += 35f
            }
        }
    }

    private fun generateReportName(response: AyuReportResponse): String {
        return when (response.intent.lowercase()) {
            "health_analysis" -> "Health Analysis Report"
            "milestone_tracking" -> "Developmental Milestones Report"
            "vitals_report" -> "Vitals Monitoring Report"
            else -> "Ayu Health Report"
        }
    }

    private fun getLatestDate(response: AyuReportResponse): String {
        val dates = mutableListOf<String>()

        // Collect all dates from vitals
        response.response_text.Vitals.height_cm.forEach { dates.add(it.date) }
        response.response_text.Vitals.weight_kg.forEach { dates.add(it.date) }
        response.response_text.Vitals.heart_rate.forEach { dates.add(it.date) }
        response.response_text.Vitals.spo2.forEach { dates.add(it.date) }

        // Add health event dates
        response.response_text.Health_Events.forEach { dates.add(it.date) }

        // Return the latest date or current date
        return dates.maxOrNull() ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    fun clearDownloadStatus() {
        _downloadStatus.value = DownloadStatus.Idle
    }
}

// Download status sealed class
sealed class DownloadStatus {
    object Idle : DownloadStatus()
    data class Loading(val reportId: String) : DownloadStatus()
    data class Success(val reportId: String) : DownloadStatus()
    data class Error(val reportId: String, val message: String) : DownloadStatus()
}