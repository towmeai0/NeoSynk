package com.ayudevices.neosynkparent.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Monitor
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ayudevices.neosynkparent.R
import com.ayudevices.neosynkparent.ui.navigation.NeoSynkNavHost
import com.ayudevices.neosynkparent.ui.screen.Screen
import com.ayudevices.neosynkparent.ui.theme.darkBackground
import com.ayudevices.neosynkparent.ui.theme.orange
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.ui.draw.clip


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeoSynkApp(
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Determine if the BottomBar and TopBar should be shown based on the screen route
    val showTopAndBottomBar = currentDestination?.route !in listOf(
        Screen.Login.route,
        Screen.OnboardingScreen.route,
        Screen.Signup.route,
        Screen.SplashScreen.route,
        Screen.DiyaScreen.route,
        Screen.KidsLoginScreen.route,
        Screen.Profile.route
    )

    Scaffold(
        containerColor = darkBackground,
        topBar = {
            if (showTopAndBottomBar) {
                TopAppBar(
                    title = {
                        Text(
                            text = "NeoSynk",
                            color = Color.White,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    actions = {
                        IconButton(onClick = { /* Notifications action */ }) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.White)
                        }
                        IconButton(onClick = {  navController.navigate(Screen.Profileshow.route)
                        }) {
                            Icon(Icons.Default.Person, contentDescription = "Person", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
                )
            }
        },
        bottomBar = {
            if (showTopAndBottomBar) {
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
    val orange = Color(0xFFFF9800)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(percent = 90))
            .background(Color(0xFF1E1E1E))
            .border(width = 2.dp, color = orange, shape = RoundedCornerShape(percent = 90))
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(percent = 50))
                .background(Color.Transparent)
        ) {
            items.forEachIndexed { index, screen ->
                val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true

                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onItemSelected(screen) },
                    icon = {
                        if (screen.route == Screen.DiyaScreen.route) {
                            Icon(
                                painter = painterResource(id = R.drawable.diya_icon),
                                contentDescription = screen.route,
                                modifier = Modifier.size(30.dp),
                                tint = Color.Unspecified
                            )
                        } else {
                            Icon(
                                imageVector = icons[index],
                                contentDescription = screen.route,
                                tint = if (isSelected) orange else Color.White
                            )
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color.Transparent,
                        selectedIconColor = orange,
                        unselectedIconColor = Color.White
                    )
                )
            }
        }
    }
}
