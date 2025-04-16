package com.ayudevices.neosynkparent.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.ayudevices.neosynkparent.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: ChatRepository
) : ViewModel() {

    val messages = repository.getAllMessages()

    fun onSendMessage(text: String) {
        viewModelScope.launch {
            repository.sendMessage(text)
        }
    }
}
