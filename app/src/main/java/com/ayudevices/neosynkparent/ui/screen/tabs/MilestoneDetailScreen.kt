package com.ayudevices.neosynkparent.ui.screen.tabs

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ayudevices.neosynkparent.data.model.*
import com.ayudevices.neosynkparent.ui.theme.*
import com.ayudevices.neosynkparent.viewmodel.MilestoneViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MilestoneDetailScreen(
    navController: NavController,
    category: String,
    userId: String,
    milestoneViewModel: MilestoneViewModel = hiltViewModel()
) {
    val milestoneData by milestoneViewModel.milestoneData.collectAsState()
    val isLoading by milestoneViewModel.isLoading.collectAsState()
    val error by milestoneViewModel.error.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }

    // Fetch data when composable is first created
    LaunchedEffect(userId) {
        milestoneViewModel.fetchMilestoneData(userId)
    }

    // Handle error state
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            milestoneViewModel.clearError()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBackground)
    ) {
        // Top Bar
        TopAppBar(
            title = {
                Text(
                    text = "$category Milestones",
                    color = WhiteText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = WhiteText
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = LightDarkBackground
            )
        )

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = WarmOrange)
            }
        } else {
            milestoneData?.let { data ->
                // Get category-specific data
                val categoryData = when (category) {
                    "Motor" -> CategoryData(
                        percentage = data.milestone_results.Motor.percentage,
                        completed = data.milestone_results.Motor.completed,
                        pending = data.milestone_results.Motor.pending
                    )
                    "Sensory" -> CategoryData(
                        percentage = data.milestone_results.Sensory.percentage,
                        completed = data.milestone_results.Sensory.completed,
                        pending = data.milestone_results.Sensory.pending
                    )
                    "Cognitive" -> CategoryData(
                        percentage = data.milestone_results.Cognitive.percentage,
                        completed = data.milestone_results.Cognitive.completed,
                        pending = data.milestone_results.Cognitive.pending
                    )
                    "Feeding" -> CategoryData(
                        percentage = data.milestone_results.Feeding.percentage,
                        completed = data.milestone_results.Feeding.completed,
                        pending = data.milestone_results.Feeding.pending
                    )
                    else -> null
                }

                categoryData?.let { details ->
                    Column {
                        // Progress Header
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            colors = CardDefaults.cardColors(containerColor = LightDarkBackground),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = category,
                                        color = WarmOrange,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Overall Progress",
                                        color = WhiteText,
                                        fontSize = 14.sp
                                    )
                                }

                                // Progress Circle
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.size(80.dp)
                                ) {
                                    val sweepAngle = ((details.percentage / 100.0) * 360.0).toFloat()
                                    Canvas(modifier = Modifier.size(80.dp)) {
                                        drawArc(
                                            color = InactiveProgress,
                                            startAngle = 0f,
                                            sweepAngle = 360f,
                                            useCenter = false,
                                            style = Stroke(width = 8f, cap = StrokeCap.Round)
                                        )
                                        drawArc(
                                            color = WarmOrange,
                                            startAngle = -90f,
                                            sweepAngle = sweepAngle,
                                            useCenter = false,
                                            style = Stroke(width = 8f, cap = StrokeCap.Round)
                                        )
                                    }
                                    Text(
                                        text = "${details.percentage.toInt()}%",
                                        color = WhiteText,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Tab Row
                        TabRow(
                            selectedTabIndex = selectedTabIndex,
                            containerColor = LightDarkBackground,
                            contentColor = WarmOrange,
                            indicator = { tabPositions ->
                                TabRowDefaults.Indicator(
                                    Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                    color = WarmOrange
                                )
                            }
                        ) {
                            Tab(
                                selected = selectedTabIndex == 0,
                                onClick = { selectedTabIndex = 0 },
                                text = {
                                    Text(
                                        text = "Completed (${details.completed.size})",
                                        color = if (selectedTabIndex == 0) WarmOrange else WhiteText
                                    )
                                }
                            )
                            Tab(
                                selected = selectedTabIndex == 1,
                                onClick = { selectedTabIndex = 1 },
                                text = {
                                    Text(
                                        text = "Pending (${details.pending.size})",
                                        color = if (selectedTabIndex == 1) WarmOrange else WhiteText
                                    )
                                }
                            )
                        }

                        // Tab Content
                        when (selectedTabIndex) {
                            0 -> MilestoneQuestionsList(
                                questions = details.completed,
                                isCompleted = true
                            )
                            1 -> MilestoneQuestionsList(
                                questions = details.pending,
                                isCompleted = false
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MilestoneQuestionsList(
    questions: List<String>,
    isCompleted: Boolean
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (questions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = LightDarkBackground),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isCompleted) "No completed milestones yet" else "No pending milestones",
                            color = WhiteText.copy(alpha = 0.6f),
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(questions) { question ->
                MilestoneQuestionCard(
                    question = question,
                    isCompleted = isCompleted
                )
            }
        }
    }
}

@Composable
fun MilestoneQuestionCard(
    question: String,
    isCompleted: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted)
                LightDarkBackground.copy(alpha = 0.8f)
            else
                darkBackground
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (isCompleted)
            BorderStroke(1.dp, WarmOrange.copy(alpha = 0.3f))
        else
            BorderStroke(1.dp, WhiteText.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Status Icon
            Icon(
                imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = if (isCompleted) "Completed" else "Pending",
                tint = if (isCompleted) WarmOrange else WhiteText.copy(alpha = 0.6f),
                modifier = Modifier.size(24.dp)
            )

            // Question Text
            Text(
                text = question,
                color = WhiteText,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}