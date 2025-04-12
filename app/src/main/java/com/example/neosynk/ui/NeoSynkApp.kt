package com.example.neosynk.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.neosynk.ui.navigation.NeoSynkNavHost

@Composable
fun NeoSynkApp() {
    val navController = rememberNavController()
    Surface(modifier = Modifier.fillMaxSize()) {
        NeoSynkNavHost(navController = navController)

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
    object weightDetailsScreen :Screen ("WeightDetailsScreen")



    companion object {
        val bottomScreens = listOf(
            Home,
            UploadScreen,
            DocsScreen,
            DiyaScreen
        )
    }
}
