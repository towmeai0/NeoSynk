package com.example.neosynk.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.neosynk.data.ChatEntity
import java.util.UUID

class ChatViewModel : ViewModel() {

    // Dummy chat message list
    val messages = mutableStateListOf<ChatEntity>()

    init {
        // Add some initial dummy messages
        messages.addAll(
            listOf(

            )
        )
    }

    fun sendMessage(text: String) {
        // Add user message
        messages.add(ChatEntity(id = UUID.randomUUID().toString(), message = text, isUser = true))

        // Simulate bot reply after sending message (optional)
        simulateBotReply()
    }

    private fun simulateBotReply() {
        // Very basic dummy response
        messages.add(
            ChatEntity(
                id = UUID.randomUUID().toString(),
                message = "Thanks for your message! We will get back to you.",
                isUser = false
            )
        )
    }
}
