package com.ayudevices.neosynkparent.ui.screen.tabs

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ayudevices.neosynkparent.ui.screen.Screen
import com.ayudevices.neosynkparent.ui.theme.*
import com.ayudevices.neosynkparent.viewmodel.MilestoneViewModel

@Composable
fun MilestonesTab(
    navController: NavController,
    milestoneViewModel: MilestoneViewModel = hiltViewModel(),
    userId: String // Pass this parameter
) {
    val overallProgress by milestoneViewModel.overallProgress.collectAsState()
    val motorProgress by milestoneViewModel.motorProgress.collectAsState()
    val sensoryProgress by milestoneViewModel.sensoryProgress.collectAsState()
    val cognitiveProgress by milestoneViewModel.cognitiveProgress.collectAsState()
    val feedingProgress by milestoneViewModel.feedingProgress.collectAsState()
    val milestoneReport by milestoneViewModel.milestoneReport.collectAsState()
    val isLoading by milestoneViewModel.isLoading.collectAsState()
    val error by milestoneViewModel.error.collectAsState()

    // Fetch data when composable is first created
    LaunchedEffect(userId) {
        milestoneViewModel.fetchMilestoneData(userId)
    }

    // Handle error state
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            // Show error to user (snackbar, toast, etc.)
            milestoneViewModel.clearError()
        }
    }

    val sweepAngle = ((overallProgress / 100.0) * 360.0).toFloat()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBackground)
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = WarmOrange)
            }
        } else {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Overall Progress Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = LightDarkBackground),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Progress Circle
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(120.dp)
                            ) {
                                Canvas(modifier = Modifier.size(120.dp)) {
                                    drawArc(
                                        color = InactiveProgress,
                                        startAngle = 0f,
                                        sweepAngle = 360f,
                                        useCenter = false,
                                        style = Stroke(width = 12f, cap = StrokeCap.Round)
                                    )
                                    drawArc(
                                        color = WarmOrange,
                                        startAngle = -90f,
                                        sweepAngle = sweepAngle,
                                        useCenter = false,
                                        style = Stroke(width = 12f, cap = StrokeCap.Round)
                                    )
                                }
                                Text(
                                    text = "${overallProgress.toInt()}%",
                                    color = WhiteText,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Trophy Icon
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(darkBackground),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = "Trophy Icon",
                                    tint = WarmOrange,
                                    modifier = Modifier.size(60.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Progress Cards with Navigation
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                MilestoneStatCard(
                                    title = "Motor",
                                    progress = "${motorProgress.toInt()}%",
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        navController.navigate(
                                            Screen.MilestoneDetail.createRoute("Motor", userId)
                                        )
                                    }
                                )
                                MilestoneStatCard(
                                    title = "Sensory",
                                    progress = "${sensoryProgress.toInt()}%",
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        navController.navigate(
                                            Screen.MilestoneDetail.createRoute("Sensory", userId)
                                        )
                                    }
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                MilestoneStatCard(
                                    title = "Cognitive",
                                    progress = "${cognitiveProgress.toInt()}%",
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        navController.navigate(
                                            Screen.MilestoneDetail.createRoute("Cognitive", userId)
                                        )
                                    }
                                )
                                MilestoneStatCard(
                                    title = "Feeding",
                                    progress = "${feedingProgress.toInt()}%",
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        navController.navigate(
                                            Screen.MilestoneDetail.createRoute("Feeding", userId)
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Milestone Report Card
                if (milestoneReport.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = LightDarkBackground),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Milestone Report",
                                color = WarmOrange,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = milestoneReport,
                                color = WhiteText,
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun MilestoneStatCard(
    title: String,
    progress: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .height(60.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = darkBackground),
        border = BorderStroke(2.dp, WarmOrange)
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = title,
                color = WhiteText,
                fontSize = 12.sp
            )
            Text(
                text = progress,
                color = WhiteText,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }
    }
}