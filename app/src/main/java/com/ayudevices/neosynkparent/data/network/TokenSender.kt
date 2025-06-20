package com.ayudevices.neosynkparent.data.network

import android.content.Context
import android.util.Log
import com.ayudevices.neosynkparent.data.database.chatdatabase.ChatDao
import com.ayudevices.neosynkparent.data.database.chatdatabase.ChatEntity
import com.ayudevices.neosynkparent.data.model.DeviceBodyRequest
import com.ayudevices.neosynkparent.data.model.FcmTokenRequest
import com.ayudevices.neosynkparent.data.model.VitalsBodyRequest
import com.ayudevices.neosynkparent.data.repository.AuthRepository
import com.ayudevices.neosynkparent.data.repository.ChatRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider


class TokenSender @Inject constructor(
    private val fcmApiService: FcmApiService,
    private val chatDao: ChatDao,
    private val chatApiService: ChatApiService,
    @ApplicationContext private val context: Context,
    private val authRepository: AuthRepository,
    private val chatRepositoryProvider: Provider<ChatRepository> // Lazy injection

){
    fun sendFcmTokenToServer(token: String) {
        val userId = authRepository.getCurrentUserId()
        val request = FcmTokenRequest(
            userId = userId.toString(),
            fcmToken = token,
            appType = "parent"
        )

        Log.d("FCM","FCM TOKEN Fetched $token")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = fcmApiService.updateFcmToken(request)
                if (response.isSuccessful) {
                    Log.d("FCM", "Token updated: ${response.body()?.detail} ")
                } else {
                    Log.e("FCM", "Failed: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("FCM", "Error sending token", e)
            }
        }
    }

    fun requestVitals(parentId: String, childId: String, reqVitals: List<String>) {
        Log.d("Vitals", "Vital type: ${reqVitals}")
        val request = VitalsBodyRequest(parentId, childId, reqVitals)
        Log.d("Vitals", "Data payload: ${request}")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = fcmApiService.requestVitals(request)
                if (response.isSuccessful) {
                    Log.d("Vitals", "Vitals requested successfully")
                } else {
                    Log.e("Vitals", "Failed to request vitals: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("Vitals", "Error requesting vitals", e)
            }
        }
    }

    fun requestDevice(parentId: String, childId: String, reqVitals: List<String>) {
        Log.d("Device", "Device type: ${reqVitals}")
        val request = DeviceBodyRequest(parentId, childId, reqVitals)
        Log.d("Device", "Data payload: ${reqVitals}")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = fcmApiService.requestDevice(request)
                if (response.isSuccessful) {
                    Log.d("Vitals", "Device requested successfully ${response.code()} ${response.message()}")
                } else {
                    Log.e("Vitals", "Failed to request device: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("Vitals", "Error requesting device", e)
            }
        }
    }

    fun fetchVitalsFromServer(responseKey: String) {
        val chatRepository = chatRepositoryProvider.get()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userId = authRepository.getCurrentUserId().toString()
                if (userId.isEmpty()) {
                    Log.e("Vitals", "User ID is null or empty.")
                    return@launch
                }
                val fetchResponse = fcmApiService.fetchVitals(responseKey)
                if (fetchResponse.isSuccessful) {
                    val vitals = fetchResponse.body()
                    val vitalMsg = when (vitals?.vital?.vitalType) {
                        "weight" -> "Weight: ${vitals.vital.value} Kg"
                        "height" -> "Height: ${vitals.vital.value} Cm"
                        "spo2" -> "SpO2: ${vitals.vital.value} %"
                        "heart_rate" -> "Heart Rate: ${vitals.vital.value} bpm"
                        else -> null
                    }
                    vitalMsg?.let {
                        Log.d("Vitals", "${vitals?.vital?.vitalType}: ${vitals?.vital?.value} at ${vitals?.vital?.recordedAt}")

                        // Insert the vital information message
                        chatDao.insertMessage(ChatEntity(message = it, sender = "bot"))

                        // IMPORTANT: Use ChatRepository.sendMessage instead of direct API call
                        // This will trigger all the automatic intent handling
                        chatRepository.sendMessage("${vitals?.vital?.value}")
                    }
                } else {
                    Log.e("Vitals", "Fetch failed: ${fetchResponse.code()} ${fetchResponse.message()}")
                }
            } catch (e: Exception) {
                Log.e("Vitals", "Error during fetch", e)
            }
        }
    }
}
