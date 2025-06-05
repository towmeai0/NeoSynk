package com.ayudevices.neosynkparent.viewmodel

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.*
import com.ayudevices.neosynkparent.data.model.AyuReportResponse
import com.ayudevices.neosynkparent.data.repository.AyuReportRepository
import com.ayudevices.neosynkparent.ui.screen.dashboard.AyuReport
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

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

    fun loadAyuReports() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            FirebaseAuth.getInstance().currentUser?.uid?.let { userId ->
                ayuReportRepository.getAllAyuReports(userId)
                    .onSuccess { response ->
                        val report = AyuReport(
                            id = "1",
                            name = generateReportName(response),
                            generatedDate = getLatestDate(response),
                            reportType = response.intent.replaceFirstChar {
                                if (it.isLowerCase()) it.titlecase() else it.toString()
                            },
                            downloadUrl = "",
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
                val profileData = fetchProfileData()
                generateAndDownloadPdf(report, context, profileData)
                _downloadStatus.value = DownloadStatus.Success(report.id)
            } catch (e: Exception) {
                _downloadStatus.value = DownloadStatus.Error(report.id, e.message ?: "Download failed")
            }
        }
    }

    private suspend fun fetchProfileData(): Map<String, String> = withContext(Dispatchers.IO) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@withContext emptyMap()
        val ref = FirebaseDatabase.getInstance().getReference("NeoSynk").child("user").child(userId)

        suspendCoroutine { cont ->
            ref.get().addOnSuccessListener { snapshot ->
                val name = snapshot.child("name").getValue(String::class.java) ?: ""
                val location = snapshot.child("location").getValue(String::class.java) ?: ""
                val gender = snapshot.child("gender").getValue(String::class.java) ?: ""
                cont.resume(mapOf("name" to name, "location" to location, "gender" to gender))
            }.addOnFailureListener {
                cont.resume(emptyMap())
            }
        }
    }

    private suspend fun generateAndDownloadPdf(
        report: AyuReport,
        context: Context,
        profileData: Map<String, String>
    ) = withContext(Dispatchers.IO) {
        val pdfDocument = PdfDocument()

        val pageWidth = 595
        val pageHeight = 842
        val margin = 40f

        val textPaint = TextPaint().apply {
            color = Color.BLACK
            textSize = 14f
        }

        val titlePaint = TextPaint().apply {
            color = Color.BLACK
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
        }

        val headerFooterPaint = TextPaint().apply {
            color = Color.GRAY
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        }

        var y = 80f
        var pageNumber = 1
        lateinit var canvas: Canvas
        lateinit var page: PdfDocument.Page

        fun startNewPage() {
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            page = pdfDocument.startPage(pageInfo)
            canvas = page.canvas
            drawHeader(canvas, headerFooterPaint, margin, pageWidth)  // pass pageWidth here
            y = 80f
            pageNumber++
        }

        fun finishPage() {
            drawFooter(canvas, headerFooterPaint, margin, pageHeight)
            pdfDocument.finishPage(page)
        }

        fun drawWrappedText(text: String, paint: TextPaint = textPaint) {
            val maxWidth = pageWidth - 2 * margin
            val staticLayout = StaticLayout.Builder
                .obtain(text, 0, text.length, paint, maxWidth.toInt())
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(0f, 1f)
                .setIncludePad(false)
                .build()

            if (y + staticLayout.height > pageHeight - 60f) {
                finishPage()
                startNewPage()
            }

            canvas.save()
            canvas.translate(margin, y)
            staticLayout.draw(canvas)
            canvas.restore()

            y += staticLayout.height + 10f
        }

        fun drawParagraph(title: String, lines: List<String>) {
            drawWrappedText(title, titlePaint)
            lines.forEach { line ->
                drawWrappedText("• $line", textPaint)
            }
            y += 10f
        }

        fun drawHeader(canvas: Canvas, paint: TextPaint, margin: Float) {
            // Left side title
            canvas.drawText("NeoSynk Android App", margin, 40f, paint)
            canvas.drawLine(margin, 45f, pageWidth - margin, 45f, paint)

            // Draw current date at top right
            val currentDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
            val dateText = "Generated: $currentDate"
            val textWidth = paint.measureText(dateText)
            canvas.drawText(dateText, pageWidth - margin - textWidth, 40f, paint)
        }

        fun drawFooter(canvas: Canvas, paint: TextPaint, margin: Float, pageHeight: Int) {
            canvas.drawLine(margin, pageHeight - 60f, pageWidth - margin, pageHeight - 60f, paint)
            canvas.drawText(
                "AI-generated report. Please consult your doctor.",
                margin,
                pageHeight - 40f,
                paint
            )
        }

        startNewPage()

        // Header title
        drawWrappedText("Ayu Health Report", titlePaint)

        // Basic Details
        drawParagraph("Personal Information", listOf(
            "Name: ${profileData["name"] ?: "N/A"}",
            "Gender: ${profileData["gender"] ?: "N/A"}",
            "Location: ${profileData["location"] ?: "N/A"}",
            "Age: ${report.responseData?.response_text?.Age ?: "N/A"} Months"
        ))

        // Vitals Section
        val vitals = report.responseData?.response_text?.Vitals
        if (vitals != null) {
            drawParagraph("Vitals", listOfNotNull(
                vitals.height_cm.lastOrNull()?.let { "Height: ${it.value} cm" },
                vitals.weight_kg.lastOrNull()?.let { "Weight: ${it.value} kg" },
                vitals.heart_rate.lastOrNull()?.let { "Heart Rate: ${it.value} bpm" },
                vitals.spo2.lastOrNull()?.let { "SpO2: ${it.value}%" }
            ))
        }

        // Milestones Section
        val milestones = report.responseData?.response_text?.Milestones
        if (milestones != null) {
            drawWrappedText("Developmental Milestones", titlePaint)

            drawParagraph("Completion Percentage", listOf(
                "Motor: ${milestones.Completion_Percentage.Motor}%",
                "Sensory: ${milestones.Completion_Percentage.Sensory}%",
                "Cognitive: ${milestones.Completion_Percentage.Cognitive}%",
                "Feeding: ${milestones.Completion_Percentage.Feeding}%"
            ))

            // Loop pending items list and display one by one
            val pendingList = milestones.Pending // Assuming Pending is a List<String>
            if (pendingList != null && pendingList.isNotEmpty()) {
                drawWrappedText("Pending Milestones:", titlePaint)
                pendingList.forEach { pendingItem ->
                    drawWrappedText("• $pendingItem", textPaint)
                }
            }
        }

        // Health Events Section
        val healthEvents = report.responseData?.response_text?.Health_Events ?: emptyList()
        if (healthEvents.isNotEmpty()) {
            drawWrappedText("Health Events", titlePaint)
            healthEvents.forEach { event ->
                drawParagraph("Condition: ${event.condition}", listOf("Symptoms: ${event.symptoms}"))
            }
        }

        finishPage()

        val fileName = "${report.name}_${System.currentTimeMillis()}.pdf"
        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)

        pdfDocument.writeTo(FileOutputStream(file))
        pdfDocument.close()

        withContext(Dispatchers.Main) {
            try {
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "No PDF viewer found", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun drawHeader(canvas: Canvas, paint: TextPaint, margin: Float, pageWidth: Int) {
        canvas.drawText("NeoSynk Android App", margin, 40f, paint)
        canvas.drawLine(margin, 45f, pageWidth - margin, 45f, paint)

        val currentDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
        val dateText = "Generated: $currentDate"
        val textWidth = paint.measureText(dateText)
        canvas.drawText(dateText, pageWidth - margin - textWidth, 40f, paint)
    }


    private fun drawFooter(canvas: Canvas, paint: Paint, margin: Float, pageHeight: Int) {
        canvas.drawLine(margin, pageHeight - 60f, 595f - margin, pageHeight - 60f, paint)
        canvas.drawText(
            "AI-generated report. Please consult your doctor.",
            margin,
            pageHeight - 40f,
            paint
        )
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
        response.response_text.Vitals.height_cm.forEach { dates.add(it.date) }
        response.response_text.Vitals.weight_kg.forEach { dates.add(it.date) }
        response.response_text.Vitals.heart_rate.forEach { dates.add(it.date) }
        response.response_text.Vitals.spo2.forEach { dates.add(it.date) }
        response.response_text.Health_Events.forEach { dates.add(it.date) }
        return dates.maxOrNull() ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    fun clearDownloadStatus() {
        _downloadStatus.value = DownloadStatus.Idle
    }
}

sealed class DownloadStatus {
    object Idle : DownloadStatus()
    data class Loading(val reportId: String) : DownloadStatus()
    data class Success(val reportId: String) : DownloadStatus()
    data class Error(val reportId: String, val message: String) : DownloadStatus()
}