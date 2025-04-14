package com.example.neosynk.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.neosynk.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: ChatRepository
) : ViewModel() {

    val messages = repository.getAllMessages().asLiveData()

    fun onSendMessage(text: String) {
        viewModelScope.launch {
            repository.sendMessage(text)
        }
    }
}
