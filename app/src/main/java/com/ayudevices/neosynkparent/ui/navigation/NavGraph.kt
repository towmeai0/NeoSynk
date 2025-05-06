package com.ayudevices.neosynkparent.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ayudevices.neosynkparent.ui.screen.Screen
import com.ayudevices.neosynkparent.ui.screen.vitals.WeightDetailsScreen
import com.ayudevices.neosynkparent.ui.screen.auth.KidsLoginScreen
import com.ayudevices.neosynkparent.ui.screen.auth.LoginScreen
import com.ayudevices.neosynkparent.ui.screen.auth.OnboardingScreen
import com.ayudevices.neosynkparent.ui.screen.auth.SignupScreen
import com.ayudevices.neosynkparent.ui.screen.auth.SplashScreen
import com.ayudevices.neosynkparent.ui.screen.tabs.VitalTabScreen
import com.ayudevices.neosynkparent.ui.screen.dashboard.DiyaScreen
import com.ayudevices.neosynkparent.ui.screen.dashboard.DocsScreen
import com.ayudevices.neosynkparent.ui.screen.dashboard.HomeScreen
import com.ayudevices.neosynkparent.ui.screen.dashboard.MilestoneTab
import com.ayudevices.neosynkparent.ui.screen.dashboard.UploadScreen
import com.ayudevices.neosynkparent.ui.screen.vitals.HeartRateDetailsScreen
import com.ayudevices.neosynkparent.ui.screen.vitals.VitalsSPO2Screen
import com.google.firebase.auth.FirebaseAuth


@Composable
fun NeoSynkNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val startDestination = if (FirebaseAuth.getInstance().currentUser != null) {
        Screen.Home.route
    } else {
        Screen.SplashScreen.route
    }
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.OnboardingScreen.route) {
            OnboardingScreen(navController)
        }
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }
        composable(Screen.Signup.route) {
            SignupScreen(navController = navController)
        }
        composable(Screen.VitalTabScreen.route) {
            VitalTabScreen(navController)
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
        composable(Screen.MilestonesTab.route) {
            MilestoneTab(navController)
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
