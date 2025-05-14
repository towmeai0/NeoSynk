package com.ayudevices.neosynkparent.ui.screen.dashboard

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ayudevices.neosynkparent.data.database.chatdatabase.ChatEntity
import com.ayudevices.neosynkparent.viewmodel.ChatViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiyaScreen(navController: NavController, viewModel: ChatViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val messages by viewModel.messages.collectAsState(initial = emptyList())
    var userInput by remember { mutableStateOf(TextFieldValue("")) }
    var isListening by remember { mutableStateOf(false) }
    var showKeyboard by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val voiceLevel = remember { Animatable(0.5f) }

    // Text-to-Speech initialization
    val tts = remember {
        TextToSpeech(context, null)
    }

    LaunchedEffect(Unit) {
        tts.language = Locale.US
    }

    val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }
    val recognizerIntent = remember {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
        }
    }

    // Permission Launcher
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                startListening(speechRecognizer, recognizerIntent)
            }
        }
    )

    // Start listening when screen loads
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            if (!showKeyboard) startListening(speechRecognizer, recognizerIntent)
        } else {
            launcher.launch(Manifest.permission.RECORD_AUDIO)
        }

        scope.launch {
            while (true) {
                val newLevel = Random.nextFloat() * 0.7f + 0.3f
                voiceLevel.animateTo(newLevel, animationSpec = tween(300))
                delay(300)
            }
        }
    }

    LaunchedEffect(messages, showKeyboard) {
        if (!showKeyboard) {
            messages.lastOrNull()?.let { message ->
                if (message.sender != "user") {
                    tts.speak(message.message, TextToSpeech.QUEUE_FLUSH, null, null)
                }
            }
        } else {
            tts.stop()
        }
    }

    DisposableEffect(Unit) {
        val listener = object : android.speech.RecognitionListener {
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                matches?.firstOrNull()?.let { spokenText ->
                    viewModel.onSendMessage(spokenText)
                }
                if (!showKeyboard) speechRecognizer.startListening(recognizerIntent)
            }
            override fun onError(error: Int) {
                if (!showKeyboard) speechRecognizer.startListening(recognizerIntent)
            }

            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
        speechRecognizer.setRecognitionListener(listener)
        onDispose {
            speechRecognizer.destroy()
            tts.shutdown()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        TopAppBar(
            title = { Text("Diya", color = Color.White, fontSize = 30.sp) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            },
            actions = {
                Button(onClick = {
                    showKeyboard = !showKeyboard
                    if (showKeyboard) {
                        speechRecognizer.stopListening()  // Pause voice input when keyboard opens
                    } else {
                        speechRecognizer.startListening(recognizerIntent)  // Resume voice input
                    }
                }) {
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = if (message.sender == "user") Arrangement.End else Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (message.sender == "user") Color(0xFF4CAF50) else Color.DarkGray,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp)
                            .widthIn(max = 280.dp)
                    ) {
                        Text(text = message.message, color = Color.White)
                    }
                }
            }
        }

        if (!showKeyboard) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                VoiceOrb(
                    modifier = Modifier.size(100.dp),
                    voiceLevel = voiceLevel.value
                )
            }
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
                    onValueChange = { userInput = it },
                    placeholder = { Text("Type a message...") },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White
                    )
                )
                IconButton(onClick = {
                    if (userInput.text.isNotBlank()) {
                        viewModel.onSendMessage(userInput.text)
                        userInput = TextFieldValue("")

                        // Restart listening after sending a message
                        speechRecognizer.startListening(recognizerIntent)
                    }
                }) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White)
                }
            }
        }
    }
}

fun startListening(speechRecognizer: SpeechRecognizer, intent: Intent) {
    speechRecognizer.startListening(intent)
}

@Composable
fun VoiceOrb(modifier: Modifier = Modifier, voiceLevel: Float) {
    Canvas(modifier = modifier) {
        val radius = size.minDimension / 2
        val center = Offset(size.width / 2, size.height / 2)
        drawCircle(
            color = Color(0xFF00BCD4),
            center = center,
            radius = radius * voiceLevel,
            style = Stroke(width = 4.dp.toPx())
        )
        drawCircle(
            color = Color(0xFF00BCD4).copy(alpha = 0.3f),
            center = center,
            radius = radius * (1 + 0.2f * sin(PI * voiceLevel).toFloat())
        )
    }
}
