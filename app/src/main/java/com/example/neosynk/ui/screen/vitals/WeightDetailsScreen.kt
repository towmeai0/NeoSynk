package com.example.neosynk.ui.screen.vitals

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.neosynk.ui.theme.orange
import com.example.neosynk.ui.theme.white

@Composable
fun WeightDetailsScreen(navController: NavController) {
    val darkBackground = Color(0xFF121212)
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
            // Top Title
            Text(
                text = "Vitals SPO2 rate",
                color = white,
                fontSize = 24.sp,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // SPO2 Data Display
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E1E1E))
                        .padding(16.dp)
                ) {
                    // SPO2 level
                    Text(
                        text = "SPO2 Level:",
                        color = white,
                        fontSize = 16.sp
                    )

                    Text(
                        text = "98%", // Example data; this can be dynamic
                        color = orange,
                        fontSize = 28.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // SPO2 level status
                    Text(
                        text = "Status: Normal",
                        color = white.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Optional: Display more information
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E1E1E))
                        .padding(16.dp)
                ) {
                    // Weight data
                    Text(
                        text = "Weight Details:",
                        color = white,
                        fontSize = 16.sp
                    )

                    Text(
                        text = "72 kg", // Example data; dynamic input expected here
                        color = orange,
                        fontSize = 28.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Weight status
                    Text(
                        text = "Status: Healthy",
                        color = white.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
