package com.example.neosynk.ui.screen.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.neosynk.ui.Screen
import com.example.neosynk.ui.screen.tabs.VitalTabScreen

@Composable
fun HomeScreen(
    navController: NavController
) {
    val darkBackground = Color(0xFF121212)
    val orange = Color(0xFFFF9800)
    var selectedTab by remember { mutableStateOf("Live Feed") }

    Scaffold(
        containerColor = darkBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(darkBackground)
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Title + Bot Button Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "NeoSynk",
                    color = Color.White,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Button(
                    onClick = {
                        // TODO: Add your Bot button action here
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = orange,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(50),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("Bot")
                }
            }


            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("Live Feed", "Vitals", "Milestones").forEach { tab ->
                    Button(
                        onClick = { selectedTab = tab },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedTab == tab) orange else Color.DarkGray,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                    ) {
                        Text(tab)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            when (selectedTab) {
                "Live Feed" -> LiveFeedTab()
                "Vitals" -> VitalsTab(navController)
                "Milestones" -> MilestonesTab()
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .border(BorderStroke(2.dp, orange), shape = RoundedCornerShape(16.dp))
                    .background(darkBackground, shape = RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("Dynamic Content for \"$selectedTab\"", color = Color.White)
            }
        }
    }
}

@Composable
fun LiveFeedTab() {
    Text(text = "Live Feed Content", color = Color.White)
}

@Composable
fun VitalsTab(navController: NavController) {
    VitalTabScreen(navController = navController)
}

@Composable
fun MilestonesTab() {
    Text(text = "Milestones Content", color = Color.White)
}


