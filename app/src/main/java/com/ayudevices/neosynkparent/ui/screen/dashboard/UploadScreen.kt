package com.ayudevices.neosynkparent.ui.screen.dashboard

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ayudevices.neosynkparent.data.repository.FileUploadRepository
import com.ayudevices.neosynkparent.ui.theme.appBarColor
import com.ayudevices.neosynkparent.ui.theme.orange
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

// Data classes for reports
data class MedicalReport(
    val id: String,
    val name: String,
    val uploadDate: String,
    val fileType: String,
    val size: String
)

data class AyuReport(
    val id: String,
    val name: String,
    val generatedDate: String,
    val reportType: String,
    val downloadUrl: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadScreen(navController: NavController) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var uploadMessage by remember { mutableStateOf<String?>(null) }
    var showUploadDialog by remember { mutableStateOf(false) }

    // Dummy data for medical reports
    var medicalReports by remember {
        mutableStateOf(listOf(
            MedicalReport("1", "Blood Test Report.pdf", "2024-05-20", "PDF", "2.3 MB"),
            MedicalReport("2", "X-Ray Chest.jpg", "2024-05-18", "Image", "1.8 MB"),
            MedicalReport("3", "ECG Report.pdf", "2024-05-15", "PDF", "1.2 MB"),
            MedicalReport("4", "MRI Scan.pdf", "2024-05-10", "PDF", "5.6 MB")
        ))
    }

    // Dummy data for Ayu reports
    val ayuReports = remember {
        listOf(
            AyuReport("1", "Comprehensive Health Analysis", "2024-05-25", "Health Analysis", "https://example.com/report1.pdf"),
            AyuReport("2", "Dosha Assessment Report", "2024-05-22", "Dosha Analysis", "https://example.com/report2.pdf"),
            AyuReport("3", "Wellness Recommendation", "2024-05-20", "Wellness Plan", "https://example.com/report3.pdf"),
            AyuReport("4", "Progress Tracking Report", "2024-05-18", "Progress Report", "https://example.com/report4.pdf")
        )
    }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val fileUploadRepository = remember { FileUploadRepository() }

    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedFileUri = it
            val cursor = context.contentResolver.query(it, null, null, null, null)
            cursor?.use { c ->
                if (c.moveToFirst()) {
                    val displayNameIndex = c.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
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

                // Tab Row - Fixed tab indicator
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
                    0 -> MedicalReportsTab(medicalReports)
                    1 -> AyuReportsTab(ayuReports)
                }
            }

            // Floating Action Button (only visible in Medical Reports tab)
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
                isUploading = isUploading,
                uploadMessage = uploadMessage,
                onUpload = {
                    selectedFileUri?.let { uri ->
                        isUploading = true
                        uploadMessage = null

                        scope.launch {
                            fileUploadRepository.uploadFile(
                                uri = uri,
                                context = context,
                                uploadUrl = "https://your-server.com/upload",
                                onSuccess = { response ->
                                    isUploading = false
                                    uploadMessage = "File uploaded successfully!"

                                    // Add new report to the list
                                    val newReport = MedicalReport(
                                        id = (medicalReports.size + 1).toString(),
                                        name = selectedFileName ?: "Unknown file",
                                        uploadDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                                        fileType = getFileType(selectedFileName ?: ""),
                                        size = "Unknown"
                                    )
                                    medicalReports = medicalReports + newReport

                                    // Reset after successful upload - Fixed coroutine scope
                                    scope.launch {
                                        delay(2000)
                                        showUploadDialog = false
                                        selectedFileUri = null
                                        selectedFileName = null
                                        uploadMessage = null
                                    }
                                },
                                onError = { error ->
                                    isUploading = false
                                    uploadMessage = "Upload failed: $error"
                                }
                            )
                        }
                    }
                },
                onDismiss = {
                    showUploadDialog = false
                    selectedFileUri = null
                    selectedFileName = null
                    uploadMessage = null
                }
            )
        }
    }
}

@Composable
fun MedicalReportsTab(reports: List<MedicalReport>) {
    if (reports.isEmpty()) {
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
    } else {
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

@Composable
fun AyuReportsTab(reports: List<AyuReport>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(reports) { report ->
            AyuReportCard(report = report)
        }
    }
}

@Composable
fun MedicalReportCard(report: MedicalReport) {
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
fun AyuReportCard(report: AyuReport) {
    val context = LocalContext.current

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
                    onClick = {
                        // Handle download - you can implement actual download logic here
                        // For now, just show a toast or log
                        android.widget.Toast.makeText(
                            context,
                            "Downloading ${report.name}",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Download Report",
                        tint = orange,
                        modifier = Modifier.size(20.dp)
                    )
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
        }
    }
}

@Composable
fun UploadDialog(
    fileName: String,
    isUploading: Boolean,
    uploadMessage: String?,
    onUpload: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isUploading) onDismiss() },
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

                uploadMessage?.let { message ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = message,
                        color = if (message.contains("success")) Color.Green else Color.Red,
                        fontSize = 14.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onUpload,
                colors = ButtonDefaults.buttonColors(containerColor = orange),
                enabled = !isUploading && uploadMessage?.contains("success") != true
            ) {
                if (isUploading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Uploading...")
                } else if (uploadMessage?.contains("success") == true) {
                    Text("Done")
                } else {
                    Text("Upload")
                }
            }
        },
        dismissButton = {
            if (!isUploading) {
                TextButton(
                    onClick = onDismiss
                ) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        }
    )
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




//package com.ayudevices.neosynkparent.ui.screen.dashboard
//
//import android.net.Uri
//import androidx.activity.compose.rememberLauncherForActivityResult
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.AttachFile
//import androidx.compose.material.icons.filled.CloudUpload
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.navigation.NavController
//import com.ayudevices.neosynkparent.data.repository.FileUploadRepository
//import com.ayudevices.neosynkparent.ui.theme.appBarColor
//import com.ayudevices.neosynkparent.ui.theme.orange
//import kotlinx.coroutines.launch
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun UploadScreen(navController: NavController) {
//    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
//    var selectedFileName by remember { mutableStateOf<String?>(null) }
//    var isUploading by remember { mutableStateOf(false) }
//    var uploadMessage by remember { mutableStateOf<String?>(null) }
//
//    val context = LocalContext.current
//    val scope = rememberCoroutineScope()
//    val fileUploadRepository = remember { FileUploadRepository() }
//
//    // File picker launcher
//    val filePickerLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.GetContent()
//    ) { uri: Uri? ->
//        uri?.let {
//            selectedFileUri = it
//            // Get the file name from the URI
//            val cursor = context.contentResolver.query(it, null, null, null, null)
//            cursor?.use { c ->
//                if (c.moveToFirst()) {
//                    val displayNameIndex = c.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
//                    if (displayNameIndex != -1) {
//                        selectedFileName = c.getString(displayNameIndex)
//                    }
//                }
//            }
//            // Fallback to last path segment if display name is not available
//            if (selectedFileName == null) {
//                selectedFileName = it.lastPathSegment ?: "Unknown file"
//            }
//        }
//    }
//
//    Surface(
//        modifier = Modifier.fillMaxSize(),
//        color = Color.Black
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(16.dp)
//        ) {
//            TopAppBar(
//                title = { Text("Upload File", color = Color.White) },
//                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
//            )
//
//            Spacer(modifier = Modifier.height(32.dp))
//
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(200.dp)
//                    .border(2.dp, Color.Gray, RoundedCornerShape(16.dp))
//                    .background(appBarColor.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
//                    .clickable {
//                        // Launch file picker with various file types
//                        filePickerLauncher.launch("*/*")
//                    },
//                contentAlignment = Alignment.Center
//            ) {
//                Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                    Icon(
//                        imageVector = Icons.Default.CloudUpload,
//                        contentDescription = "Upload Icon",
//                        tint = Color.White,
//                        modifier = Modifier.size(48.dp)
//                    )
//                    Spacer(modifier = Modifier.height(8.dp))
//                    Text(
//                        text = if (selectedFileName != null) "Tap to select another file" else "Tap to select a file",
//                        color = Color.White
//                    )
//                }
//            }
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            selectedFileName?.let {
//                Card(
//                    modifier = Modifier.fillMaxWidth(),
//                    colors = CardDefaults.cardColors(containerColor = Color.Gray.copy(alpha = 0.2f))
//                ) {
//                    Column(
//                        modifier = Modifier.padding(16.dp)
//                    ) {
//                        Text(
//                            text = "Selected File:",
//                            color = Color.Gray,
//                            fontSize = 12.sp,
//                            fontWeight = FontWeight.Light
//                        )
//                        Spacer(modifier = Modifier.height(4.dp))
//                        Text(
//                            text = it,
//                            color = Color.White,
//                            fontSize = 14.sp,
//                            fontWeight = FontWeight.Medium
//                        )
//                    }
//                }
//            }
//
//            Spacer(modifier = Modifier.height(24.dp))
//
//            Button(
//                onClick = {
//                    selectedFileUri?.let { uri ->
//                        isUploading = true
//                        uploadMessage = null
//
//                        scope.launch {
//                            fileUploadRepository.uploadFile(
//                                uri = uri,
//                                context = context,
//                                uploadUrl = "https://your-server.com/upload",
//                                onSuccess = { response ->
//                                    isUploading = false
//                                    uploadMessage = "File uploaded successfully!"
//                                },
//                                onError = { error ->
//                                    isUploading = false
//                                    uploadMessage = "Upload failed: $error"
//                                }
//                            )
//                        }
//                    }
//                },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(50.dp),
//                colors = ButtonDefaults.buttonColors(containerColor = orange),
//                shape = RoundedCornerShape(12.dp),
//                enabled = selectedFileUri != null && !isUploading
//            ) {
//                if (isUploading) {
//                    CircularProgressIndicator(
//                        color = Color.White,
//                        modifier = Modifier.size(20.dp)
//                    )
//                } else {
//                    Icon(
//                        imageVector = Icons.Default.AttachFile,
//                        contentDescription = "Attach File",
//                        tint = Color.White
//                    )
//                }
//                Spacer(modifier = Modifier.width(8.dp))
//                Text(
//                    text = if (isUploading) "Uploading..." else "Upload",
//                    color = Color.White
//                )
//            }
//
//            if (selectedFileUri == null) {
//                Spacer(modifier = Modifier.height(8.dp))
//                Text(
//                    text = "Please select a file to upload",
//                    color = Color.Gray,
//                    fontSize = 12.sp,
//                    modifier = Modifier.fillMaxWidth()
//                )
//            }
//
//            // Upload status message
//            uploadMessage?.let { message ->
//                Spacer(modifier = Modifier.height(16.dp))
//                Card(
//                    modifier = Modifier.fillMaxWidth(),
//                    colors = CardDefaults.cardColors(
//                        containerColor = if (message.contains("success"))
//                            Color.Green.copy(alpha = 0.2f)
//                        else
//                            Color.Red.copy(alpha = 0.2f)
//                    )
//                ) {
//                    Text(
//                        text = message,
//                        color = if (message.contains("success")) Color.Green else Color.Red,
//                        fontSize = 14.sp,
//                        modifier = Modifier.padding(16.dp)
//                    )
//                }
//            }
//        }
//    }
//}