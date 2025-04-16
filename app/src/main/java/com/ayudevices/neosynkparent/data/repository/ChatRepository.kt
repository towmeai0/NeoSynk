package com.ayudevices.neosynkparent.data.repository

import android.content.Context
import android.util.Log
import com.ayudevices.neosynkparent.data.database.chatdatabase.ChatDao
import com.ayudevices.neosynkparent.data.database.chatdatabase.ChatEntity
import com.ayudevices.neosynkparent.data.model.ChatRequest
import com.ayudevices.neosynkparent.data.network.ChatApiService
import com.ayudevices.neosynkparent.utils.UserIdManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject


class ChatRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val chatDao: ChatDao,
    private val apiService: ChatApiService
) {
    suspend fun sendMessage(message: String) {
        val userId = UserIdManager.getUserId(context)
        val userMsg = ChatEntity(message = message, sender = "user")
        chatDao.insertMessage(userMsg)
        try {
            val response = apiService.sendMessage(ChatRequest(userId,message))
            val botMessage = response.response ?: "Failed to get a valid response"
            val replyMsg = ChatEntity(message = botMessage, sender = "bot")
            chatDao.insertMessage(replyMsg)
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error sending message", e)
            val fallbackMsg = ChatEntity(message = "Failed to get reply", sender = "bot")
            chatDao.insertMessage(fallbackMsg)
        }
    }
    fun getAllMessages(): Flow<List<ChatEntity>> = chatDao.getAllMessages()
}