package com.example.neosynk.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.neosynk.ui.screen.vitalsScreen.WeightDetailsScreen
import com.example.neosynk.ui.Screen
import com.example.neosynk.ui.screen.*
import com.example.neosynk.ui.screen.TabScreen.VitalTabScreen
import com.example.neosynk.ui.screen.bottomNavScreen.DiyaScreen
import com.example.neosynk.ui.screen.bottomNavScreen.DocsScreen
import com.example.neosynk.ui.screen.bottomNavScreen.HomeScreen
import com.example.neosynk.ui.screen.bottomNavScreen.UploadScreen
import com.example.neosynk.ui.screen.vitalsScreen.HeartRateDetailsScreen
import com.example.neosynk.ui.screen.vitalsScreen.VitalsSPO2Screen

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
        composable("heartRateDetailsScreen") { HeartRateDetailsScreen(navController) }
        composable("VitalsSPO2Screen") { VitalsSPO2Screen(navController) }
        composable("weightDetailsScreen") { WeightDetailsScreen(navController) }

    }
}
