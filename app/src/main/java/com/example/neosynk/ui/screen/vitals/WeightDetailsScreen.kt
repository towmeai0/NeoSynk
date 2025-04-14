package com.example.neosynk.ui.screen.vitals

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController


@Composable
fun WeightDetailsScreen(navController: NavController) {
    val darkBackground = Color(0xFF121212)
    val white = Color.White
    val orange = Color(0xFFFF9800)
    val redLine = Color.Red

    Scaffold(
        containerColor = darkBackground,

        ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(darkBackground)
                .padding(16.dp)
        ) {
            // Top Title
            Text(
                text = "Vitals SPO2 rate",
                color = white,
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.Start)
            )
        }
    }
}