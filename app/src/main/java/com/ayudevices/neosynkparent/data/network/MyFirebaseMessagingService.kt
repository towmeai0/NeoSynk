package com.ayudevices.neosynkparent.data.network

import android.util.Log
import com.ayudevices.neosynkparent.di.NetworkModule.TokenSenderEntryPoint
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.EntryPointAccessors
import kotlin.String
import kotlin.text.get

class MyFirebaseMessagingService : FirebaseMessagingService() {
    val entryPoint = EntryPointAccessors.fromApplication(
        this,
        TokenSenderEntryPoint::class.java
    )
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (remoteMessage.data.isNotEmpty()) {
            Log.d("FCM", "Message data payload: ${remoteMessage.data}")
        }
        remoteMessage.notification?.let {
            // Handle notification payload
            Log.d("FCM", "Message Notification Body: ${it.body}")
        }
        val type = remoteMessage.data["type"] // Assume FCM payload contains "type" field
        if (type == "vitals_ready") {
            val responseKey = remoteMessage.data["responseKey"]
            if (!responseKey.isNullOrEmpty()) {
                entryPoint.tokenSender().fetchVitalsFromServer(responseKey)
            } else {
                Log.e("Vitals", "Missing responseKey in FCM")
            }
            Log.d("FCM", "Vitals submitted notification received")
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Refreshed token: $token")
        entryPoint.tokenSender().sendFcmTokenToServer(token)
    }
}