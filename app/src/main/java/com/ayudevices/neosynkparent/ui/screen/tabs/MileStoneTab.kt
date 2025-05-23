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
import com.ayudevices.neosynkparent.ui.theme.*
import com.ayudevices.neosynkparent.viewmodel.MilestoneViewModel

@Composable
fun MilestonesTab(navController: NavController, milestoneViewModel: MilestoneViewModel = hiltViewModel()) {
    val overallProgress by milestoneViewModel.overallProgress.collectAsState()
    val motorProgress by milestoneViewModel.motorProgress.collectAsState()
    val sensoryProgress by milestoneViewModel.sensoryProgress.collectAsState()
    val communicationProgress by milestoneViewModel.communicationProgress.collectAsState()
    val feedingProgress by milestoneViewModel.feedingProgress.collectAsState()

    val sweepAngle = (overallProgress / 100f) * 360f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBackground)
    ) {
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
                                text = "$overallProgress%",
                                color = WhiteText,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Trophy Icon with same size as progress circle
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
                                modifier = Modifier.size(150.dp) // Adjust icon size inside the box
                            )
                        }
                    }
                }

                    // Spacer between progress and stat cards
                    Spacer(modifier = Modifier.height(16.dp))

                    // Two rows of stat cards
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            MilestoneStatCard(
                                title = "Motor",
                                progress = "$motorProgress%",
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { navController.navigate(" MilestoneQues") }
                            )
                            MilestoneStatCard(
                                title = "Sensory",
                                progress = "$sensoryProgress%",
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { navController.navigate(" MilestoneQues") }
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            MilestoneStatCard(
                                title = "Communication",
                                progress = "$communicationProgress%",
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { navController.navigate(" MilestoneQues") }
                            )
                            MilestoneStatCard(
                                title = "Feeding",
                                progress = "$feedingProgress%",
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { navController.navigate(" MilestoneQues") }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }


@Composable
fun MilestoneStatCard(
    title: String,
    progress: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(60.dp),
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
