package com.ayudevices.neosynkparent.data.repository

import android.content.Context
import android.util.Log
import com.ayudevices.neosynkparent.data.database.chatdatabase.ChatDao
import com.ayudevices.neosynkparent.data.database.chatdatabase.ChatEntity
import com.ayudevices.neosynkparent.data.model.ChatRequest
import com.ayudevices.neosynkparent.data.network.ChatApiService
import com.ayudevices.neosynkparent.data.network.TokenSender
import com.ayudevices.neosynkparent.utils.UserIdManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ChatRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val chatDao: ChatDao,
    private val apiService: ChatApiService,
    private val tokenSender: TokenSender
) {
    suspend fun sendMessage(message: String) {
        val userId = UserIdManager.getUserId(context)
        val userMsg = ChatEntity(message = message, sender = "user")
        chatDao.insertMessage(userMsg)

        try {
            val response = apiService.sendMessage(ChatRequest(userId, message))
            val botMessage = response.response.responseText ?: "Failed to get a valid response"
            val intent = response.response.intent
            Log.d("ChatRepository", "Intent:$intent")
            val replyMsg = ChatEntity(message = botMessage, sender = "bot")
            chatDao.insertMessage(replyMsg)

            if (intent == "weight_vital_request") {
                while (true){
                    val latestMessage = chatDao.getLatestMessage().first()  // Safely get the latest ChatEntity
                    if (latestMessage?.message.equals("yes", ignoreCase = true)) {
                        tokenSender.requestVitals(
                            parentId = "parent_001",
                            childId = "child_001",
                            reqVitals = listOf("weight")
                        )
                        Log.d("ChatRepository", "Weight Vital API triggered due to user consent")
                        break
                    }
                    if (latestMessage?.message.equals("no", ignoreCase = true)) {
                        Log.d("ChatRepository", "User did not consent. No vital request made.")
                        break
                    }
                }
            }
            if (intent == "height_vital_request") {
                val latestMessage = chatDao.getLatestMessage().first()  // Safely get the latest ChatEntity
                if (latestMessage?.message.equals("yes", ignoreCase = true)) {
                    tokenSender.requestVitals(
                        parentId = "parent_001",
                        childId = "child_001",
                        reqVitals = listOf("height")
                    )
                    Log.d("ChatRepository", "Height Vital API triggered due to user consent")
                } else {
                    Log.d("ChatRepository", "User did not consent. No vital request made.")
                }
            }
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error sending message", e)
            val fallbackMsg = ChatEntity(message = "Failed to get reply", sender = "bot")
            chatDao.insertMessage(fallbackMsg)
        }
    }

    fun getAllMessages(): Flow<List<ChatEntity>> = chatDao.getAllMessages()
}
