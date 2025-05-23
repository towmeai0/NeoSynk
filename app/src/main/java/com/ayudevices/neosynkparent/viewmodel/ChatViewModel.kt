package com.ayudevices.neosynkparent.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.ayudevices.neosynkparent.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: ChatRepository
) : ViewModel() {

    val messages = repository.getAllMessages()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = emptyList()
        )

    fun onSendMessage(text: String) {
        viewModelScope.launch {
            repository.sendMessage(text)
        }
    }

    // Helper function to handle option button clicks
    fun onOptionSelected(option: String) {
        viewModelScope.launch {
            // Simply send the selected option as a message
            // The repository will handle marking messages as answered
            onSendMessage(option)
        }
    }
}
