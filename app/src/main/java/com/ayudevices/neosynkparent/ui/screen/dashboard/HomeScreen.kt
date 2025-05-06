package com.ayudevices.neosynkparent.ui.screen.dashboard

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ayudevices.neosynkparent.ui.screen.Screen
import com.ayudevices.neosynkparent.ui.screen.Tab
import com.ayudevices.neosynkparent.ui.screen.tabs.LiveTab
import com.ayudevices.neosynkparent.ui.screen.tabs.MilestonesTab
import com.ayudevices.neosynkparent.ui.screen.tabs.VitalTabScreen
import com.ayudevices.neosynkparent.ui.theme.darkBackground
import com.ayudevices.neosynkparent.ui.theme.orange
import com.ayudevices.neosynkparent.viewmodel.AuthViewModel
import com.ayudevices.neosynkparent.viewmodel.HomeViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val selectedTab = viewModel.selectedTab.collectAsState()

    LaunchedEffect(Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.Home.route) { inclusive = true }
            }
        }
    }


    Log.d("UserId" ,
        "Email : ${authViewModel.getCurrentUser()?.email} \n," +
                " Uid : ${authViewModel.getCurrentUser()?.uid} \n," +
                "Tenantid : ${authViewModel.getCurrentUser()?.tenantId} \n,")


    Scaffold(
        containerColor = darkBackground,

    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(darkBackground)
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            TabSection(
                selectedTab = selectedTab.value,
                onTabSelected = { viewModel.selectTab(it) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            when (selectedTab.value) {
                Tab.LIVE_FEED -> LiveFeedTab(navController)
                Tab.VITALS -> VitalsTab(navController)
                Tab.MILESTONES -> MilestonesTab(navController)
            }

            Spacer(modifier = Modifier.height(24.dp))

            DynamicContent(selectedTab = selectedTab.value)
        }
    }
}



@Composable
fun TabSection(selectedTab: Tab, onTabSelected: (Tab) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Tab.values().forEach { tab ->
            Button(
                onClick = { onTabSelected(tab) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedTab == tab) orange else Color.DarkGray,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .padding(horizontal = 4.dp)
            ) {
                Text(tab.label, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun DynamicContent(selectedTab: Tab) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .border(BorderStroke(2.dp, orange), shape = RoundedCornerShape(16.dp))
            .background(darkBackground, shape = RoundedCornerShape(16.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Dynamic Content for \"${selectedTab.label}\"",
            color = Color.White,
            fontSize = 16.sp
        )
    }
}

@Composable
fun LiveFeedTab(navController: NavController) {
    LiveTab(navController = navController)
}

@Composable
fun VitalsTab(navController: NavController) {
    VitalTabScreen(navController = navController)
}

@Composable
fun MilestoneTab(navController: NavController) {
    MilestonesTab(navController = navController)
}
