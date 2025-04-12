package com.example.neosynk.ui.screen.TabScreen


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.navigation.NavController

@Composable
fun VitalTabScreen(navController: NavController) {
    var selectedTab by remember { mutableStateOf("Vitals") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            VitalsCard(
                title = "Level of HeartRate",
                gradient = Brush.verticalGradient(listOf(Color(0xFF4CAF50), Color(0xFFA5D6A7))),
                onClick = { navController.navigate("heartRateDetailsScreen") }

            ) {
                Text("📈", fontSize = 48.sp, color = Color.Red)
            }

            VitalsCard(
                title = "Level Of O2",
                gradient = Brush.verticalGradient(listOf(Color(0xFFFFA726), Color(0xFFFFCC80))),
                onClick = { navController.navigate("VitalsSPO2Screen") }
            ) {
                Text("📉", fontSize = 48.sp, color = Color.Red)
            }

            VitalsCard(
                title = "Weight",
                gradient = Brush.verticalGradient(listOf(Color(0xFFE57373), Color(0xFFF8BBD0))),
                onClick = { navController.navigate("WeightDetailsScreen") }
            ) {
                Text("49 ", fontSize = 20.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun VitalsCard(
    title: String,
    gradient: Brush,
    onClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(brush = gradient)
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(text = title, color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}
