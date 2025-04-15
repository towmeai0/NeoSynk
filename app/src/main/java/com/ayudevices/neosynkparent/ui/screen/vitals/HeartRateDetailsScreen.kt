package com.ayudevices.neosynkparent.ui.screen.vitals

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
        containerColor = Color.Black
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Heart Rate",
                fontSize = 24.sp,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 8.dp) // reduced top padding to make it closer to the top
            )

            Spacer(modifier = Modifier.height(16.dp)) // Reduced vertical space here

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

            Spacer(modifier = Modifier.height(16.dp)) // Reduced vertical space here

            Text(
                text = "Results:",
                fontSize = 18.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp)) // Reduced vertical space here

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
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        val width = size.width
        val height = size.height
        val gridLines = 5

        val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

        // Draw horizontal grid lines
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

        val heartRatePoints = listOf(60, 80, 90, 70, 85, 75, 65, 110, 95, 105)
        val pointGap = width / (heartRatePoints.size - 1)
        val maxBPM = 120f
        val healthyRange = 60..100

        val scaledPoints = heartRatePoints.mapIndexed { i, bpm ->
            Offset(i * pointGap, height - (bpm / maxBPM) * height)
        }

        // Draw smooth curved path
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(scaledPoints.first().x, scaledPoints.first().y)
            for (i in 1 until scaledPoints.size) {
                val prev = scaledPoints[i - 1]
                val curr = scaledPoints[i]
                val midX = (prev.x + curr.x) / 2
                val midY = (prev.y + curr.y) / 2
                quadraticBezierTo(prev.x, prev.y, midX, midY)
            }
        }

        // Optional: Gradient could go here if needed (not trivial in Canvas)

        drawPath(
            path = path,
            color = Color.Cyan, // Use one color for smooth path (or gradient later)
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f)
        )

        // Draw data points as colored dots
        heartRatePoints.forEachIndexed { i, bpm ->
            val point = scaledPoints[i]
            val dotColor = if (bpm in healthyRange) Color.Green else Color.Red
            drawCircle(
                color = dotColor,
                center = point,
                radius = 6f
            )
        }
    }
}
