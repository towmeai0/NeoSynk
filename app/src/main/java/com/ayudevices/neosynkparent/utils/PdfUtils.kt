package com.example.heightmodel.utils

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import com.ayudevices.neosynkparent.viewmodel.MilestoneViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/*


fun generateBabyReportPDF(
    context: Context,
    name: String,
    height: String,
    weight: String,
    heartRate: String,
    spo2: String,
    milestoneViewModel: MilestoneViewModel? = null
): File {
    val fileName = "Baby_Report_${System.currentTimeMillis()}.pdf"
    val file = File(context.getExternalFilesDir(null), fileName)

    val paint = Paint()
    val titlePaint = Paint()
    val dividerPaint = Paint()
    val labelPaint = Paint()
    val valuePaint = Paint()
    val progressPaint = Paint().apply {
        color = Color.rgb(33, 150, 243)
        style = Paint.Style.FILL
    }
    val progressBackgroundPaint = Paint().apply {
        color = Color.LTGRAY
        style = Paint.Style.FILL
    }

    val pdfDocument = android.graphics.pdf.PdfDocument()
    val pageWidth = 595
    val pageHeight = 842
    val margin = 60f

    var pageNumber = 1
    var y = 100
    lateinit var currentPage: android.graphics.pdf.PdfDocument.Page
    lateinit var canvas: Canvas

    fun startNewPage() {
        val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        currentPage = pdfDocument.startPage(pageInfo)
        canvas = currentPage.canvas
        y = 100
        pageNumber++
    }

    fun drawFooter() {
        val footerY = pageHeight - 40f
        paint.textSize = 12f
        paint.color = Color.GRAY
        paint.textAlign = Paint.Align.CENTER
        canvas.drawLine(margin, footerY - 10f, (pageWidth - margin), footerY - 10f, dividerPaint)
        canvas.drawText("Generated using NeoSynk Android App", (pageWidth / 2).toFloat(), footerY, paint)
    }

    fun finishCurrentPage() {
        drawFooter()
        pdfDocument.finishPage(currentPage)
    }

    fun checkSpaceNeeded(space: Int) {
        if (y + space > pageHeight - 60) {
            finishCurrentPage()
            startNewPage()
        }
    }

    startNewPage()

    // Title
    titlePaint.typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
    titlePaint.textSize = 28f
    titlePaint.color = Color.rgb(33, 150, 243)
    titlePaint.textAlign = Paint.Align.CENTER
    canvas.drawText("Baby Report", (pageWidth / 2).toFloat(), y.toFloat(), titlePaint)
    y += 20

    dividerPaint.color = Color.LTGRAY
    dividerPaint.strokeWidth = 2f
    canvas.drawLine(margin, y.toFloat(), (pageWidth - margin), y.toFloat(), dividerPaint)
    y += 50

    labelPaint.typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
    labelPaint.textSize = 18f
    labelPaint.color = Color.DKGRAY
    valuePaint.textSize = 18f
    valuePaint.color = Color.BLACK

    fun drawRow(label: String, value: String) {
        checkSpaceNeeded(40)
        canvas.drawText(label, margin + 20, y.toFloat(), labelPaint)
        canvas.drawText(value, margin + 200, y.toFloat(), valuePaint)
        y += 40
    }

    drawRow("👶 Baby Name:", name)
    drawRow("📏 Current Height:", height)
    drawRow("⚖️ Current Weight:", weight)
    drawRow("❤️ Heart Rate:", heartRate)
    drawRow("🫁 SpO₂ Level:", spo2)
    drawRow("📅 Generated On:", SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date()))

    milestoneViewModel?.let { vm ->
        checkSpaceNeeded(40)
        val milestoneTitlePaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
            textSize = 22f
            color = Color.rgb(33, 150, 243)
        }
        canvas.drawText("Developmental Progress", margin + 20, y.toFloat(), milestoneTitlePaint)
        y += 40

       */
/* val currentLeap = vm.currentLeap.value
        if (currentLeap in 1..10) {
            checkSpaceNeeded(50)
            canvas.drawText("Current Developmental Leap: Leap $currentLeap", margin + 20, y.toFloat(), labelPaint)
            y += 25
            canvas.drawText(vm.getLeapTitle(currentLeap), margin + 40, y.toFloat(), valuePaint)
            y += 25
        }*//*


        val overallProgress = vm.overallProgress.value
        canvas.drawText("Overall Progress: $overallProgress%", margin + 20, y.toFloat(), labelPaint)
        y += 30

        val progressBarWidth = pageWidth - margin * 2 - 40
        val progressWidth = (progressBarWidth * overallProgress / 100f)
        canvas.drawRoundRect(
            margin + 20,
            y.toFloat(),
            margin + 20 + progressBarWidth,
            y + 30f,
            10f,
            10f,
            progressBackgroundPaint
        )
       */
/* canvas.drawRoundRect(
            margin + 20,
            y.toFloat(),
            margin + 20 + progressWidth,
            y + 30f,
            10f,
            10f,
            progressPaint
        )*//*

        y += 50

        fun drawCategoryProgress(category: String, progress: Int) {
            checkSpaceNeeded(70)
            canvas.drawText("$category: $progress%", margin + 20, y.toFloat(), labelPaint)
            y += 25
            val width = (progressBarWidth * progress / 100f)
            canvas.drawRoundRect(
                margin + 20,
                y.toFloat(),
                margin + 20 + progressBarWidth,
                y + 20f,
                5f,
                5f,
                progressBackgroundPaint
            )
            canvas.drawRoundRect(
                margin + 20,
                y.toFloat(),
                margin + 20 + width,
                y + 20f,
                5f,
                5f,
                progressPaint
            )
            y += 40
        }

        */
/*drawCategoryProgress("Motor", vm.motorProgress.value)
        drawCategoryProgress("Sensory", vm.sensoryProgress.value)
        drawCategoryProgress("Communication", vm.communicationProgress.value)
        drawCategoryProgress("Feeding", vm.feedingProgress.value)*//*



    }

    finishCurrentPage()

    file.outputStream().use {
        pdfDocument.writeTo(it)
    }
    pdfDocument.close()
    return file
}
*/
