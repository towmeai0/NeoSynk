package com.ayudevices.neosynkparent.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.ayudevices.neosynkparent.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: ChatRepository
) : ViewModel() {

    val messages = repository.getAllMessages()

    // Exposed event to notify UI for milestone_tab redirection
    private val _milestoneEvent = MutableSharedFlow<Unit>()
    val milestoneEvent: SharedFlow<Unit> = _milestoneEvent

    init {
        viewModelScope.launch {
            repository.navigationIntent.collect { intent ->
                if (intent == "milestone_tab") {
                    _milestoneEvent.emit(Unit)
                }
            }
        }
    }

    fun onSendMessage(text: String) {
        viewModelScope.launch {
            repository.sendMessage(text)
        }
    }
}
