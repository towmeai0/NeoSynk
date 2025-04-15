package com.ayudevices.neosynkparent.ui.screen.tabs

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
    val vitalCards = listOf(
        VitalCardData(
            title = "Heart Rate",
            value = "76 bpm",
            icon = "📈",
            gradient = Brush.verticalGradient(listOf(Color(0xFF4CAF50), Color(0xFFA5D6A7))),
            navigateTo = "heartRateDetailsScreen"
        ),
        VitalCardData(
            title = "SpO₂ Level",
            value = "98%",
            icon = "📉",
            gradient = Brush.verticalGradient(listOf(Color(0xFFFFA726), Color(0xFFFFCC80))),
            navigateTo = "VitalsSPO2Screen"
        ),
        VitalCardData(
            title = "Weight",
            value = "49 kg",
            icon = "⚖️",
            gradient = Brush.verticalGradient(listOf(Color(0xFFE57373), Color(0xFFF8BBD0))),
            navigateTo = "WeightDetailsScreen"
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        vitalCards.forEach { data ->
            VitalsCard(
                title = data.title,
                value = data.value,
                icon = data.icon,
                gradient = data.gradient,
                onClick = { navController.navigate(data.navigateTo) }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun VitalsCard(
    title: String,
    value: String,
    icon: String,
    gradient: Brush,
    onClick: () -> Unit
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
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = icon,
                    fontSize = 40.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    text = value,
                    fontSize = 24.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

data class VitalCardData(
    val title: String,
    val value: String,
    val icon: String,
    val gradient: Brush,
    val navigateTo: String
)
