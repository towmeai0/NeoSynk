package com.example.neosynk.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.neosynk.data.ChatDatabase
import com.example.neosynk.data.ChatEntity
import com.example.neosynk.network.RetrofitClient
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val chatDao = ChatDatabase.getDatabase(application).chatDao()
    private val _messages = mutableStateListOf<ChatEntity>()
    val messages: List<ChatEntity> = _messages

    private val api = RetrofitClient.getService()

    init {
        // Load messages from DB
        viewModelScope.launch {
            chatDao.getAllMessages().collectLatest { messages ->
                _messages.clear()
                _messages.addAll(messages)
            }
        }
    }

    fun sendMessage(message: String) {
        viewModelScope.launch {
            // Insert user message
            chatDao.insert(ChatEntity(message = message, isUser = true))

            try {
                // Get bot reply from jsonbin.io
                val response = api.getBotReply()
                val botReply = response.body()?.record?.reply ?: "No reply"

                // Insert bot reply
                chatDao.insert(ChatEntity(message = botReply, isUser = false))

            } catch (e: Exception) {
                // Insert error message
                chatDao.insert(ChatEntity(message = "Error: ${e.message}", isUser = false))
            }
        }
    }
}
