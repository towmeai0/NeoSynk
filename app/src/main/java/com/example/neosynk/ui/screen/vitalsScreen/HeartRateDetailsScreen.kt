package com.example.neosynk.ui.screen.vitalsScreen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeartRateDetailsScreen(navController: NavController) {
    var selectedTab by remember { mutableStateOf("VitalsScreen") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Vitals Heart Rate", color = Color.White, fontSize = 14.sp)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = Color.White)
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        containerColor = Color.Black
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Heart Rate",
                fontSize = 24.sp,
                color = Color.White,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .border(1.dp, Color(0xFFFF9800), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.DarkGray),
                shape = RoundedCornerShape(16.dp)
            ) {
                GraphPlaceholder()
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Results:",
                fontSize = 18.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Your heart rate has remained within a healthy range.\n\n" +
                        "The average BPM recorded over the past session was 78 BPM.\n\n" +
                        "Keep monitoring regularly to maintain cardiovascular health.",
                fontSize = 14.sp,
                color = Color.White,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun GraphPlaceholder() {
    Canvas(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        val width = size.width
        val height = size.height
        val gridLines = 5

        val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        for (i in 1 until gridLines) {
            val y = height * i / gridLines
            drawLine(
                color = Color.LightGray,
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1f,
                pathEffect = pathEffect
            )
        }

        val heartRatePoints = listOf(60, 80, 90, 70, 85, 75, 65, 80)
        val pointGap = width / (heartRatePoints.size - 1)
        val maxBPM = 100
        val scaledPoints = heartRatePoints.mapIndexed { i, bpm ->
            Offset(i * pointGap, height - (bpm / maxBPM.toFloat()) * height)
        }

        for (i in 0 until scaledPoints.size - 1) {
            drawLine(
                color = Color.Red,
                start = scaledPoints[i],
                end = scaledPoints[i + 1],
                strokeWidth = 4f
            )
        }
    }
}
