package com.example.neosynk.ui.screen

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
