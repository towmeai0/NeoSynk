package com.ayudevices.neosynkparent.ui.screen.vitals

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController

@Composable
fun VitalsSPO2Screen(navController: NavController) {
    var selectedTab by remember { mutableStateOf("") }
    val darkBackground = Color(0xFF121212)
    val white = Color.White
    val orange = Color(0xFFFF9800)
    val redLine = Color.Red

    val sampleSPO2Data = listOf(0.1f, 0.3f, 0.6f, 0.4f, 0.7f, 0.8f, 0.9f)  // Example data; replace with actual dynamic data
    val healthyRange = 95..100  // Example healthy SPO2 range in percentage

    Scaffold(
        containerColor = darkBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(darkBackground)
                .padding(16.dp)
        ) {

            // Main Title
            Text(
                text = "SPO2",
                color = white,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Graph Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                border = BorderStroke(2.dp, orange)
            ) {
                Canvas(modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)) {

                    val widthStep = size.width / (sampleSPO2Data.size - 1)
                    val height = size.height

                    // Define the healthy SPO2 range (95% to 100%)
                    val healthyRange = 95f..100f

                    for (i in 0 until sampleSPO2Data.size - 1) {
                        // Convert the value to percentage by multiplying by 100
                        val spo2Percentage = sampleSPO2Data[i] * 100

                        // Customize line color based on SPO2 level
                        val color = if (spo2Percentage in healthyRange) {
                            Color.Green // Healthy range
                        } else {
                            redLine // Unhealthy range
                        }

                        drawLine(
                            color = color,
                            start = Offset(x = i * widthStep, y = height * (1 - sampleSPO2Data[i])),
                            end = Offset(x = (i + 1) * widthStep, y = height * (1 - sampleSPO2Data[i + 1])),
                            strokeWidth = 4f
                        )
                    }
                }

            }

            Spacer(modifier = Modifier.height(24.dp))

            // Results Section
            Text(
                text = "Results:",
                color = white,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "The SPO2 rate graph displays the blood oxygen level\n" +
                        "fluctuations over time. A healthy SPO2 level is\n" +
                        "typically between 95% and 100%. This data helps\n" +
                        "monitor respiratory efficiency and detect early\n" +
                        "signs of breathing issues or fatigue.",
                color = white,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}
