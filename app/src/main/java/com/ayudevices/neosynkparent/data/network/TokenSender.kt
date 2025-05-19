package com.ayudevices.neosynkparent.data.network

import android.content.Context
import android.util.Log
import androidx.hilt.navigation.compose.hiltViewModel
import com.ayudevices.neosynkparent.data.database.chatdatabase.ChatDao
import com.ayudevices.neosynkparent.data.database.chatdatabase.ChatEntity
import com.ayudevices.neosynkparent.data.model.ChatRequest
import com.ayudevices.neosynkparent.data.model.DeviceBodyRequest
import com.ayudevices.neosynkparent.data.model.FcmTokenRequest
import com.ayudevices.neosynkparent.data.model.VitalsBodyRequest
import com.ayudevices.neosynkparent.data.repository.AuthRepository
import com.ayudevices.neosynkparent.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class TokenSender @Inject constructor(
    private val fcmApiService: FcmApiService,
    private val chatDao: ChatDao,
    private val chatApiService: ChatApiService,
    @ApplicationContext private val context: Context,
    private val authRepository: AuthRepository
) {
    fun sendFcmTokenToServer(token: String) {
        val request = FcmTokenRequest(
            userId = "parent_001",
            fcmToken = token,
            appType = "parent"
        )

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

    fun requestVitals(parentId: String = "parent_001", childId: String = "child_001", reqVitals: List<String>) {
        Log.d("Vitals", "Vital type: ${reqVitals}")
        val request = VitalsBodyRequest(parentId, childId, reqVitals)
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

    fun requestDevice(parentId: String, childId: String = "child_001", reqVitals: List<String>) {
        Log.d("Device", "Device type: ${reqVitals}")
        val request = DeviceBodyRequest(parentId, childId, reqVitals)
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
                        chatDao.insertMessage(ChatEntity(message = it, sender = "bot"))
                        val chatResponse = chatApiService.sendMessage(ChatRequest(userId, message = "${vitals?.vital?.value}"))
                        chatDao.insertMessage(ChatEntity(message = chatResponse.response.responseText, sender = "bot"))
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
