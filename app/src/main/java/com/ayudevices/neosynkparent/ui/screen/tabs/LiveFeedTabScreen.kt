package com.ayudevices.neosynkparent.ui.screen.tabs

import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.ayudevices.neosynkparent.viewmodel.CustomSurfaceViewRenderer
import com.ayudevices.neosynkparent.viewmodel.WebRTCManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

private const val TAG = "LiveFeedTab"


@Composable
fun LiveTab(navController: NavController) {
    val context = LocalContext.current
    val auth = Firebase.auth
    val user = auth.currentUser
    val database = Firebase.database

    var isViewing by remember { mutableStateOf(false) }
    var connectionStatus by remember { mutableStateOf("Disconnected") }
    var webRtcManager by remember { mutableStateOf<WebRTCManager?>(null) }

    // Handle cleanup when composable leaves composition
    DisposableEffect(isViewing) {
        onDispose {
            if (!isViewing) {
                webRtcManager?.cleanup()
                database.getReference("requests").child(user?.uid ?: "").removeValue()
            }
        }
    }

    if (user == null) {
        Text("Please log in to view live feed")
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Connection controls
        Button(
            onClick = {
                isViewing = !isViewing
                if (isViewing) {
                    connectionStatus = "Connecting..."
                    database.getReference("requests").child(user.uid).setValue(true)

                    // Initialize WebRTC if not already done
                    if (webRtcManager == null) {
                        webRtcManager = WebRTCManager(
                            context = context,
                            firebaseRef = database.reference,
                            userId = user.uid,
                            isCaller = false // Parent app is the receiver
                        )
                    }
                } else {
                    connectionStatus = "Disconnected"
                    database.getReference("requests").child(user.uid).setValue(false)
                    webRtcManager?.cleanup()
                }
            }
        ) {
            Text(if (isViewing) "Stop Viewing" else "Start Viewing")
        }

        // Connection status indicator
        Text(
            text = connectionStatus,
            color = when (connectionStatus) {
                "Connected" -> Color.Green
                "Connecting..." -> Color.Yellow
                else -> Color.Red
            }
        )

        // Video preview area
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
                            webRtcManager?.setupRemoteRenderer(this)
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    update = { renderer ->
                        // Reattach renderer if needed
                        webRtcManager?.videoTrack?.removeSink(renderer)
                        webRtcManager?.videoTrack?.addSink(renderer)
                    }
                )
            } else {
                Text("Feed Inactive", modifier = Modifier.align(Alignment.Center))
            }
        }
    }

    // Listen for connection status changes
    LaunchedEffect(user.uid) {
        database.getReference("status").child(user.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.getValue(String::class.java)?.let { status ->
                        connectionStatus = status
                        if (status == "Connected") {
                            // Handle successful connection
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    connectionStatus = "Error: ${error.message}"
                }
            })
    }
}