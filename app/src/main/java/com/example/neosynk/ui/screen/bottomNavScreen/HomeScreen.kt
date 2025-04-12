package com.example.neosynk.ui.screen.bottomNavScreen

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
import com.example.neosynk.ui.screen.TabScreen.VitalTabScreen

@Composable
fun HomeScreen(
    navController: NavController
) {
    val darkBackground = Color(0xFF121212)
    val orange = Color(0xFFFF9800)
    var selectedTab by remember { mutableStateOf("Live Feed") }
    var selectedBottom by remember { mutableIntStateOf(0) }

    val bottomScreen = listOf(
        Screen.Home.route,
        Screen.UploadScreen.route,
        Screen.DocsScreen.route,
        Screen.DiyaScreen.route
    )

    // Track the current screen route
    val currentScreen = navController.currentBackStackEntryAsState().value?.destination?.route

    Scaffold(
        containerColor = darkBackground,
        bottomBar = {
            // Hide bottom navigation on Login or SplashScreen
            if (currentScreen !in listOf(Screen.Login.route, Screen.SplashScreen.route)) {
                BottomNavigationBar(selectedBottom) { index ->
                    selectedBottom = index
                    navController.navigate(bottomScreen[index]) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        }
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

@Composable
fun BottomNavigationBar(selectedItem: Int, onItemSelected: (Int) -> Unit) {
    val orange = Color(0xFFFF9800)
    // Set a solid background for the Bottom Navigation Bar
    NavigationBar(containerColor = Color(0xFF1E1E1E)) {
        val items = listOf(
            Icons.Filled.Home,
            Icons.Filled.ArrowUpward,
            Icons.Filled.Description,
            Icons.Filled.Monitor
        )
        items.forEachIndexed { index, icon ->
            NavigationBarItem(
                selected = selectedItem == index,
                onClick = { onItemSelected(index) },
                icon = {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = if (selectedItem == index) orange else Color.White
                    )
                }
            )
        }
    }
}
