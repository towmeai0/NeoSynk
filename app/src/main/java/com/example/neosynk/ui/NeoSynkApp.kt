package com.example.neosynk.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Monitor
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.neosynk.ui.navigation.NeoSynkNavHost
import kotlin.collections.contains

@Composable
fun NeoSynkApp() {
    val navController = rememberNavController()
    val darkBackground = Color(0xFF121212)
    var selectedBottom by remember { mutableIntStateOf(0) }
    val currentScreen = navController.currentBackStackEntryAsState().value?.destination?.route
    val bottomScreen = listOf(
        Screen.Home.route,
        Screen.UploadScreen.route,
        Screen.DocsScreen.route,
        Screen.DiyaScreen.route
    )
    Scaffold(
        containerColor = darkBackground,
        bottomBar = {
            // Hide bottom navigation on Login or SplashScreen
            if (currentScreen !in listOf(Screen.Login.route, Screen.SplashScreen.route , Screen.KidsLoginScreen.route ,
                    Screen.DiyaScreen.route)) {
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
        NeoSynkNavHost(navController = navController, modifier = Modifier.padding(innerPadding))
    }
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

// Sealed class for defining screen routes
sealed class Screen(val route: String) {

    object DiyaScreen : Screen("ChatScreen")
    object Home : Screen("home")
    object Login : Screen("login")
    object VitalTabScreen : Screen("vitals")
    object KidsLoginScreen : Screen("KidsLoginScreen")

    object UploadScreen : Screen("upload")
    object DocsScreen : Screen("pdf")

    object SplashScreen : Screen("SplashScreen")

    object HeartRateDetailsScreen : Screen("HeartRateDetailsScreen")
    object VitalsSPO2Screen :Screen ("VitalsSPO2Screen")
    object WeightDetailsScreen :Screen ("WeightDetailsScreen")

    companion object {
        val bottomScreens = listOf(
            Home,
            UploadScreen,
            DocsScreen,
            DiyaScreen
        )
    }
}
