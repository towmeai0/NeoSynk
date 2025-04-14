package com.example.neosynk.ui.screen.dashboard

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
import androidx.compose.material.icons.filled.ArrowBack

import com.example.neosynk.viewmodel.ChatViewModel
import com.example.neosynk.data.database.chatdatabase.ChatEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.math.PI



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiyaScreen(navController: NavController) {
    var userInput by remember { mutableStateOf(TextFieldValue("")) }
    var isListening by remember { mutableStateOf(false) }
    var showKeyboard by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Local messages list instead of ViewModel
    var messages by remember { mutableStateOf(listOf<ChatEntity>()) }

    val voiceLevel = remember { Animatable(0.5f) }

    // Simulate audio-first listening on screen launch
    LaunchedEffect(Unit) {
        isListening = true
        scope.launch {
            while (isListening) {
                val newLevel = Random.nextFloat() * 0.7f + 0.3f
                voiceLevel.animateTo(newLevel, animationSpec = tween(300))
                delay(300)
            }
        }
        delay(11000)
        isListening = false
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
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            },
            actions = {
                TextButton(onClick = { showKeyboard = !showKeyboard }) {
                    Text("Keyboard", color = Color.White)
                }
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
            items(messages.reversed()) { message ->
                Text(
                    text = "${message.sender}: ${message.message}",
                    color = Color.White,
                    modifier = Modifier
                        .padding(4.dp)
                        .background(Color.DarkGray, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                )
            }
        }

        if (isListening) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                VoiceOrb(
                    modifier = Modifier.size(100.dp),
                    voiceLevel = voiceLevel.value
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = { showKeyboard = !showKeyboard },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Keyboard")
        }


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
                        val userMsg = ChatEntity(
                            message = text,
                            sender = "You"
                        )
                        messages = messages + userMsg

                        // Simulate bot reply after delay
                        scope.launch {
                            delay(1000)
                            val replyText = if (text.contains("hi", ignoreCase = true)) {
                                "Hey there!"
                            } else {
                                "I'm still learning."
                            }

                            val botMsg = ChatEntity(
                                message = replyText,
                                sender = "Bot"
                            )
                            messages = messages + botMsg
                        }

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
