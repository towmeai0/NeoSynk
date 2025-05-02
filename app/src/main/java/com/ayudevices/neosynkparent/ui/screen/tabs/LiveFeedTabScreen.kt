package com.ayudevices.neosynkparent.ui.screen.tabs

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.Camera
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ayudevices.neosynkparent.R

@Composable
fun LiveTab(navController: NavController) {
    var isCameraOn by remember { mutableStateOf(false) }
    var mCamera: Camera? by remember { mutableStateOf(null) }
    val context = LocalContext.current

    // Button to toggle the camera
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = {
                if (!isCameraOn) {
                    // Check camera permission
                    if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        ActivityCompat.requestPermissions(
                            context as ComponentActivity,
                            arrayOf(Manifest.permission.CAMERA),
                            101
                        )
                    } else {
                        // Start Camera
                        startCamera(mCamera)
                        isCameraOn = true
                    }
                } else {
                    // Stop camera feed
                    stopCamera(mCamera)
                    isCameraOn = false
                }
            }
        ) {
            Text(text = if (isCameraOn) "Stop Camera" else "Start Camera")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Show live feed frame
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isCameraOn) {
                SurfaceView(context).apply {
                    val surfaceHolder = holder
                    surfaceHolder.addCallback(object : SurfaceHolder.Callback {
                        override fun surfaceCreated(p0: SurfaceHolder) {
                            try {
                                mCamera = Camera.open()
                                mCamera?.setPreviewDisplay(holder)
                                mCamera?.startPreview()
                            } catch (e: Exception) {
                                e.printStackTrace()
                                Toast.makeText(context, "Error opening camera", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun surfaceChanged(
                            p0: SurfaceHolder,
                            height: Int,
                            p2: Int,
                            p3: Int
                        ) {}

                        override fun surfaceDestroyed(p0: SurfaceHolder) {
                            stopCamera(mCamera)
                        }
                    })
                }
            }
        }
    }
}

// Function to start the camera
fun startCamera(mCamera: Camera?) {
    mCamera?.apply {
        startPreview()
    }
}

// Function to stop the camera
fun stopCamera(mCamera: Camera?) {
    mCamera?.apply {
        stopPreview()
        release()
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewLiveTab() {
    LiveTab(navController = rememberNavController()) // Preview with a dummy NavController
}
