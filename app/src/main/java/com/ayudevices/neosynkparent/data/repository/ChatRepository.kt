package com.ayudevices.neosynkparent.data.repository

import android.util.Log
import com.ayudevices.neosynkparent.data.database.chatdatabase.ChatDao
import com.ayudevices.neosynkparent.data.database.chatdatabase.ChatEntity
import com.ayudevices.neosynkparent.data.model.ChatRequest
import com.ayudevices.neosynkparent.data.network.ChatApiService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject


class ChatRepository @Inject constructor(
    private val chatDao: ChatDao,
    private val apiService: ChatApiService
) {
    suspend fun sendMessage(userId: String = UUID.randomUUID().toString(), message: String) {
        val userMsg = ChatEntity(user_id = userId, message = message, sender = "user")
        chatDao.insertMessage(userMsg)

        try {
            val response = apiService.sendMessage(ChatRequest(userId,message))
            val replyMsg = ChatEntity(user_id = userId, message = response.response.responseText, sender = "bot")
            chatDao.insertMessage(replyMsg)
        } catch (e: Exception) {
            val fallbackMsg = ChatEntity(user_id = userId, message = "Failed to get reply", sender = "bot")
            chatDao.insertMessage(fallbackMsg)
        }
    }

    fun getAllMessages(): Flow<List<ChatEntity>> = chatDao.getAllMessages()
}



