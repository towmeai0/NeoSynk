package com.ayudevices.neosynkparent.ui.screen.tabs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.ayudevices.neosynkparent.ui.theme.darkBackground
import com.ayudevices.neosynkparent.ui.theme.InactiveProgress
import com.ayudevices.neosynkparent.ui.theme.LightDarkBackground
import com.ayudevices.neosynkparent.ui.theme.LightGrayText
import com.ayudevices.neosynkparent.ui.theme.WarmOrange
import com.ayudevices.neosynkparent.ui.theme.WhiteText

@Composable
fun MilestonesTab(navController: NavController) {
    var selectedTab by remember { mutableStateOf("0-2 Months") }
    val ageTabs = listOf("0-2 Months", "4-6 Months", "7-9 Months", "10-12 Months")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBackground)
            .padding(16.dp)
    ) {
        // Overall Progress Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = LightDarkBackground),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Overall Progress",
                    color = LightGrayText,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Circular Progress
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(120.dp)
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
                                sweepAngle = 0f, // change this to real progress
                                useCenter = false,
                                style = Stroke(width = 12f, cap = StrokeCap.Round)
                            )
                        }
                        Text(
                            text = "0%",
                            color = WhiteText,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // 2x2 Grid of Milestones
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            MilestoneStatCard("Motor", "0%")
                            MilestoneStatCard("Sensory", "0%")
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            MilestoneStatCard("Communication", "0%")
                            MilestoneStatCard("Feeding", "0%")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Age Tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ageTabs.forEach { label ->
                val isSelected = label == selectedTab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) WarmOrange else darkBackground)
                        .border(
                            width = 1.dp,
                            color = WarmOrange,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable { selectedTab = label }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = WhiteText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Milestone Placeholders
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(3) {
                MilestonePlaceholder()
            }
        }
    }
}

@Composable
fun MilestoneStatCard(label: String, percent: String) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .height(50.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = darkBackground),
        border = BorderStroke(1.dp, WarmOrange)
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(text = label, color = WhiteText, fontSize = 12.sp)
            Text(text = percent, color = WhiteText, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun MilestonePlaceholder() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(LightDarkBackground)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .border(2.dp, WarmOrange, CircleShape)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(darkBackground)
        )
    }
}
