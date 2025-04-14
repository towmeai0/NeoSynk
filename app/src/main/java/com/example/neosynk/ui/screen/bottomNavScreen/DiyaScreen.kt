package com.example.neosynk.ui.screen.bottomNavScreen

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.input.TextFieldValue
import kotlin.random.Random
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.neosynk.viewmodel.ChatViewModel
import com.example.neosynk.data.ChatEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.math.PI



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiyaScreen(navController: NavController, viewModel: ChatViewModel = viewModel()) {
    var userInput by remember { mutableStateOf(TextFieldValue("")) }
    var isListening by remember { mutableStateOf(false) }
    var showKeyboard by remember { mutableStateOf(false) }
    val messages = viewModel.messages

    val voiceLevel = remember { Animatable(0.5f) }
    val scope = rememberCoroutineScope()

    // Simulate audio-first listening on screen launch
    LaunchedEffect(Unit) {
        isListening = true
        // Animate fake voice levels for demo
        scope.launch {
            while (isListening) {
                val newLevel = Random.nextFloat() * 0.7f + 0.3f  // gives a value between 0.3f and 1.0f
                voiceLevel.animateTo(newLevel, animationSpec = tween(300))
                delay(300)
            }
        }
        delay(11000) // simulate 5 seconds of listening
        isListening = false
        viewModel.sendMessage("This is a voice input!") // dummy voice message
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        TopAppBar(
            title = {
                Text("Diya", color = Color.White, fontSize = 30.sp)
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(8.dp),
            reverseLayout = true
        ) {
            items(messages.reversed(), key = { it.id }) { message ->
                ChatBubble(message)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Show futuristic voice orb animation centered
        if (isListening) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                VoiceOrb(
                    modifier = Modifier
                        .size(100.dp),
                    voiceLevel = voiceLevel.value
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        // Button to show/hide keyboard input field
        Button(
            onClick = { showKeyboard = !showKeyboard },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Keyboard")
        }

        // Input Field + Send Button (visible only when keyboard is shown)
        if (showKeyboard) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.DarkGray, RoundedCornerShape(24.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = userInput,
                    onValueChange = {
                        userInput = it
                        if (it.text.isNotEmpty()) {
                            isListening = true
                        }
                    },
                    placeholder = { Text("Type a message...") },
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White
                    ),
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    singleLine = false
                )

                IconButton(onClick = {
                    val text = userInput.text.trim()
                    if (text.isNotEmpty()) {
                        viewModel.sendMessage(text)
                        userInput = TextFieldValue("")
                        isListening = false
                    }
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatEntity) {
    val bgColor = if (message.isUser) Color(0xFF4CAF50) else Color(0xFF333333)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .background(bgColor, RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Text(text = message.message, color = Color.White)
        }
    }
}

@Composable
fun VoiceOrb(
    modifier: Modifier = Modifier,
    voiceLevel: Float = 0.5f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave_shift")
    val waveShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing)
        ),
        label = "wave_shift"
    )

    Canvas(modifier = modifier) {
        val center = size.center
        val radius = size.minDimension / 2

        // Outer glowing orb - Increased size
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.Cyan.copy(alpha = 0.5f), Color.Transparent),
                center = center,
                radius = radius * 3.0f  // Increased radius multiplier
            ),
            radius = radius * 3.0f,  // Increased radius multiplier
            center = center
        )

        // Inner solid orb
        drawCircle(
            color = Color(0xFF1E1E2F),
            radius = radius * 1.5f,  // Increased radius for the inner orb
            center = center
        )

        val path = Path()
        val waveHeight = radius * 0.4f * voiceLevel
        val pointCount = 100

        for (i in 0..pointCount) {
            val x = i / pointCount.toFloat() * size.width
            val angle = (i / pointCount.toFloat()) * 4 * PI + waveShift
            val y = center.y + (sin(angle) * waveHeight).toFloat()

            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        drawPath(
            path = path,
            brush = Brush.horizontalGradient(
                colors = listOf(Color.Cyan, Color.Magenta, Color.Blue)
            ),
            style = Stroke(width = 4f)
        )
    }
}
