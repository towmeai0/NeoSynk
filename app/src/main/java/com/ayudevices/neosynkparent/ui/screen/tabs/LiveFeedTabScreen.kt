package com.ayudevices.neosynkparent.ui.screen.tabs

import android.content.Context
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.firebase.database.*
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ayudevices.neosynkparent.viewmodel.CustomSurfaceViewRenderer
import com.ayudevices.neosynkparent.viewmodel.LiveFeedViewModel


private const val TAG = "LiveFeedTab"

@Composable
fun LiveTab(
    navController: NavController,
    viewModel: LiveFeedViewModel = hiltViewModel()
) {
    val isViewing by viewModel.isViewing.collectAsState()
    val connectionStatus by viewModel.connectionStatus.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 🔲 Video feed takes up remaining space
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .border(2.dp, Color.Gray),
            contentAlignment = Alignment.Center
        ) {
            if (isViewing) {
                AndroidView(
                    factory = { ctx ->
                        CustomSurfaceViewRenderer(ctx).apply {
                            viewModel.setRemoteRenderer(this)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text("Feed Inactive")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 📶 Connection Status
        Text(
            text = connectionStatus,
            color = when (connectionStatus) {
                "Connected" -> Color.Green
                "Connecting..." -> Color.Yellow
                else -> Color.Red
            },
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // 🎬 Toggle Button
        Button(
            onClick = {
                if (isViewing) viewModel.stopViewing(context)
                else viewModel.startViewing(context)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isViewing) Color.Red else Color.Green,
                contentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Text(if (isViewing) "Stop Viewing" else "Start Viewing")
        }
    }
}
