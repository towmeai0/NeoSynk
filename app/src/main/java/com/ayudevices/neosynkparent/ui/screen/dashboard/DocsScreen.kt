package com.ayudevices.neosynkparent.ui.screen.dashboard

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ayudevices.neosynkparent.viewmodel.DocsViewModel
import java.io.File

/*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocsScreen(
    navController: NavController,
    viewModel: DocsViewModel = hiltViewModel(),
    // Add parameter with explicit type and default value
    milestoneViewModel: com.ayudevices.neosynkparent.viewmodel.MilestoneViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    // Collect vitals and intent from ViewModel
    val height = viewModel.height
    val weight = viewModel.weight
    val heartRate = viewModel.heartRate
    val spo2 = viewModel.spo2
    val latestIntent = viewModel.latestIntent

    // State for including milestones in PDF
    var includeMilestones by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            TopAppBar(
                title = { Text("Current Report", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )


            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Vitals Summary", fontSize = 20.sp, color = Color.White)

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Height", fontSize = 18.sp, color = Color.White)
                    Text(height, fontSize = 20.sp, color = Color.Yellow)

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Weight", fontSize = 18.sp, color = Color.White)
                    Text(weight, fontSize = 20.sp, color = Color.Green)

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Heart Rate", fontSize = 18.sp, color = Color.White)
                    Text(heartRate, fontSize = 20.sp, color = Color.Red)

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("SpO2", fontSize = 18.sp, color = Color.White)
                    Text(spo2, fontSize = 20.sp, color = Color.Cyan)

                    // Add toggle for including milestones
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = includeMilestones,
                            onCheckedChange = { includeMilestones = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFF2196F3),
                                uncheckedColor = Color.Gray
                            )
                        )
                        Text(
                            text = "Include Developmental Milestones",
                            color = Color.White,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = {
                try {
                    val pdfFile: File = generateBabyReportPDF(
                        context = context,
                        name = "cgi", // You might want to make this dynamic
                        height = height,
                        weight = weight,
                        heartRate = heartRate,
                        spo2 = spo2,
                        milestoneViewModel = if (includeMilestones) milestoneViewModel else null
                    )

                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.provider",
                        pdfFile
                    )

                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "application/pdf")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }

                    context.startActivity(intent)
                    viewModel.updateLatestIntent(
                        if (includeMilestones) "Opened PDF Report with Milestones"
                        else "Opened PDF Report"
                    )

                } catch (e: Exception) {
                    Toast.makeText(context, "PDF Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }) {
                Text("Generate PDF", color = Color.White)
            }
        }
    }
}

// Keep the original FileSelectorBox composable unchanged
@Composable
fun FileSelectorBox(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .border(2.dp, Color.Gray, RoundedCornerShape(16.dp))
            .background(Color.Black, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.CloudUpload,
                contentDescription = "Upload Icon",
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Tap to select a file", color = Color.White)
        }
    }
}*/
