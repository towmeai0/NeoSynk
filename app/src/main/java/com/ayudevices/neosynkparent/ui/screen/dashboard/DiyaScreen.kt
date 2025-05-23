package com.ayudevices.neosynkparent.ui.screen.dashboard

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
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
    val messages by viewModel.messages.collectAsState()
    var userInput by remember { mutableStateOf(TextFieldValue("")) }
    var isRecording by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val calendar = Calendar.getInstance()

    // Date picker state
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = calendar.timeInMillis
    )

    // Text-to-Speech initialization
    val ttsInstance = remember { mutableStateOf<TextToSpeech?>(null) }

    DisposableEffect(context) {
        val tts = TextToSpeech(context) { status ->
            if (status != TextToSpeech.ERROR) {
                //tts.language = Locale.US
            }
        }
        ttsInstance.value = tts

        onDispose {
            tts.shutdown()
        }
    }

    val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }
    val recognizerIntent = remember {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
    }

    // Permission Launcher
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted && isRecording) {
                speechRecognizer.startListening(recognizerIntent)
            } else if (!granted) {
                isRecording = false
            }
        }
    )

    // Scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    // Handle TTS for bot messages
    LaunchedEffect(messages) {
        val tts = ttsInstance.value
        if (tts != null && messages.isNotEmpty()) {
            val lastMessage = messages.lastOrNull()
            if (lastMessage?.sender != "user") {
                tts.speak(lastMessage?.message, TextToSpeech.QUEUE_FLUSH, null, null)
            }
        }
    }

    DisposableEffect(Unit) {
        val listener = object : android.speech.RecognitionListener {
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                matches?.firstOrNull()?.let { spokenText ->
                    userInput = TextFieldValue(spokenText)
                }
                isRecording = false
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                matches?.firstOrNull()?.let { spokenText ->
                    userInput = TextFieldValue(spokenText)
                }
            }

            override fun onError(error: Int) {
                isRecording = false
                when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> {
                        // Handle audio recording error
                    }
                    SpeechRecognizer.ERROR_CLIENT -> {
                        // Handle client side error
                    }
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> {
                        // Handle permission error
                    }
                    SpeechRecognizer.ERROR_NETWORK -> {
                        // Handle network error
                    }
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> {
                        // Handle network timeout
                    }
                    SpeechRecognizer.ERROR_NO_MATCH -> {
                        // Handle no speech input
                    }
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> {
                        // Handle recognizer busy
                    }
                    SpeechRecognizer.ERROR_SERVER -> {
                        // Handle server error
                    }
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {
                        // Handle speech timeout
                    }
                }
            }

            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                isRecording = false
            }
            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
        speechRecognizer.setRecognitionListener(listener)
        onDispose {
            speechRecognizer.destroy()
        }
    }

    // DatePicker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePicker = false
                    datePickerState.selectedDateMillis?.let { dateMillis ->
                        val selectedDate = Calendar.getInstance().apply {
                            timeInMillis = dateMillis
                        }
                        val year = selectedDate.get(Calendar.YEAR)
                        val month = selectedDate.get(Calendar.MONTH) + 1
                        val day = selectedDate.get(Calendar.DAY_OF_MONTH)
                        val formattedDate = String.format("%04d-%02d-%02d", year, month, day)
                        viewModel.onSendMessage(formattedDate)
                    }
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
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
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(8.dp),
            reverseLayout = true,
            state = listState
        ) {
            items(messages.reversed()) { message ->
                MessageItem(
                    message = message,
                    onOptionSelected = { option ->
                        if (option == "Select Date") {
                            showDatePicker = true
                        } else {
                            viewModel.onOptionSelected(option)
                        }
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Input field with voice recording
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .background(Color.DarkGray, RoundedCornerShape(24.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = userInput,
                onValueChange = { userInput = it },
                placeholder = { Text("Type a message...", color = Color.Gray) },
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                maxLines = 4
            )

            // Voice recording button
            IconButton(
                onClick = {
                    if (!isRecording) {
                        // Start recording
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                            isRecording = true
                            userInput = TextFieldValue("") // Clear text when starting voice input
                            speechRecognizer.startListening(recognizerIntent)
                        } else {
                            launcher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    } else {
                        // Stop recording
                        isRecording = false
                        speechRecognizer.stopListening()
                    }
                }
            ) {
                Icon(
                    imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = if (isRecording) "Stop Recording" else "Start Recording",
                    tint = if (isRecording) Color.Red else Color.White
                )
            }

            // Send button
            IconButton(
                onClick = {
                    if (userInput.text.isNotBlank()) {
                        viewModel.onSendMessage(userInput.text)
                        userInput = TextFieldValue("")
                    }
                }
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = if (userInput.text.isNotBlank()) Color.White else Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun MessageItem(message: ChatEntity, onOptionSelected: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (message.sender == "user") Alignment.End else Alignment.Start
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

        // Show option buttons only if available AND the message hasn't been answered yet
        if (message.options.isNotEmpty() && message.sender != "user" && !message.isAnswered) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                message.options.forEach { option ->
                    Button(
                        onClick = { onOptionSelected(option) },
                        modifier = Modifier.wrapContentWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2196F3)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(option)
                    }
                }
            }
        }
    }
}


/*
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
                        speechRecognizer.stopListening()
                    } else {
                        if (userInput.text.isEmpty()) {
                            speechRecognizer.startListening(recognizerIntent)
                        }
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
                    onValueChange = {
                        userInput = it

                        // Stop Speech Recognizer if user is typing
                        if (it.text.isNotEmpty()) {
                            speechRecognizer.stopListening()
                        } else {
                            // Resume Speech Recognizer if text is cleared and keyboard is hidden
                            if (!showKeyboard) {
                                speechRecognizer.startListening(recognizerIntent)
                            }
                        }
                    },
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

                        // Restart listening after sending a message if keyboard is hidden
                        if (!showKeyboard) {
                            speechRecognizer.startListening(recognizerIntent)
                        }
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
*/
