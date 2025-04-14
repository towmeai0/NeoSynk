package com.example.neosynk.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.neosynk.ui.screen.vitals.WeightDetailsScreen
import com.example.neosynk.ui.Screen
import com.example.neosynk.ui.screen.auth.KidsLoginScreen
import com.example.neosynk.ui.screen.auth.LoginScreen
import com.example.neosynk.ui.screen.auth.SplashScreen
import com.example.neosynk.ui.screen.tabs.VitalTabScreen
import com.example.neosynk.ui.screen.dashboard.DiyaScreen
import com.example.neosynk.ui.screen.dashboard.DocsScreen
import com.example.neosynk.ui.screen.dashboard.HomeScreen
import com.example.neosynk.ui.screen.dashboard.UploadScreen
import com.example.neosynk.ui.screen.vitals.HeartRateDetailsScreen
import com.example.neosynk.ui.screen.vitals.VitalsSPO2Screen

@Composable
fun NeoSynkNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.SplashScreen.route,
        modifier = modifier
    ) {
        composable(Screen.Login.route) {
            LoginScreen(navController)
        }
        composable(Screen.Home.route) {
            HomeScreen(navController)
        }
        composable(Screen.KidsLoginScreen.route) {
            KidsLoginScreen(navController)
        }
        composable(Screen.VitalTabScreen.route) {
            VitalTabScreen(navController)
        }
        composable(Screen.UploadScreen.route) {
            UploadScreen(navController)
        }
        composable(Screen.SplashScreen.route) {
            SplashScreen(navController)
        }
        composable(Screen.DocsScreen.route) {
            DocsScreen(navController)
        }
        composable(Screen.DiyaScreen.route) {
            DiyaScreen(navController)
        }
        composable(Screen.HeartRateDetailsScreen.route) {
            HeartRateDetailsScreen(navController)
        }
        composable(Screen.VitalsSPO2Screen.route) {
            VitalsSPO2Screen(navController)
        }
        composable(Screen.WeightDetailsScreen.route) {
            WeightDetailsScreen(navController)
        }
    }
}
