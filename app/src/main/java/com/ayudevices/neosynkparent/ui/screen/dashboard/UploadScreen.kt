package com.ayudevices.neosynkparent.ui.screen.dashboard

import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ayudevices.neosynkparent.data.model.AyuReportResponse
import com.ayudevices.neosynkparent.data.model.MedicalReportResponse
import com.ayudevices.neosynkparent.data.model.UploadStatus
import com.ayudevices.neosynkparent.ui.theme.appBarColor
import com.ayudevices.neosynkparent.ui.theme.orange
import com.ayudevices.neosynkparent.viewmodel.AyuReportViewModel
import com.ayudevices.neosynkparent.viewmodel.DownloadStatus
import com.ayudevices.neosynkparent.viewmodel.MedicalReportViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


// Updated data classes
data class AyuReport(
    val id: String,
    val name: String,
    val generatedDate: String,
    val reportType: String,
    val downloadUrl: String,
    val responseData: AyuReportResponse? = null // Include the full response data
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadScreen(
    navController: NavController,
    ayuReportViewModel: AyuReportViewModel = hiltViewModel(),
    medicalReportViewModel: MedicalReportViewModel = hiltViewModel(),
    userId: String
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var showUploadDialog by remember { mutableStateOf(false) }

    // Observe Medical Reports from ViewModel
    val medicalReports by medicalReportViewModel.medicalReports.observeAsState(emptyList())
    val isLoadingMedical by medicalReportViewModel.isLoading.observeAsState(false)
    val medicalError by medicalReportViewModel.error.observeAsState()
    val uploadStatus by medicalReportViewModel.uploadStatus.observeAsState(UploadStatus.Idle)

    // Observe AyuReports from ViewModel
    val ayuReports by ayuReportViewModel.ayuReports.observeAsState(emptyList())
    val isLoadingAyu by ayuReportViewModel.isLoading.observeAsState(false)
    val ayuError by ayuReportViewModel.error.observeAsState()
    val downloadStatus by ayuReportViewModel.downloadStatus.observeAsState(DownloadStatus.Idle)

    val context = LocalContext.current

    // Load Medical reports when Medical Reports tab is selected
    LaunchedEffect(selectedTabIndex) {
        if (selectedTabIndex == 0 && medicalReports.isEmpty() && !isLoadingMedical && medicalError == null) {
            medicalReportViewModel.loadMedicalReports(userId)
        }
    }

    // Load Ayu reports when Ayu Reports tab is selected
    LaunchedEffect(selectedTabIndex) {
        if (selectedTabIndex == 1 && ayuReports.isEmpty() && !isLoadingAyu && ayuError == null) {
            ayuReportViewModel.loadAyuReports()
        }
    }

    // Handle upload status
    LaunchedEffect(uploadStatus) {
        when (uploadStatus) {
            is UploadStatus.Success -> {
                Toast.makeText(context, (uploadStatus as UploadStatus.Success).message, Toast.LENGTH_SHORT).show()
                showUploadDialog = false
                selectedFileUri = null
                selectedFileName = null
                medicalReportViewModel.clearUploadStatus()
            }
            is UploadStatus.Error -> {
                Toast.makeText(context, "Upload failed: ${(uploadStatus as UploadStatus.Error).message}", Toast.LENGTH_LONG).show()
            }
            else -> {}
        }
    }

    // Handle download status
    LaunchedEffect(downloadStatus) {
        when (downloadStatus) {
            is DownloadStatus.Success -> {
                Toast.makeText(context, "Report downloaded successfully", Toast.LENGTH_SHORT).show()
                ayuReportViewModel.clearDownloadStatus()
            }
            is DownloadStatus.Error -> {
                Log.d("DownloadStatus", "Download failed: ${(downloadStatus as DownloadStatus.Error).message}")
                Toast.makeText(context, "Download failed: ${(downloadStatus as DownloadStatus.Error).message}", Toast.LENGTH_SHORT).show()
                ayuReportViewModel.clearDownloadStatus()
            }
            else -> {}
        }
    }

    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedFileUri = it
            val cursor = context.contentResolver.query(it, null, null, null, null)
            cursor?.use { c ->
                if (c.moveToFirst()) {
                    val displayNameIndex = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (displayNameIndex != -1) {
                        selectedFileName = c.getString(displayNameIndex)
                    }
                }
            }
            if (selectedFileName == null) {
                selectedFileName = it.lastPathSegment ?: "Unknown file"
            }
            showUploadDialog = true
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Custom compact top bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Reports",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Tab Row
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.Black,
                    contentColor = Color.White,
                    indicator = { tabPositions ->
                        if (selectedTabIndex < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentSize(align = Alignment.BottomStart)
                                    .offset(x = tabPositions[selectedTabIndex].left)
                                    .width(tabPositions[selectedTabIndex].width),
                                color = orange
                            )
                        }
                    }
                ) {
                    Tab(
                        text = { Text("Medical Reports") },
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        selectedContentColor = orange,
                        unselectedContentColor = Color.Gray
                    )
                    Tab(
                        text = { Text("Ayu Reports") },
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        selectedContentColor = orange,
                        unselectedContentColor = Color.Gray
                    )
                }

                // Tab Content
                when (selectedTabIndex) {
                    0 -> MedicalReportsTab(
                        reports = medicalReports,
                        isLoading = isLoadingMedical,
                        error = medicalError,
                        onRetry = {
                            medicalReportViewModel.loadMedicalReports(userId)
                        }
                    )
                    1 -> AyuReportsTab(
                        reports = ayuReports,
                        isLoading = isLoadingAyu,
                        error = ayuError,
                        downloadStatus = downloadStatus,
                        onDownload = { report ->
                            ayuReportViewModel.downloadPdf(report, context)
                        },
                        onRetry = {
                            ayuReportViewModel.loadAyuReports()
                        }
                    )
                }
            }

            // Floating Action Button (only for Medical Reports tab)
            if (selectedTabIndex == 0) {
                FloatingActionButton(
                    onClick = {
                        filePickerLauncher.launch("*/*")
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    containerColor = orange,
                    contentColor = Color.White
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Medical Report"
                    )
                }
            }
        }

        // Upload Dialog
        if (showUploadDialog) {
            UploadDialog(
                fileName = selectedFileName ?: "",
                uploadStatus = uploadStatus,
                onUpload = {
                    selectedFileUri?.let { uri ->
                        medicalReportViewModel.uploadMedicalReport(userId, uri, context)
                    }
                },
                onDismiss = {
                    showUploadDialog = false
                    selectedFileUri = null
                    selectedFileName = null
                    medicalReportViewModel.clearUploadStatus()
                }
            )
        }
    }
}



@Composable
fun MedicalReportsTab(
    reports: List<MedicalReportResponse>,
    isLoading: Boolean,
    error: String?,
    onRetry: () -> Unit
) {
    when {
        isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = orange)
            }
        }

        error != null -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Error loading reports",
                        color = Color.Red,
                        fontSize = 16.sp
                    )
                    Text(
                        text = error,
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onRetry,
                        colors = ButtonDefaults.buttonColors(containerColor = orange)
                    ) {
                        Text("Retry")
                    }
                }
            }
        }

        reports.isEmpty() -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = "No Reports",
                        tint = Color.Gray,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No medical reports found",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Tap the + button to upload your first report",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }
        }

        else -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(reports) { report ->
                    MedicalReportCard(report = report)
                }
            }
        }
    }
}

// Updated UploadDialog
@Composable
fun UploadDialog(
    fileName: String,
    uploadStatus: UploadStatus,
    onUpload: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {
            if (uploadStatus !is UploadStatus.Loading) onDismiss()
        },
        containerColor = appBarColor,
        title = {
            Text(
                text = "Upload Medical Report",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.Gray.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Selected File:",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Light
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = fileName,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                when (uploadStatus) {
                    is UploadStatus.Success -> {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = uploadStatus.message,
                            color = Color.Green,
                            fontSize = 14.sp
                        )
                    }
                    is UploadStatus.Error -> {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = uploadStatus.message,
                            color = Color.Red,
                            fontSize = 14.sp
                        )
                    }
                    else -> {}
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onUpload,
                colors = ButtonDefaults.buttonColors(containerColor = orange),
                enabled = uploadStatus !is UploadStatus.Loading && uploadStatus !is UploadStatus.Success
            ) {
                when (uploadStatus) {
                    is UploadStatus.Loading -> {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Uploading...")
                    }
                    is UploadStatus.Success -> {
                        Text("Done")
                    }
                    else -> {
                        Text("Upload")
                    }
                }
            }
        },
        dismissButton = {
            if (uploadStatus !is UploadStatus.Loading) {
                TextButton(
                    onClick = onDismiss
                ) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        }
    )
}




@Composable
fun MedicalReportCard(report: MedicalReportResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = appBarColor.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = "Medical Report",
                    tint = orange,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = report.fileType,
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = report.name,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Uploaded: ${report.uploadDate}",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
                Text(
                    text = report.size,
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }
    }
}



@Composable
fun AyuReportsTab(
    reports: List<AyuReport>,
    isLoading: Boolean,
    error: String?,
    downloadStatus: DownloadStatus,
    onDownload: (AyuReport) -> Unit,
    onRetry: () -> Unit
) {
    val currentTime = remember {
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
    }

    when {
        isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = orange)
            }
        }

        error != null -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Error loading reports",
                        color = Color.Red,
                        fontSize = 16.sp
                    )
                    Text(
                        text = error,
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onRetry,
                        colors = ButtonDefaults.buttonColors(containerColor = orange)
                    ) {
                        Text("Retry")
                    }
                }
            }
        }

        reports.isEmpty() -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = "No Reports",
                        tint = Color.Gray,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Ayu reports found",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                }
            }
        }

        else -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(reports) { report ->
                    AyuReportCard(
                        report = report,
                        downloadStatus = downloadStatus,
                        viewedAt = currentTime,
                        onDownload = { onDownload(report)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AyuReportCard(
    report: AyuReport,
    downloadStatus: DownloadStatus,
    onDownload: () -> Unit,
    viewedAt: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = appBarColor.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = "Ayu Report",
                        tint = orange,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = report.reportType,
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }

                IconButton(
                    onClick = onDownload,
                    enabled = downloadStatus !is DownloadStatus.Loading ||
                            (downloadStatus as? DownloadStatus.Loading)?.reportId != report.id
                ) {
                    when (downloadStatus) {
                        is DownloadStatus.Loading -> {
                            if (downloadStatus.reportId == report.id) {
                                CircularProgressIndicator(
                                    color = orange,
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = "Download Report",
                                    tint = orange,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        else -> {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Download Report",
                                tint = orange,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = report.name,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Generated: ${report.generatedDate}",
                color = Color.Gray,
                fontSize = 12.sp
            )

            Text(
                text = "Generated at: $viewedAt",
                color = Color.Gray,
                fontSize = 12.sp
            )

            report.responseData?.let { response ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Age: ${response.response_text.Age} months",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }
    }
}



private fun getFileType(fileName: String): String {
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


