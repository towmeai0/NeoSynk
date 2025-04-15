package com.ayudevices.neosynkparent.ui

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ayudevices.neosynkparent.ui.navigation.NeoSynkNavHost
import com.ayudevices.neosynkparent.ui.screen.Screen
import com.ayudevices.neosynkparent.ui.theme.darkBackground
import com.ayudevices.neosynkparent.ui.theme.orange

@Composable
fun NeoSynkApp(
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = when (currentDestination?.route) {
        Screen.Login.route,
        Screen.SplashScreen.route,
        Screen.DiyaScreen.route,
        Screen.KidsLoginScreen.route -> false
        else -> true
    }

    Scaffold(
        containerColor = darkBackground,
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(
                    currentDestination = currentDestination,
                    onItemSelected = { screen ->
                        navController.navigate(screen.route) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { contentPadding ->
        NeoSynkNavHost(
            navController = navController,
            modifier = Modifier.padding(contentPadding)
        )
    }
}

@Composable
fun BottomNavigationBar(
    currentDestination: NavDestination?,
    onItemSelected: (Screen) -> Unit
) {
    val items = Screen.bottomScreens
    val icons = listOf(
        Icons.Filled.Home,
        Icons.Filled.ArrowUpward,
        Icons.Filled.Description,
        Icons.Filled.Monitor
    )

    NavigationBar(containerColor = Color(0xFF1E1E1E)) {
        items.forEachIndexed { index, screen ->
            val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true

            NavigationBarItem(
                selected = isSelected,
                onClick = { onItemSelected(screen) },
                icon = {
                    Icon(
                        icons[index],
                        contentDescription = screen.route,
                        tint = if (isSelected) orange else Color.White
                    )
                }
            )
        }
    }
}