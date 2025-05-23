
package com.ayudevices.neosynkparent.ui.screen.tabs

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ayudevices.neosynkparent.viewmodel.CustomSurfaceViewRenderer
import com.ayudevices.neosynkparent.viewmodel.LiveFeedViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import android.util.Log

private const val TAG = "LiveFeedTab"

@Composable
fun LiveTab(
    navController: NavController,
    viewModel: LiveFeedViewModel = hiltViewModel()
) {
    val isViewing by viewModel.isViewing.collectAsState()
    val connectionStatus by viewModel.connectionStatus.collectAsState()
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    // Handle lifecycle events - stop when leaving, auto-restart when returning
    var wasViewingBeforePause by remember { mutableStateOf(false) }

    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    Log.d(TAG, "LiveTab paused - stopping stream")
                    // Remember current viewing state before stopping
                    wasViewingBeforePause = viewModel.isViewing.value

                    // Stop viewing when tab is no longer visible
                    if (viewModel.isViewing.value) {
                        viewModel.stopViewing()
                    }
                }
                Lifecycle.Event.ON_RESUME -> {
                    Log.d(TAG, "LiveTab resumed $wasViewingBeforePause")
                    // Auto-restart if we were viewing before
                    if (wasViewingBeforePause) {
                        Log.d(TAG, "Auto-restarting stream")
                        viewModel.startViewing()
                    }
                }
                else -> {}
            }
        }

        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }

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
                if (isViewing) viewModel.stopViewing()
                else viewModel.startViewing()
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