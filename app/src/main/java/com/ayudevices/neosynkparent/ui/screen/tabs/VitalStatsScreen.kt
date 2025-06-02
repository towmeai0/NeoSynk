package com.ayudevices.neosynkparent.ui.screen.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ayudevices.neosynkparent.data.model.HeartRate
import com.ayudevices.neosynkparent.data.model.HeightCm
import com.ayudevices.neosynkparent.data.model.Spo2
import com.ayudevices.neosynkparent.data.model.WeightKg
import com.ayudevices.neosynkparent.viewmodel.DocsViewModel
import com.ayudevices.neosynkparent.viewmodel.MilestoneViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun VitalTabScreen(
    navController: NavController,
    milestoneViewModel: MilestoneViewModel = hiltViewModel(),
    docsViewModel: DocsViewModel = hiltViewModel() // Keep if you still need other data from DocsViewModel
) {
    val context = LocalContext.current

    // Observe milestone data from API
    val milestoneData by milestoneViewModel.milestoneData.collectAsState()
    val isLoading by milestoneViewModel.isLoading.collectAsState()
    val error by milestoneViewModel.error.collectAsState()

    // Get userId - replace with your actual way of getting userId

    // Fetch data when screen loads
    LaunchedEffect(Unit) {
        FirebaseAuth.getInstance().currentUser?.uid?.let { milestoneViewModel.fetchMilestoneData(it) }
    }

    // Extract latest vital values from API response
    val latestHeartRate = milestoneData?.vital_trends?.heart_rate?.lastOrNull()?.value?.toString() ?: ""
    val latestSpo2 = milestoneData?.vital_trends?.spo2?.lastOrNull()?.value?.toString() ?: ""
    val latestWeight = milestoneData?.vital_trends?.weight_kg?.lastOrNull()?.value?.toString() ?: ""
    val latestHeight = milestoneData?.vital_trends?.height_cm?.lastOrNull()?.value?.toString() ?: ""

    val vitalCards = listOf(
        VitalCardData(
            title = "Heart Rate",
            value = if (latestHeartRate.isNotEmpty()) "$latestHeartRate BPM" else "N/A",
            icon = "📈",
            gradient = Brush.verticalGradient(listOf(Color(0xFF4CAF50), Color(0xFFA5D6A7))),
            navigateTo = "heartRateDetailsScreen"
        ),
        VitalCardData(
            title = "SpO₂ Level",
            value = if (latestSpo2.isNotEmpty()) "$latestSpo2%" else "N/A",
            icon = "📉",
            gradient = Brush.verticalGradient(listOf(Color(0xFFFFA726), Color(0xFFFFCC80))),
            navigateTo = "VitalsSPO2Screen"
        ),
        VitalCardData(
            title = "Weight & Height",
            value = when {
                latestWeight.isNotEmpty() && latestHeight.isNotEmpty() -> "$latestWeight kg / $latestHeight cm"
                latestWeight.isNotEmpty() -> "$latestWeight kg / N/A"
                latestHeight.isNotEmpty() -> "N/A / $latestHeight cm"
                else -> "N/A"
            },
            icon = "⚖️",
            gradient = Brush.verticalGradient(listOf(Color(0xFFE57373), Color(0xFFF8BBD0))),
            navigateTo = "WeightDetailsScreen"
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Show loading indicator
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        } else if (error != null) {
            // Show error message
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Error loading vital data",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error ?: "Unknown error",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { FirebaseAuth.getInstance().currentUser?.uid?.let {
                        milestoneViewModel.fetchMilestoneData(
                            it
                        )
                    } },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Text(text = "Retry", color = Color.Black)
                }
            }
        } else {
            // Show vital cards
            vitalCards.forEach { data ->
                VitalsCard(
                    title = data.title,
                    value = data.value,
                    icon = data.icon,
                    gradient = data.gradient,
                    onClick = {
                        navController.navigate(data.navigateTo)
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

// Enhanced VitalsCard with loading state support
@Composable
fun VitalsCard(
    title: String,
    value: String,
    icon: String,
    gradient: Brush,
    onClick: () -> Unit,
    isLoading: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(brush = gradient)
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = icon,
                    fontSize = 40.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )

                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = value,
                        fontSize = 24.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

data class VitalCardData(
    val title: String,
    val value: String,
    val icon: String,
    val gradient: Brush,
    val navigateTo: String
)
