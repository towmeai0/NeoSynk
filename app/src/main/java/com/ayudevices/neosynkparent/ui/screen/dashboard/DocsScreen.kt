package com.ayudevices.neosynkparent.ui.screen.dashboard

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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ayudevices.neosynkparent.viewmodel.DocsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocsScreen(
    navController: NavController,
    viewModel: DocsViewModel = viewModel()
) {
    val selectedFileName by viewModel.selectedFileName.collectAsState()

    // Set background to full black
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
                title = { Text("PDF", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )

            Spacer(modifier = Modifier.height(24.dp))

            FileSelectorBox(
                onClick = {
                    // Placeholder for actual file picker
                    viewModel.onFileSelected("example_document.pdf")
                }
            )

            selectedFileName?.let {
                Spacer(modifier = Modifier.height(24.dp))
                Text("Selected file: $it", color = Color.White)
            }
        }
    }
}

@Composable
fun FileSelectorBox(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
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
}
