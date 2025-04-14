package com.example.neosynk.ui.screen.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.neosynk.ui.screen.Tab
import com.example.neosynk.ui.screen.tabs.VitalTabScreen
import com.example.neosynk.ui.theme.darkBackground
import com.example.neosynk.ui.theme.orange
import com.example.neosynk.viewmodel.HomeViewModel

@Composable
fun HomeScreen(navController: NavController, viewModel: HomeViewModel = hiltViewModel()) {
    val selectedTab = viewModel.selectedTab.collectAsState()

    Scaffold(containerColor = darkBackground) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(darkBackground)
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            HeaderSection(onBotClick = { /* TODO: Add your Bot button action */ })

            Spacer(modifier = Modifier.height(16.dp))

            TabSection(
                selectedTab = selectedTab.value,
                onTabSelected = { viewModel.selectTab(it) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            when (selectedTab.value) {
                Tab.LIVE_FEED -> LiveFeedTab()
                Tab.VITALS -> VitalsTab(navController)
                Tab.MILESTONES -> MilestonesTab()
            }

            Spacer(modifier = Modifier.height(16.dp))

            DynamicContent(selectedTab = selectedTab.value)
        }
    }
}

@Composable
fun HeaderSection(onBotClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "NeoSynk",
            color = Color.White,
            fontSize = 38.sp,
            fontWeight = FontWeight.SemiBold
        )
        Button(
            onClick = onBotClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = orange,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(50),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text("Bot")
        }
    }
}

@Composable
fun TabSection(selectedTab: Tab, onTabSelected: (Tab) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
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
                    .padding(horizontal = 4.dp)
            ) {
                Text(tab.label)
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
            .background(darkBackground, shape = RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text("Dynamic Content for \"${selectedTab.label}\"", color = Color.White)
    }
}

@Composable
fun LiveFeedTab() {
    Text(text = "Live Feed Content", color = Color.White)
}

@Composable
fun VitalsTab(navController: NavController) {
    VitalTabScreen(navController = navController)
}

@Composable
fun MilestonesTab() {
    Text(text = "Milestones Content", color = Color.White)
}