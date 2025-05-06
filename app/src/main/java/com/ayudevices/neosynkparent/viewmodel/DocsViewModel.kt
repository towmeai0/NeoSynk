package com.ayudevices.neosynkparent.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ayudevices.neosynkparent.data.database.chatdatabase.ChatDao
import com.ayudevices.neosynkparent.data.database.chatdatabase.ChatEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DocsViewModel @Inject constructor(
    private val chatDao: ChatDao
) : ViewModel() {

    // File name state
    private val _selectedFileName = MutableStateFlow<String?>(null)
    val selectedFileName: StateFlow<String?> = _selectedFileName

    fun onFileSelected(fileName: String) {
        _selectedFileName.value = fileName
    }

    // Vitals
    var weight by mutableStateOf("Loading...")
        private set

    var height by mutableStateOf("Loading...")
        private set

    var heartRate by mutableStateOf("Loading...")
        private set

    var spo2 by mutableStateOf("Loading...")
        private set

    // Latest intent state
    var latestIntent by mutableStateOf("No intent received yet")
        private set

    fun updateLatestIntent(intent: String) {
        latestIntent = intent
    }

    init {
        fetchVitalsFromServer()
        observeChatMessages()
    }

    private fun fetchVitalsFromServer() {
        // Initial values
        weight = "-- kg"
        height = "-- cm"
        heartRate = "-- bpm"
        spo2 = "--%"
        latestIntent = "weight_vital_request"
    }

    private fun observeChatMessages() {
        viewModelScope.launch {
            chatDao.getAllMessages().collectLatest { messages ->
                messages.forEach { message ->
                    val msg = message.message.lowercase() // To ensure case-insensitive matching
                    when {
                        msg.contains("weight") -> {
                            weight = extractValue(msg, "weight")
                            latestIntent = "weight_vital_request"
                        }

                        msg.contains("height") -> {
                            height = extractValue(msg, "height")
                            latestIntent = "height_vital_request"
                        }

                        msg.contains("heart rate") || msg.contains("heart_rate") -> {
                            heartRate = extractValue(msg, "heart_rate")
                            latestIntent = "heart_rate_vital_request"
                        }

                        msg.contains("spo2") -> {
                            spo2 = extractValue(msg, "spo2")
                            latestIntent = "spo2_vital_request"
                        }
                    }
                }
            }
        }
    }

    private fun extractValue(message: String, type: String): String {
        val cleanType = type.replace("_", " ")
        val pattern = Regex("""\b$cleanType\b\s*[:=]?\s*([0-9.]+)""", RegexOption.IGNORE_CASE)
        val match = pattern.find(message)
        return match?.groups?.get(1)?.value ?: "Fetching...."
    }
}