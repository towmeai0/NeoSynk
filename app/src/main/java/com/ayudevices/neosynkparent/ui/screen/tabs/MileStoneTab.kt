package com.ayudevices.neosynkparent.ui.screen.tabs

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons

import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
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
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun MilestonesTab(navController: NavController, milestoneViewModel: MilestoneViewModel = hiltViewModel()){
    val overallProgress by milestoneViewModel.overallProgress.collectAsState()
    val motorProgress by milestoneViewModel.motorProgress.collectAsState()
    val sensoryProgress by milestoneViewModel.sensoryProgress.collectAsState()
    val communicationProgress by milestoneViewModel.communicationProgress.collectAsState()
    val feedingProgress by milestoneViewModel.feedingProgress.collectAsState()

    val currentLeap by milestoneViewModel.currentLeap.collectAsState()
    val currentCategory by milestoneViewModel.currentCategory.collectAsState()
    val currentQuestionIndex by milestoneViewModel.currentQuestionIndex.collectAsState()

    val currentQuestions by remember(currentLeap, currentCategory) {
        derivedStateOf { milestoneViewModel.getCurrentQuestions(currentLeap, currentCategory) }
    }

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
                    Text(
                        text = "Overall Progress",
                        color = LightGrayText,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
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

                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                MilestoneStatCard("Motor", "$motorProgress%")
                                MilestoneStatCard("Sensory", "$sensoryProgress%")
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                MilestoneStatCard("Communication", "$communicationProgress%")
                                MilestoneStatCard("Feeding", "$feedingProgress%")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Leap Info Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Previous",
                    tint = WhiteText,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            if (currentLeap > 1) milestoneViewModel.changeLeap(currentLeap - 1)
                        }
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Leap $currentLeap: ${milestoneViewModel.getLeapTitle(currentLeap)}",
                    color = WhiteText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Next",
                    tint = WhiteText,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            if (currentLeap < 10) milestoneViewModel.changeLeap(currentLeap + 1)
                        }
                )
            }

            Text(
                text = milestoneViewModel.getLeapDescription(currentLeap),
                color = LightGrayText,
                fontSize = 14.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Category Tabs
            val categories = listOf("Motor", "Sensory", "Communication", "Feeding")
            val categoryCodes = listOf("M", "S", "C", "F")
            ScrollableTabRow(
                selectedTabIndex = categoryCodes.indexOf(currentCategory),
                containerColor = Color.Transparent,
                contentColor = WarmOrange,
                edgePadding = 0.dp
            ) {
                categories.forEachIndexed { index, category ->
                    Tab(
                        selected = categoryCodes[index] == currentCategory,
                        onClick = {
                            milestoneViewModel.changeCategory(categoryCodes[index])
                        },
                        text = {
                            Text(
                                text = category,
                                color = if (categoryCodes[index] == currentCategory) WarmOrange else LightGrayText
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Questions Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp, max = 400.dp),
                colors = CardDefaults.cardColors(containerColor = LightDarkBackground),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    if (currentQuestions.isNotEmpty()) {
                        Text(
                            text = "${currentCategory} Questions (${currentQuestionIndex + 1}/${currentQuestions.size})",
                            color = WhiteText,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        val currentQuestion = currentQuestions[currentQuestionIndex]
                        val response = milestoneViewModel.getResponse(currentLeap, currentCategory, currentQuestion)

                        QuestionCard(currentQuestion, response ?: false) { answer ->
                            milestoneViewModel.answerQuestion(answer)
                        }
                    } else {
                        Text(
                            text = "No questions available for this category",
                            color = LightGrayText,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun QuestionCard(question: String, answer: Boolean, onAnswerSelected: (Boolean) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(darkBackground)
            .padding(16.dp)
    ) {
        Text(text = question, color = WhiteText, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OptionButton("Yes", selected = answer) { onAnswerSelected(true) }
            OptionButton("No", selected = !answer) { onAnswerSelected(false) }
        }
    }
}

@Composable
fun OptionButton(label: String, selected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = if (selected) WarmOrange else darkBackground),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(
            modifier = Modifier.padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = label, color = WhiteText, fontWeight = FontWeight.Bold)
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
        border = BorderStroke(2.dp, WarmOrange)
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = label,
                color = WhiteText,
                fontSize = 12.sp
            )
            Text(
                text = percent,
                color = WhiteText,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )
        }
    }
}