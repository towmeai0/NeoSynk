package com.ayudevices.neosynkparent.ui.screen.dashboard

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Language
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ayudevices.neosynkparent.data.database.chatdatabase.ChatEntity
import com.ayudevices.neosynkparent.viewmodel.ChatViewModel
import com.google.mlkit.common.model.DownloadConditions

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

import com.google.mlkit.nl.translate.*
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import com.ayudevices.neosynkparent.utils.MLKitTranslationManager
import com.ayudevices.neosynkparent.utils.Language

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiyaScreen(navController: NavController, viewModel: ChatViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val messages by viewModel.messages.collectAsState()
    var userInput by remember { mutableStateOf(TextFieldValue("")) }
    var isRecording by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var currentlyPlayingMessageId by remember { mutableStateOf<Int?>(null) }
    var showLoader by remember { mutableStateOf(true) }
    var showLanguageDropdown by remember { mutableStateOf(false) }

    // Translation Manager
    val translationManager = remember { MLKitTranslationManager() }

    // Language state - Updated with ML Kit supported languages
    val availableLanguages = remember {
        listOf(
            Language("English", "en", Locale.ENGLISH),
            Language("Hindi", "hi", Locale("hi", "IN")),
            Language("Kannada", "kn", Locale("kn", "IN")),
            Language("Tamil", "ta", Locale("ta", "IN")),
            Language("Telugu", "te", Locale("te", "IN")),
            Language("Malayalam", "ml", Locale("ml", "IN")),
            Language("Urdu", "ur", Locale("ur", "PK"))
        ).filter { translationManager.isLanguageSupported(it.code) }
    }

    // SharedPreferences for language persistence
    val sharedPreferences = remember {
        context.getSharedPreferences("diya_preferences", Context.MODE_PRIVATE)
    }

    // Load saved language on first composition
    var selectedLanguage by remember {
        mutableStateOf(
            // Try to load saved language, fallback to English
            getSavedLanguage(sharedPreferences, availableLanguages)
        )
    }

    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val calendar = Calendar.getInstance()

    // Date picker state
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = calendar.timeInMillis
    )

    // Text-to-Speech initialization - Fixed
    val ttsInstance = remember { mutableStateOf<TextToSpeech?>(null) }
    var isTtsInitialized by remember { mutableStateOf(false) }

    DisposableEffect(context) {
        var tts: TextToSpeech? = null

        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsInstance.value = tts
                isTtsInitialized = true
                // Set initial language
                val result = tts?.setLanguage(selectedLanguage.locale)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.w("TTS", "Language ${selectedLanguage.name} not supported, falling back to default")
                    tts?.setLanguage(Locale.ENGLISH)
                }
            } else {
                Log.e("TTS", "TextToSpeech initialization failed")
            }
        }

        onDispose {
            tts?.shutdown()
            ttsInstance.value = null
            isTtsInitialized = false
            translationManager.closeTranslators()
        }
    }

    // Update TTS language when selected language changes - Fixed
    LaunchedEffect(selectedLanguage, isTtsInitialized) {
        if (isTtsInitialized && ttsInstance.value != null) {
            val result = ttsInstance.value?.setLanguage(selectedLanguage.locale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.w("TTS", "Language ${selectedLanguage.name} not supported for TTS")
                // Optionally show a toast to user
                Toast.makeText(context, "TTS not available for ${selectedLanguage.name}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Predownload translation models for better performance
    LaunchedEffect(selectedLanguage) {
        if (selectedLanguage.code != "en") {
            translationManager.predownloadLanguageModel(selectedLanguage.code)
        }
    }

    val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }
    val recognizerIntent = remember {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, selectedLanguage.locale.toString())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
    }

    // Update speech recognizer language when selected language changes
    LaunchedEffect(selectedLanguage) {
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, selectedLanguage.locale.toString())
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
                val errorMessage = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                    SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                    SpeechRecognizer.ERROR_NETWORK -> "Network error"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                    SpeechRecognizer.ERROR_NO_MATCH -> "No speech input detected"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech recognizer busy"
                    SpeechRecognizer.ERROR_SERVER -> "Server error"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout"
                    else -> "Unknown error"
                }
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
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

    // Function to translate text to English before sending to server - Updated with ML Kit
    suspend fun translateToEnglish(text: String, fromLanguage: Language): String {
        return translationManager.translateToEnglish(text, fromLanguage)
    }

    // Function to translate English text to selected language for display - Updated with ML Kit
    suspend fun translateFromEnglish(text: String, toLanguage: Language): String {
        return translationManager.translateFromEnglish(text, toLanguage)
    }

    // Function to save language to SharedPreferences
    fun saveLanguagePreference(language: Language) {
        with(sharedPreferences.edit()) {
            putString("selected_language_code", language.code)
            putString("selected_language_name", language.name)
            apply()
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
            title = {
                Column {
                    Text("Diya", color = Color.White, fontSize = 30.sp)
                    Text(
                        "Language: ${selectedLanguage.name}",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            },
            actions = {
                Box {
                    IconButton(onClick = { showLanguageDropdown = true }) {
                        Icon(
                            Icons.Default.Language,
                            contentDescription = "Change Language",
                            tint = Color.White
                        )
                    }

                    DropdownMenu(
                        expanded = showLanguageDropdown,
                        onDismissRequest = { showLanguageDropdown = false },
                        modifier = Modifier.background(Color.DarkGray)
                    ) {
                        availableLanguages.forEach { language ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = language.name,
                                        color = if (language == selectedLanguage) Color(0xFF4CAF50) else Color.White,
                                        fontWeight = if (language == selectedLanguage) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    selectedLanguage = language
                                    saveLanguagePreference(language) // Save to SharedPreferences
                                    showLanguageDropdown = false
                                    Toast.makeText(context, "Language changed to ${language.name}", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.background(
                                    if (language == selectedLanguage) Color(0xFF2E2E2E) else Color.Transparent
                                )
                            )
                        }
                    }
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
                // Create translated message for display
                val translatedMessage by produceState(
                    initialValue = message,
                    key1 = message,
                    key2 = selectedLanguage
                ) {
                    if (message.sender != "user" && selectedLanguage.code != "en") {
                        // Translate bot messages to selected language
                        val translated = translateFromEnglish(message.message, selectedLanguage)
                        value = message.copy(message = translated)
                    } else {
                        value = message
                    }
                }

                MessageItem(
                    message = translatedMessage,
                    originalMessage = message, // Keep original for TTS fallback
                    tts = ttsInstance.value,
                    isTtsInitialized = isTtsInitialized,
                    selectedLanguage = selectedLanguage,
                    isPlaying = currentlyPlayingMessageId == message.id,
                    onPlayClick = { messageId ->
                        if (currentlyPlayingMessageId == messageId) {
                            ttsInstance.value?.stop()
                            currentlyPlayingMessageId = null
                        } else {
                            currentlyPlayingMessageId = messageId
                        }
                    },
                    onPlaybackComplete = {
                        currentlyPlayingMessageId = null
                    },
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

        // Three dots progress loader
        if (showLoader) {
            ThreeDotsLoader(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
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
                placeholder = { Text("Type a message in ${selectedLanguage.name}...", color = Color.Gray) },
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

            // Send button - Modified to handle translation
            IconButton(
                onClick = {
                    if (userInput.text.isNotBlank()) {
                        scope.launch {
                            val messageToSend = if (selectedLanguage.code != "en") {
                                // Translate to English before sending to server
                                translateToEnglish(userInput.text, selectedLanguage)
                            } else {
                                userInput.text
                            }

                            viewModel.onSendMessage(messageToSend)
                            userInput = TextFieldValue("")
                            showLoader = true

                            // Hide loader after some time
                            delay(3000)
                            showLoader = false
                        }
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

// Helper function to load saved language
private fun getSavedLanguage(
    sharedPreferences: SharedPreferences,
    availableLanguages: List<Language>
): Language {
    val savedLanguageCode = sharedPreferences.getString("selected_language_code", "en")
    val savedLanguageName = sharedPreferences.getString("selected_language_name", "English")

    // Try to find the saved language in available languages
    return availableLanguages.find { it.code == savedLanguageCode }
        ?: availableLanguages.find { it.name == savedLanguageName }
        ?: availableLanguages.firstOrNull() // Fallback to first available language
        ?: Language("English", "en", Locale.ENGLISH) // Ultimate fallback
}

@Composable
fun MessageItem(
    message: ChatEntity,
    originalMessage: ChatEntity,
    tts: TextToSpeech?,
    isTtsInitialized: Boolean,
    selectedLanguage: Language,
    isPlaying: Boolean,
    onPlayClick: (Int) -> Unit,
    onPlaybackComplete: () -> Unit,
    onOptionSelected: (String) -> Unit
) {
    // Track TTS completion - Fixed
    LaunchedEffect(isPlaying) {
        if (isPlaying && tts != null && isTtsInitialized) {
            // Set utterance progress listener
            tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    Log.d("TTS", "Started speaking: $utteranceId")
                }
                override fun onDone(utteranceId: String?) {
                    Log.d("TTS", "Finished speaking: $utteranceId")
                    onPlaybackComplete()
                }
                override fun onError(utteranceId: String?) {
                    Log.e("TTS", "Error speaking: $utteranceId")
                    onPlaybackComplete()
                }
            })

            // Create bundle with utterance ID
            val params = Bundle().apply {
                putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, message.id.toString())
            }

            // Use translated message for TTS (the message parameter already contains translated text)
            val result = tts.speak(
                message.message,
                TextToSpeech.QUEUE_FLUSH,
                params,
                message.id.toString()
            )

            if (result == TextToSpeech.ERROR) {
                Log.e("TTS", "Error starting TTS")
                onPlaybackComplete()
            }
        } else if (isPlaying && !isTtsInitialized) {
            // TTS not ready, complete immediately
            onPlaybackComplete()
        }
    }

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

        // Voice playback section (WhatsApp style) - Show for ALL messages
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .background(
                    color = if (message.sender == "user") Color(0xFF3E8E41) else Color(0xFF424242),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .widthIn(max = 280.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Play/Pause button
            IconButton(
                onClick = {
                    if (isTtsInitialized) {
                        onPlayClick(message.id)
                    } else {
                        // Show message that TTS is not ready
                        Log.w("TTS", "TTS not initialized yet")
                    }
                },
                modifier = Modifier.size(32.dp),
                enabled = isTtsInitialized
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = if (isTtsInitialized) Color.White else Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Waveform visualization (simplified bars)
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Generate some random heights for waveform bars
                val barHeights = remember { List(20) { (8..24).random().dp } }

                barHeights.forEach { height ->
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(height)
                            .background(
                                color = if (isPlaying) Color(0xFF4CAF50) else Color.Gray,
                                shape = RoundedCornerShape(1.5.dp)
                            )
                    )
                }
            }

            // Duration text (placeholder)
            Text(
                text = "0:${(message.message.length / 10).coerceAtLeast(1).coerceAtMost(59)}",
                color = Color.Gray,
                fontSize = 12.sp
            )
        }

        // Show option buttons only if available, message is from bot AND not answered
        if (originalMessage.options.isNotEmpty() && originalMessage.sender != "user" && !originalMessage.isAnswered) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                originalMessage.options.forEach { option ->
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

@Composable
fun ThreeDotsLoader(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(3) { index ->
                val animatedScale by infiniteTransition.animateFloat(
                    initialValue = 0.5f,
                    targetValue = 1.2f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = 600,
                            delayMillis = index * 200
                        ),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "dot_$index"
                )

                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .scale(animatedScale)
                        .background(
                            color = Color.White,
                            shape = CircleShape
                        )
                )
            }
        }
    }
}
