package com.ayudevices.neosynkparent.ui.screen.tabs

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ayudevices.neosynkparent.viewmodel.LiveFeedViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import org.videolan.libvlc.*
import org.videolan.libvlc.util.VLCVideoLayout
import java.util.*

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

    // VLC components - remember them across recompositions
    val libVLC = remember {
        val options = ArrayList<String>().apply {
            add("--rtsp-tcp")
            add("--network-caching=1500")
            add("--no-drop-late-frames")
            add("--no-skip-frames")
        }
        LibVLC(context, options)
    }

    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var videoLayout by remember { mutableStateOf<VLCVideoLayout?>(null) }
    var isBuffering by remember { mutableStateOf(false) }
    var restartTrigger by remember { mutableStateOf(0) }

    // Function to properly stop and clean up player
    fun cleanupPlayer() {
        Log.d(TAG, "Cleaning up MediaPlayer")
        mediaPlayer?.let { player ->
            // Stop playback first
            if (player.isPlaying) {
                player.stop()
            }
            // Detach views
            player.detachViews()
            // Release the player
            player.release()
        }
        mediaPlayer = null
        isBuffering = false
    }

    // Force restart when viewing state changes
    LaunchedEffect(isViewing) {
        if (isViewing) {
            // Clean up any existing player before starting
            cleanupPlayer()
            restartTrigger++
        } else {
            // Stop viewing - cleanup everything
            cleanupPlayer()
        }
    }

    // Auto-hide loader after timeout
    LaunchedEffect(isBuffering) {
        if (isBuffering) {
            kotlinx.coroutines.delay(10000) // 10 second timeout
            if (isBuffering) {
                Log.d(TAG, "Loader timeout - hiding loader")
                isBuffering = false
            }
        }
    }

    // RTSP URL
    val rtspUrl = "rtsp://admin:AyuSynk%402025@192.168.31.100:554/Streaming/Channels/101"

    // Handle lifecycle events
    var wasViewingBeforePause by remember { mutableStateOf(false) }

    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    Log.d(TAG, "LiveTab paused - stopping stream")
                    wasViewingBeforePause = viewModel.isViewing.value
                    if (viewModel.isViewing.value) {
                        viewModel.stopViewing()
                    }
                }
                Lifecycle.Event.ON_RESUME -> {
                    Log.d(TAG, "LiveTab resumed $wasViewingBeforePause")
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
            cleanupPlayer()
            libVLC.release()
            lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Video feed
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .border(2.dp, Color.Gray, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (!isViewing) {
                // Default thumbnail with gradient background
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF2C3E50),
                                    Color(0xFF3498DB)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VideocamOff,
                            contentDescription = "No Video",
                            tint = Color.White,
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Live Feed Inactive",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap 'Start Viewing' to begin live stream",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            } else {
                // Always show the AndroidView when viewing, overlay loader if buffering
                Box(modifier = Modifier.fillMaxSize()) {
                    AndroidView(
                        factory = { ctx ->
                            VLCVideoLayout(ctx).apply {
                                Log.d(TAG, "Creating VLCVideoLayout")
                                videoLayout = this
                            }
                        },
                        update = { layout ->
                            // Update reference
                            videoLayout = layout

                            // Use restartTrigger to force clean restart
                            if (isViewing && (mediaPlayer == null || restartTrigger > 0)) {
                                Log.d(TAG, "Starting VLC playback")
                                isBuffering = true

                                val player = MediaPlayer(libVLC).apply {
                                    // Detach any previous views first
                                    detachViews()
                                    attachViews(layout, null, false, false)

                                    setEventListener { event ->
                                        when (event.type) {
                                            MediaPlayer.Event.Playing -> {
                                                Log.d(TAG, "VLC Playing - hiding loader")
                                                isBuffering = false
                                            }
                                            MediaPlayer.Event.Vout -> {
                                                Log.d(TAG, "VLC Video output started")
                                                isBuffering = false
                                            }
                                            MediaPlayer.Event.Buffering -> {
                                                val bufferPercent = event.buffering
                                                Log.d(TAG, "VLC Buffering: $bufferPercent%")
                                                isBuffering = bufferPercent < 100f
                                            }
                                            MediaPlayer.Event.EncounteredError -> {
                                                Log.e(TAG, "VLC Error")
                                                isBuffering = false
                                            }
                                            MediaPlayer.Event.Stopped -> {
                                                Log.d(TAG, "VLC Stopped")
                                                isBuffering = false
                                            }
                                            MediaPlayer.Event.EndReached -> {
                                                Log.d(TAG, "VLC End reached")
                                                isBuffering = false
                                            }
                                        }
                                    }

                                    val media = Media(libVLC, Uri.parse(rtspUrl))
                                    media.setHWDecoderEnabled(true, false)
                                    media.addOption(":network-caching=1500")
                                    media.addOption(":rtsp-tcp")
                                    setMedia(media)

                                    // Set video scaling to fill the view properly
                                    videoScale = MediaPlayer.ScaleType.SURFACE_FILL
                                    aspectRatio = null // Auto aspect ratio

                                    play()
                                }

                                mediaPlayer = player
                                restartTrigger = 0 // Reset trigger after successful start
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Show loader overlay when buffering
                    if (isBuffering) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 3.dp,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Connecting to live feed...",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Connection Status
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

        // Toggle Button
        Button(
            onClick = {
                if (isViewing) {
                    viewModel.stopViewing()
                } else {
                    viewModel.startViewing()
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isViewing) Color.Red else Color(0xFF27AE60),
                contentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(12.dp)),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isViewing) Icons.Default.VideocamOff else Icons.Default.Videocam,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isViewing) "Stop Viewing" else "Start Viewing",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}