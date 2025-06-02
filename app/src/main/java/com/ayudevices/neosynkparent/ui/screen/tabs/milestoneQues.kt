package com.ayudevices.neosynkparent.ui.screen.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ayudevices.neosynkparent.ui.theme.LightGrayText
import com.ayudevices.neosynkparent.ui.theme.WarmOrange
import com.ayudevices.neosynkparent.ui.theme.WhiteText
import com.ayudevices.neosynkparent.ui.theme.darkBackground
import com.ayudevices.neosynkparent.viewmodel.MilestoneViewModel

@Composable
fun MilestoneQues(
    navController: NavController,
    milestoneViewModel: MilestoneViewModel = hiltViewModel()
) {
    /*val currentLeap by milestoneViewModel.currentLeap.collectAsState()
    val currentCategory by milestoneViewModel.currentCategory.collectAsState()
    val currentQuestionIndex by milestoneViewModel.currentQuestionIndex.collectAsState()

    val currentQuestions by remember(currentLeap, currentCategory) {
        derivedStateOf {
            milestoneViewModel.getCurrentQuestions(currentLeap, currentCategory)
        }
    }

    Column(
        modifier = Modifier
            .background(darkBackground)  // background color to avoid white under status bar if needed
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Back button row at top
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = WhiteText,
                modifier = Modifier
                    .size(28.dp)
                    .clickable {
                        navController.popBackStack()  // navigate back
                    }
            )
        }

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
                    .clickable(enabled = currentLeap > 1) {
                        milestoneViewModel.changeLeap(currentLeap - 1)
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
                    .clickable(enabled = currentLeap < 10) {
                        milestoneViewModel.changeLeap(currentLeap + 1)
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
                    onClick = { milestoneViewModel.changeCategory(categoryCodes[index]) },
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
            colors = CardDefaults.cardColors(containerColor = darkBackground),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                if (currentQuestions.isNotEmpty()) {
                    Text(
                        text = "$currentCategory Questions (${currentQuestionIndex + 1}/${currentQuestions.size})",
                        color = WhiteText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    val currentQuestion = currentQuestions[currentQuestionIndex]
                    val response = milestoneViewModel.getResponse(currentLeap, currentCategory, currentQuestion)

                    QuestionCard(currentQuestion, response ?: false) { answer: Boolean ->
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
    }*/
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
