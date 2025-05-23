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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
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
        Log.d("ChatRepository", "User ID: $userId")

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
                handlePendingIntentResponse(message, pendingIntent, userId)
                return
            }

            // No pending intent, send message to API
            Log.d("ChatRepository", "No pending intent found, sending to API")
            sendToApiAndHandleResponse(userId, message)

        } catch (e: Exception) {
            Log.e("ChatRepository", "Error sending message", e)
            // Even this error message should come from API ideally, but keeping minimal error handling
            chatDao.insertMessage(
                ChatEntity(
                    message = "Something went wrong. Try again later.",
                    sender = "bot",
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    private suspend fun sendToApiAndHandleResponse(userId: String, message: String) {
        val response = apiService.sendMessage(ChatRequest(userId, message))
        val botMessage = response.response.responseText ?: "Failed to get a valid response"
        val intent = response.response.intent
        Log.d("ChatRepository", "Intent received: $intent")
        Log.d("ChatRepository", "Message: $message")

        // Add the bot's response from API
        val options = getOptionsForIntent(intent)
        chatDao.insertMessage(ChatEntity(message = botMessage, sender = "bot", options = options))

        // Handle automatic vital requests (no user interaction needed)
        val isVitalHandled = handleAutomaticVitalRequests(intent, userId)
        Log.d("ChatRepository", "Vital request handled automatically: $isVitalHandled")

        if (isVitalHandled) {
            return // Intent was handled automatically
        }

        // Handle automatic device requests (call requestDevice immediately)
        handleAutomaticDeviceRequests(intent, userId)

        // Set pending intent if needed for user interaction
        setPendingIntentIfNeeded(intent, userId)

        // Add a small delay to ensure database write completes
        kotlinx.coroutines.delay(50)

        // Debug log to check pending intent
        val currentPendingIntent = pendingIntentDao.getPendingIntent()
        Log.d("ChatRepository", "Current pending intent after setting: ${currentPendingIntent?.vitalType}, awaiting: ${currentPendingIntent?.isAwaitingResponse}")
    }

    private suspend fun handleAutomaticVitalRequests(intent: String?, userId: String): Boolean {
        return when (intent) {
            "height_vital_request" -> {
                Log.d("ChatRepository", "Auto-handling height vital request")
                try {
                    tokenSender.requestVitals(
                        parentId = "parent_001",
                        childId = "child_001",
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
                        parentId = "parent_001",
                        childId = "child_001",
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
                        parentId = "parent_001",
                        childId = "child_001",
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
                        parentId = "parent_001",
                        childId = "child_001",
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

    private suspend fun handleAutomaticDeviceRequests(intent: String?, userId: String) {
        when (intent) {
            "device_connection_request" -> {
                Log.d("ChatRepository", "Auto-handling device connection request for weight device")
                try {
                    tokenSender.requestDevice(userId, "child_001", listOf("weight"))
                    Log.d("ChatRepository", "Weight device request sent successfully")
                } catch (e: Exception) {
                    Log.e("ChatRepository", "Error requesting weight device", e)
                }
            }
            "spo2_heart_rate_connection" -> {
                Log.d("ChatRepository", "Auto-handling device connection request for vitals device")
                try {
                    tokenSender.requestDevice(userId, "child_001", listOf("vitals"))
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
            // Remove options for automatic vital requests
            "height_vital_request", "weight_vital_request",
            "heart_rate_vital_request", "spo2_vital_request" ->
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
                    // Device request is already sent automatically, now wait for user response
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
                    // Device request is already sent automatically, now wait for user response
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
            // Remove pending intent setting for automatic vital requests
            // as they are handled immediately
        }
    }

    private suspend fun handlePendingIntentResponse(message: String, pendingIntent: PendingIntentEntity, userId: String) {
        Log.d("ChatRepository", "Handling pending intent: ${pendingIntent.vitalType} with message: $message")

        when (pendingIntent.vitalType) {
            "dob" -> {
                if (message.matches(Regex("^\\d{4}-\\d{2}-\\d{2}$"))) {
                    // Valid date format - send the actual date
                    pendingIntentDao.clearPendingIntent()
                    sendToApiAndHandleResponse(userId, message)
                } else {
                    // Invalid date format - still send what user typed
                    sendToApiAndHandleResponse(userId, message)
                }
            }
            "dob_saved" -> {
                // Send exactly what user typed (OK)
                pendingIntentDao.clearPendingIntent()
                sendToApiAndHandleResponse(userId, message)
            }
            "device_connection_request" -> {
                // Device request was already sent automatically when intent was received
                // Now just handle user response
                when (message.lowercase()) {
                    "yes" -> {
                        pendingIntentDao.clearPendingIntent()
                        // Send exactly what user typed (Yes)
                        sendToApiAndHandleResponse(userId, message)
                    }
                    "no", "skip" -> {
                        pendingIntentDao.clearPendingIntent()
                        // Send exactly what user typed (No/Skip)
                        sendToApiAndHandleResponse(userId, message)
                    }
                    else -> {
                        // Send exactly what user typed
                        sendToApiAndHandleResponse(userId, message)
                    }
                }
            }
            "spo2_heart_rate_connection" -> {
                // Device request was already sent automatically when intent was received
                // Now just handle user response
                when (message.lowercase()) {
                    "yes" -> {
                        pendingIntentDao.clearPendingIntent()
                        // Send exactly what user typed (Yes)
                        sendToApiAndHandleResponse(userId, message)
                    }
                    "no", "skip" -> {
                        pendingIntentDao.clearPendingIntent()
                        // Send exactly what user typed (No/Skip)
                        sendToApiAndHandleResponse(userId, message)
                    }
                    else -> {
                        // Send exactly what user typed
                        sendToApiAndHandleResponse(userId, message)
                    }
                }
            }
            else -> {
                // For any other pending intents, send exactly what user typed
                pendingIntentDao.clearPendingIntent()
                sendToApiAndHandleResponse(userId, message)
            }
        }
    }

    fun getAllMessages(): Flow<List<ChatEntity>> = chatDao.getAllMessages()
}

    /*class ChatRepository @Inject constructor(
        @ApplicationContext private val context: Context,
        private val chatDao: ChatDao,
        private val apiService: ChatApiService,
        private val tokenSender: TokenSender,
        private val pendingIntentDao: PendingIntentDao,
        private val authRepository: AuthRepository
    ) {

        suspend fun sendMessage(message: String) {
            val userId = authRepository.getCurrentUserId().toString()
            Log.d("ChatRepository", "User ID: $userId")

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
                    handlePendingIntentResponse(message, pendingIntent, userId)
                    return
                }

                // No pending intent, send message to API
                Log.d("ChatRepository", "No pending intent found, sending to API")
                sendToApiAndHandleResponse(userId, message)

            } catch (e: Exception) {
                Log.e("ChatRepository", "Error sending message", e)
                // Even this error message should come from API ideally, but keeping minimal error handling
                chatDao.insertMessage(
                    ChatEntity(
                        message = "Something went wrong. Try again later.",
                        sender = "bot",
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
        }

        private suspend fun sendToApiAndHandleResponse(userId: String, message: String) {
            val response = apiService.sendMessage(ChatRequest(userId, message))
            val botMessage = response.response.responseText ?: "Failed to get a valid response"
            val intent = response.response.intent
            Log.d("ChatRepository", "Intent: $intent")
            Log.d("ChatRepository", "Message: $message")

            // Add the bot's response from API
            val options = getOptionsForIntent(intent)
            chatDao.insertMessage(ChatEntity(message = botMessage, sender = "bot", options = options))

            // Set pending intent if needed
            setPendingIntentIfNeeded(intent, userId)

            // Add a small delay to ensure database write completes
            kotlinx.coroutines.delay(50)

            // Debug log to check pending intent
            val currentPendingIntent = pendingIntentDao.getPendingIntent()
            Log.d("ChatRepository", "Current pending intent after setting: ${currentPendingIntent?.vitalType}, awaiting: ${currentPendingIntent?.isAwaitingResponse}")
        }

        private fun getOptionsForIntent(intent: String?): List<String> {
            return when (intent) {
                "device_connection_request", "spo2_heart_rate_connection",
                "weight_vital_request", "height_vital_request",
                "heart_rate_vital_request", "spo2_vital_request" ->
                    listOf("Yes", "No", "Skip")
                "dob_request" ->
                    listOf("Select Date")
                "dob_saved" ->
                    listOf("OK")
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
                        tokenSender.requestDevice(userId, "child_001", listOf("weight"))
                        pendingIntentDao.setPendingIntent(
                            PendingIntentEntity(
                                vitalType = "weight_device",
                                isAwaitingResponse = true
                            )
                        )
                        Log.d("ChatRepository", "Successfully set weight_device pending intent")
                    } catch (e: Exception) {
                        Log.e("ChatRepository", "Error setting weight_device pending intent", e)
                    }
                }
                "spo2_heart_rate_connection" -> {
                    try {
                        tokenSender.requestDevice(userId, "child_001", listOf("vitals"))
                        pendingIntentDao.setPendingIntent(
                            PendingIntentEntity(
                                vitalType = "vitals_device",
                                isAwaitingResponse = true
                            )
                        )
                        Log.d("ChatRepository", "Successfully set vitals_device pending intent")
                    } catch (e: Exception) {
                        Log.e("ChatRepository", "Error setting vitals_device pending intent", e)
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
                "weight_vital_request" -> {
                    try {
                        pendingIntentDao.setPendingIntent(
                            PendingIntentEntity(
                                vitalType = "weight",
                                isAwaitingResponse = true
                            )
                        )
                        Log.d("ChatRepository", "Successfully set weight pending intent")
                    } catch (e: Exception) {
                        Log.e("ChatRepository", "Error setting weight pending intent", e)
                    }
                }
                "height_vital_request" -> {
                    try {
                        pendingIntentDao.setPendingIntent(
                            PendingIntentEntity(
                                vitalType = "height",
                                isAwaitingResponse = true
                            )
                        )
                        Log.d("ChatRepository", "Successfully set height pending intent")
                    } catch (e: Exception) {
                        Log.e("ChatRepository", "Error setting height pending intent", e)
                    }
                }
                "heart_rate_vital_request" -> {
                    try {
                        pendingIntentDao.setPendingIntent(
                            PendingIntentEntity(
                                vitalType = "heart_rate",
                                isAwaitingResponse = true
                            )
                        )
                        Log.d("ChatRepository", "Successfully set heart_rate pending intent")
                    } catch (e: Exception) {
                        Log.e("ChatRepository", "Error setting heart_rate pending intent", e)
                    }
                }
                "spo2_vital_request" -> {
                    try {
                        pendingIntentDao.setPendingIntent(
                            PendingIntentEntity(
                                vitalType = "spo2",
                                isAwaitingResponse = true
                            )
                        )
                        Log.d("ChatRepository", "Successfully set spo2 pending intent")
                    } catch (e: Exception) {
                        Log.e("ChatRepository", "Error setting spo2 pending intent", e)
                    }
                }
            }
        }

        private suspend fun handlePendingIntentResponse(message: String, pendingIntent: PendingIntentEntity, userId: String) {
            Log.d("ChatRepository", "Handling pending intent: ${pendingIntent.vitalType} with message: $message")
            val responseToSend: String

            when (pendingIntent.vitalType) {
                "dob" -> {
                    if (message.matches(Regex("^\\d{4}-\\d{2}-\\d{2}$"))) {
                        // Valid date format - send to server to save DOB
                        responseToSend = message // Send the actual date
                        pendingIntentDao.clearPendingIntent()
                    } else {
                        // Invalid date format
                        responseToSend = "dob_invalid_format"
                    }
                }
                "dob_saved" -> {
                    when (message.lowercase()) {
                        "ok" -> {
                            responseToSend = "dob_acknowledged"
                            pendingIntentDao.clearPendingIntent()
                        }
                        else -> {
                            responseToSend = "invalid_response"
                        }
                    }
                }
                "weight_device", "vitals_device" -> {
                    // Handle device connection responses
                    when (message.lowercase()) {
                        "yes" -> {
                            val vitalType = pendingIntent.vitalType?.replace("_device", "") ?: return

                            // Send vital data request after device connection is confirmed
                            val reqVitals = if (vitalType == "weight") {
                                listOf("weight")
                            } else {
                                listOf("heart_rate", "spo2") // for vitals_device
                            }

                            tokenSender.requestVitals(
                                parentId = "parent_001",
                                childId = "child_001",
                                reqVitals = reqVitals
                            )

                            responseToSend = "${pendingIntent.vitalType}_confirmed"
                            pendingIntentDao.clearPendingIntent()
                        }
                        "no" -> {
                            responseToSend = "${pendingIntent.vitalType}_declined"
                            pendingIntentDao.clearPendingIntent()
                        }
                        "skip" -> {
                            responseToSend = "${pendingIntent.vitalType}_skipped"
                            pendingIntentDao.clearPendingIntent()
                        }
                        else -> {
                            responseToSend = "invalid_response"
                        }
                    }
                }

                "weight", "height", "heart_rate", "spo2" -> {
                    // Handle vital requests - these need to call tokenSender.requestVitals()
                    when (message.lowercase()) {
                        "yes" -> {
                            val vitalType = pendingIntent.vitalType ?: return

                            // Send vital data request
                            tokenSender.requestVitals(
                                parentId = "parent_001",
                                childId = "child_001",
                                reqVitals = listOf(vitalType)
                            )
                            responseToSend = "${vitalType}_request_confirmed"
                            pendingIntentDao.clearPendingIntent()
                        }
                        "no" -> {
                            responseToSend = "${pendingIntent.vitalType}_declined"
                            pendingIntentDao.clearPendingIntent()
                        }
                        "skip" -> {
                            responseToSend = "${pendingIntent.vitalType}_skipped"
                            pendingIntentDao.clearPendingIntent()
                        }
                        else -> {
                            responseToSend = "invalid_response"
                        }
                    }
                }
                else -> {
                    // Handle any other cases
                    when (message.lowercase()) {
                        "yes" -> {
                            responseToSend = "${pendingIntent.vitalType}_confirmed"
                            pendingIntentDao.clearPendingIntent()
                        }
                        "no" -> {
                            responseToSend = "${pendingIntent.vitalType}_declined"
                            pendingIntentDao.clearPendingIntent()
                        }
                        "skip" -> {
                            responseToSend = "${pendingIntent.vitalType}_skipped"
                            pendingIntentDao.clearPendingIntent()
                        }
                        else -> {
                            responseToSend = "invalid_response"
                        }
                    }
                }
            }

            // Send the response code to API so it can return the proper message
            sendToApiAndHandleResponse(userId, responseToSend)
        }

        fun getAllMessages(): Flow<List<ChatEntity>> = chatDao.getAllMessages()
    }*/

/*
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
                            parentId = "parent_001",
                            childId = "child_001",
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

            // Handle vital requests
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
*/
