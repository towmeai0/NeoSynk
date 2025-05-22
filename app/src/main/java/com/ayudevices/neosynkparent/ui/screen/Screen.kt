package com.ayudevices.neosynkparent.ui.screen

sealed class Screen(val route: String) {

    object DiyaScreen : Screen("ChatScreen")
    object Home : Screen("home")
    object Login : Screen("login")
    object Profile : Screen("profile")
    object Profileshow : Screen("profiler")
    object Signup : Screen("signup")
    object OnboardingScreen :Screen ("Onboarding")
    object VitalTabScreen : Screen("vitals")
    object MilestonesTab : Screen("milestone")
    object LiveFeedTab : Screen("LiveFeedTab")
    object KidsLoginScreen : Screen("KidsLoginScreen")
    object UploadScreen : Screen("upload")
    object DocsScreen : Screen("pdf")
    object SplashScreen : Screen("SplashScreen")
    object HeartRateDetailsScreen : Screen("HeartRateDetailsScreen")
    object VitalsSPO2Screen :Screen ("VitalsSPO2Screen")
    object WeightDetailsScreen :Screen ("WeightDetailsScreen")
    object  MilestoneQues: Screen(" MilestoneQues")

    companion object {
        val bottomScreens = listOf(
            Home,
            UploadScreen,
            DocsScreen,
            DiyaScreen
        )
    }
}
