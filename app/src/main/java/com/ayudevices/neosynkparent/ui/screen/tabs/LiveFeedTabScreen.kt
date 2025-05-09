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
import com.ayudevices.neosynkparent.viewmodel.WebRTCManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

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
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(
            onClick = { viewModel.toggleViewing() },
            enabled = connectionStatus != "Connecting..."
        ) {
            Text(if (isViewing) "Stop Viewing" else "Start Viewing")
        }

        Text(
            text = connectionStatus,
            color = when (connectionStatus) {
                "Connected" -> Color.Green
                "Connecting...", "Connecting" -> Color.Yellow
                else -> Color.Red
            }
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(9f / 16f)
                .border(2.dp, Color.Gray)
        ) {
            if (isViewing) {
                AndroidView(
                    factory = { ctx ->
                        CustomSurfaceViewRenderer(ctx).apply {
                            viewModel.setRemoteRenderer(this)
                        }
                    },
                    modifier = Modifier.fillMaxSize(),

                )
            } else {
                Text("Feed Inactive", modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}
