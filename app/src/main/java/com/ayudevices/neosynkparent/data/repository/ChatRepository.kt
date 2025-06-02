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
        val childId = "child$userId"
        Log.d("ChatRepository", "User ID: $userId, Child ID: $childId")

        // Find and mark the latest message with options as answered (if any)
        val optionsMessage = chatDao.getLatestUnansweredOptionsMessage()
        optionsMessage?.let {
            chatDao.markMessageAsAnswered(it.id)
        }

        // Insert user message
        val userMsg = ChatEntity(message = message, sender = "user")
        chatDao.insertMessage(userMsg)

        try {
            // Check for pending intent first
            val pendingIntent = pendingIntentDao.getPendingIntent()
            Log.d("ChatRepository", "Checking pending intent: ${pendingIntent?.vitalType}, awaiting: ${pendingIntent?.isAwaitingResponse}")

            if (pendingIntent?.isAwaitingResponse == true) {
                Log.d("ChatRepository", "Handling pending intent response for: ${pendingIntent.vitalType}")
                handlePendingIntentResponse(message, pendingIntent, userId, childId)
                return
            }

            // No pending intent, send message to API
            Log.d("ChatRepository", "No pending intent found, sending to API")
            sendToApiAndHandleResponse(userId, childId, message)

        } catch (e: Exception) {
            Log.e("ChatRepository", "Error sending message", e)
            // Check if this is a report generation response with JSON format
            handleReportGenerationResponse(e, message)
        }
    }

    private suspend fun handleReportGenerationResponse(exception: Exception, originalMessage: String) {
        try {
            // Check if the exception message or response contains JSON with report_generated intent
            val errorMessage = exception.message ?: ""
            Log.d("ChatRepository", "Checking for report generation in error: $errorMessage")

            // Check if this is related to report generation
            if (originalMessage.lowercase().contains("report") ||
                originalMessage.lowercase().contains("generate") ||
                errorMessage.contains("report_generated")) {

                Log.d("ChatRepository", "Detected report generation request")

                // Insert the report generation message
                chatDao.insertMessage(
                    ChatEntity(
                        message = "Report Generated, Please check Ayu Report to download.",
                        sender = "bot",
                        timestamp = System.currentTimeMillis()
                    )
                )
                return
            }
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error handling report generation response", e)
        }

        // If not a report generation case, show generic error
        chatDao.insertMessage(
            ChatEntity(
                message = "Something went wrong. Try again later.",
                sender = "bot",
                timestamp = System.currentTimeMillis()
            )
        )
    }

    private suspend fun sendToApiAndHandleResponse(userId: String, childId: String, message: String) {
        Log.d("PARENT AND CHILD IDS", "PARENT AND CHILD IDS $userId $childId")

        try {
            val response = apiService.sendMessage(ChatRequest(userId, message))
            val botMessage = response.response.responseText ?: "Failed to get a valid response"
            val intent = response.response.intent
            Log.d("ChatRepository", "Intent received: $intent")
            Log.d("ChatRepository", "Message: $message")

            // Add the bot's response from API
            val options = getOptionsForIntent(intent)
            chatDao.insertMessage(ChatEntity(message = botMessage, sender = "bot", options = options))

            // Handle report_generated intent
            if (intent == "report_generated") {
                Log.d("ChatRepository", "Handling report_generated intent")
                chatDao.insertMessage(
                    ChatEntity(
                        message = "Report Generated. Please check Ayu Report to download.",
                        sender = "bot",
                        timestamp = System.currentTimeMillis()
                    )
                )
                return // No further processing needed for this intent
            }

            // Handle automatic vital requests (no user interaction needed)
            val isVitalHandled = handleAutomaticVitalRequests(intent, userId, childId)
            Log.d("ChatRepository", "Vital request handled automatically: $isVitalHandled")

            if (isVitalHandled) {
                return // Intent was handled automatically
            }

            // Handle automatic device requests (call requestDevice immediately)
            handleAutomaticDeviceRequests(intent, userId, childId)

            // Set pending intent if needed for user interaction
            setPendingIntentIfNeeded(intent, userId)

            // Add a small delay to ensure database write completes
            kotlinx.coroutines.delay(50)

            // Debug log to check pending intent
            val currentPendingIntent = pendingIntentDao.getPendingIntent()
            Log.d("ChatRepository", "Current pending intent after setting: ${currentPendingIntent?.vitalType}, awaiting: ${currentPendingIntent?.isAwaitingResponse}")

        } catch (e: Exception) {
            Log.e("ChatRepository", "Error in sendToApiAndHandleResponse", e)

            // Check if this might be a report generation response with different JSON format
            if (message.lowercase().contains("report") || message.lowercase().contains("generate")) {
                Log.d("ChatRepository", "Attempting to handle as report generation")
                try {
                    // Try to parse the response if it's in JSON format
                    val errorMessage = e.message ?: ""
                    if (errorMessage.contains("report_generated") ||
                        message.lowercase().contains("generate report")) {

                        chatDao.insertMessage(
                            ChatEntity(
                                message = "Report Generated, Please check Ayu Report to download.",
                                sender = "bot",
                                timestamp = System.currentTimeMillis()
                            )
                        )
                        return
                    }
                } catch (parseException: Exception) {
                    Log.e("ChatRepository", "Error parsing report response", parseException)
                }
            }

            // Re-throw the exception to be handled by the outer catch block
            throw e
        }
    }

    private suspend fun handleAutomaticVitalRequests(intent: String?, userId: String, childId: String): Boolean {
        return when (intent) {
            "height_vital_request" -> {
                Log.d("ChatRepository", "Auto-handling height vital request")
                try {
                    tokenSender.requestVitals(
                        parentId = userId,
                        childId = childId,
                        reqVitals = listOf("height")
                    )
                    Log.d("ChatRepository", "Height vital request sent successfully")
                } catch (e: Exception) {
                    Log.e("ChatRepository", "Error sending height vital request", e)
                }
                true
            }
            "weight_vital_request" -> {
                Log.d("ChatRepository", "Auto-handling weight vital request")
                try {
                    tokenSender.requestVitals(
                        parentId = userId,
                        childId = childId,
                        reqVitals = listOf("weight")
                    )
                    Log.d("ChatRepository", "Weight vital request sent successfully")
                } catch (e: Exception) {
                    Log.e("ChatRepository", "Error sending weight vital request", e)
                }
                true
            }
            "heart_rate_vital_request" -> {
                Log.d("ChatRepository", "Auto-handling heart rate vital request")
                try {
                    tokenSender.requestVitals(
                        parentId = userId,
                        childId = childId,
                        reqVitals = listOf("heart_rate")
                    )
                    Log.d("ChatRepository", "Heart rate vital request sent successfully")
                } catch (e: Exception) {
                    Log.e("ChatRepository", "Error sending heart rate vital request", e)
                }
                true
            }
            "spo2_vital_request" -> {
                Log.d("ChatRepository", "Auto-handling SpO2 vital request")
                try {
                    tokenSender.requestVitals(
                        parentId = userId,
                        childId = childId,
                        reqVitals = listOf("spo2")
                    )
                    Log.d("ChatRepository", "SpO2 vital request sent successfully")
                } catch (e: Exception) {
                    Log.e("ChatRepository", "Error sending SpO2 vital request", e)
                }
                true
            }
            else -> false
        }
    }

    private suspend fun handleAutomaticDeviceRequests(intent: String?, userId: String, childId: String) {
        when (intent) {
            "device_connection_request" -> {
                Log.d("ChatRepository", "Auto-handling device connection request for weight device")
                try {
                    tokenSender.requestDevice(userId, childId, listOf("weight"))
                    Log.d("ChatRepository", "Weight device request sent successfully")
                } catch (e: Exception) {
                    Log.e("ChatRepository", "Error requesting weight device", e)
                }
            }
            "spo2_heart_rate_connection" -> {
                Log.d("ChatRepository", "Auto-handling device connection request for vitals device")
                try {
                    tokenSender.requestDevice(userId, childId, listOf("vitals"))
                    Log.d("ChatRepository", "Vitals device request sent successfully")
                } catch (e: Exception) {
                    Log.e("ChatRepository", "Error requesting vitals device", e)
                }
            }
        }
    }

    private fun getOptionsForIntent(intent: String?): List<String> {
        return when (intent) {
            "device_connection_request", "spo2_heart_rate_connection","vitals_updated","milestone_query" ->
                listOf("Yes", "No", "Skip")
            "dob_request" ->
                listOf("Select Date")
            "dob_saved" ->
                listOf("OK")
            "height_vital_request", "weight_vital_request",
            "heart_rate_vital_request", "spo2_vital_request", "report_generated" ->
                emptyList()
            else -> emptyList()
        }
    }

    private suspend fun setPendingIntentIfNeeded(intent: String?, userId: String) {
        Log.d("ChatRepository", "Setting pending intent for: $intent")
        when (intent) {
            "dob_saved" -> {
                try {
                    pendingIntentDao.setPendingIntent(
                        PendingIntentEntity(
                            vitalType = "dob_saved",
                            isAwaitingResponse = true
                        )
                    )
                    Log.d("ChatRepository", "Successfully set dob_saved pending intent")
                } catch (e: Exception) {
                    Log.e("ChatRepository", "Error setting dob_saved pending intent", e)
                }
            }
            "device_connection_request" -> {
                try {
                    pendingIntentDao.setPendingIntent(
                        PendingIntentEntity(
                            vitalType = "device_connection_request",
                            isAwaitingResponse = true
                        )
                    )
                    Log.d("ChatRepository", "Successfully set device_connection_request pending intent")
                } catch (e: Exception) {
                    Log.e("ChatRepository", "Error setting device_connection_request pending intent", e)
                }
            }
            "spo2_heart_rate_connection" -> {
                try {
                    pendingIntentDao.setPendingIntent(
                        PendingIntentEntity(
                            vitalType = "spo2_heart_rate_connection",
                            isAwaitingResponse = true
                        )
                    )
                    Log.d("ChatRepository", "Successfully set spo2_heart_rate_connection pending intent")
                } catch (e: Exception) {
                    Log.e("ChatRepository", "Error setting spo2_heart_rate_connection pending intent", e)
                }
            }
            "dob_request" -> {
                try {
                    pendingIntentDao.setPendingIntent(
                        PendingIntentEntity(
                            vitalType = "dob",
                            isAwaitingResponse = true
                        )
                    )
                    Log.d("ChatRepository", "Successfully set dob pending intent")
                } catch (e: Exception) {
                    Log.e("ChatRepository", "Error setting dob pending intent", e)
                }
            }
        }
    }

    private suspend fun handlePendingIntentResponse(message: String, pendingIntent: PendingIntentEntity, userId: String, childId: String) {
        Log.d("ChatRepository", "Handling pending intent: ${pendingIntent.vitalType} with message: $message")

        when (pendingIntent.vitalType) {
            "dob" -> {
                if (message.matches(Regex("^\\d{4}-\\d{2}-\\d{2}$"))) {
                    pendingIntentDao.clearPendingIntent()
                    sendToApiAndHandleResponse(userId, childId, message)
                } else {
                    sendToApiAndHandleResponse(userId, childId, message)
                }
            }
            "dob_saved" -> {
                pendingIntentDao.clearPendingIntent()
                sendToApiAndHandleResponse(userId, childId, message)
            }
            "device_connection_request" -> {
                when (message.lowercase()) {
                    "yes" -> {
                        pendingIntentDao.clearPendingIntent()
                        sendToApiAndHandleResponse(userId, childId, message)
                    }
                    "no", "skip" -> {
                        pendingIntentDao.clearPendingIntent()
                        sendToApiAndHandleResponse(userId, childId, message)
                    }
                    else -> {
                        sendToApiAndHandleResponse(userId, childId, message)
                    }
                }
            }
            "spo2_heart_rate_connection" -> {
                when (message.lowercase()) {
                    "yes" -> {
                        pendingIntentDao.clearPendingIntent()
                        sendToApiAndHandleResponse(userId, childId, message)
                    }
                    "no", "skip" -> {
                        pendingIntentDao.clearPendingIntent()
                        sendToApiAndHandleResponse(userId, childId, message)
                    }
                    else -> {
                        sendToApiAndHandleResponse(userId, childId, message)
                    }
                }
            }
            else -> {
                pendingIntentDao.clearPendingIntent()
                sendToApiAndHandleResponse(userId, childId, message)
            }
        }
    }

    fun getAllMessages(): Flow<List<ChatEntity>> = chatDao.getAllMessages()
}