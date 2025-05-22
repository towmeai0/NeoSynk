package com.ayudevices.neosynkparent.data.repository

import android.content.Context
import android.util.Log
import com.ayudevices.neosynkparent.data.database.chatdatabase.ChatDao
import com.ayudevices.neosynkparent.data.database.chatdatabase.ChatEntity
import com.ayudevices.neosynkparent.data.database.chatdatabase.PendingIntentDao
import com.ayudevices.neosynkparent.data.database.chatdatabase.PendingIntentEntity
import com.ayudevices.neosynkparent.data.model.ChatRequest
import com.ayudevices.neosynkparent.data.network.ChatApiService
import com.ayudevices.neosynkparent.data.network.TokenSender
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ChatRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val chatDao: ChatDao,
    private val apiService: ChatApiService,
    private val tokenSender: TokenSender,
    private val pendingIntentDao: PendingIntentDao,
    private val authRepository: AuthRepository
) {

    suspend fun sendMessage(message: String) {
        val userId = authRepository.getCurrentUserId().toString()
        Log.d("ChatRepository", userId)
        val userMsg = ChatEntity(message = message, sender = "user")
        chatDao.insertMessage(userMsg)

        try {
            val pendingIntent = pendingIntentDao.getPendingIntent()
            if (pendingIntent?.isAwaitingResponse == true) {
                when (message.lowercase()) {
                    "yes" -> {
                        tokenSender.requestVitals(
                            parentId = userId,
                            childId = "child$userId",
                            reqVitals = listOf(pendingIntent.vitalType ?: return)
                        )
                        chatDao.insertMessage(
                            ChatEntity(
                                message = "${pendingIntent.vitalType} request sent successfully.",
                                sender = "bot"
                            )
                        )
                        pendingIntentDao.clearPendingIntent()
                    }
                    "no" -> {
                        chatDao.insertMessage(
                            ChatEntity(
                                message = "Okay, not sending any vital data.",
                                sender = "bot"
                            )
                        )
                        pendingIntentDao.clearPendingIntent()
                    }
                }
                return
            }

            val response = apiService.sendMessage(ChatRequest(userId, message))
            val botMessage = response.response.responseText ?: "Failed to get a valid response"
            val intent = response.response.intent
            Log.d("ChatRepository", "Intent:$intent")
            Log.d("ChatRepository", "Message:$message")

            chatDao.insertMessage(ChatEntity(message = botMessage, sender = "bot"))

            if (intent == "device_connection_request") {
                tokenSender.requestDevice(userId,"child_001", listOf("weight"))
                chatDao.insertMessage(
                    ChatEntity(
                        message = "Okay, sending device req.",
                        sender = "bot"
                    )
                )
            }

            if (intent == "spo2_heart_rate_connection") {
                tokenSender.requestDevice(userId,"child_001", listOf("vitals"))
                chatDao.insertMessage(
                    ChatEntity(
                        message = "Okay, sending device req.",
                        sender = "bot"
                    )
                )
            }

            when (intent) {
                "weight_vital_request", "height_vital_request", "heart_rate_vital_request", "spo2_vital_request" -> {
                    val vitalType = when (intent) {
                        "weight_vital_request" -> "weight"
                        "height_vital_request" -> "height"
                        "heart_rate_vital_request" -> "heart_rate"
                        "spo2_vital_request" -> "spo2"
                        else -> null
                    }
                    if (vitalType != null) {
                        pendingIntentDao.setPendingIntent(
                            PendingIntentEntity(
                                vitalType = vitalType,
                                isAwaitingResponse = true
                            )
                        )
                        chatDao.insertMessage(
                            ChatEntity(
                                message = "Enter Yes or No to fetch your $vitalType from the base app.",
                                sender = "bot",
                                options = listOf("Yes", "No")
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error sending message", e)
            chatDao.insertMessage(
                ChatEntity(
                    message = "Something went wrong. Try again later.",
                    sender = "bot",
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun getAllMessages(): Flow<List<ChatEntity>> = chatDao.getAllMessages()
}
