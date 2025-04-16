package com.ayudevices.neosynkparent.data.repository

import android.util.Log
import com.ayudevices.neosynkparent.data.database.chatdatabase.ChatDao
import com.ayudevices.neosynkparent.data.database.chatdatabase.ChatEntity
import com.ayudevices.neosynkparent.data.network.ChatApiService
import com.ayudevices.neosynkparent.data.network.ChatRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class ChatRepository @Inject constructor(
    private val chatDao: ChatDao,
    private val apiService: ChatApiService
) {
    suspend fun sendMessage(message: String) {
        val userMsg = ChatEntity(message = message, sender = "user")
        chatDao.insertMessage(userMsg)

        try {
            val response = apiService.sendMessage(ChatRequest(message))
            val replyMsg = ChatEntity(message = response.reply, sender = "bot")
            chatDao.insertMessage(replyMsg)
        } catch (e: Exception) {
            val fallbackMsg = ChatEntity(message = "Failed to get reply", sender = "bot")
            chatDao.insertMessage(fallbackMsg)
        }
    }


    fun getAllMessages(): Flow<List<ChatEntity>> = chatDao.getAllMessages()
}
