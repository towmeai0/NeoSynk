package com.ayudevices.neosynkparent.data.network

import android.util.Log
import com.ayudevices.neosynkparent.data.model.FcmTokenRequest
import com.ayudevices.neosynkparent.data.model.VitalsBodyRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class TokenSender @Inject constructor(
    private val fcmApiService: FcmApiService
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
                    Log.d("FCM", "Token updated: ${response.body()?.detail}")
                } else {
                    Log.e("FCM", "Failed: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("FCM", "Error sending token", e)
            }
        }
    }

    fun requestVitals(parentId: String = "parent_001" , childId: String = "child_001", reqVitals: List<String> = listOf<String>("weight")) {
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

    fun fetchVitalsFromServer(responseKey: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = fcmApiService.fetchVitals(responseKey)
                if (response.isSuccessful) {
                    val vitals = response.body()
                    Log.d("Vitals", "Weight: ${vitals?.vital?.value} at ${vitals?.vital?.recordedAt}")
                } else {
                    Log.e("Vitals", "Fetch failed: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("Vitals", "Error during fetch", e)
            }
        }
    }
}
