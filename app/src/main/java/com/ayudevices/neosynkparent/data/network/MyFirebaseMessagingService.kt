package com.ayudevices.neosynkparent.data.network

import android.util.Log
import com.ayudevices.neosynkparent.di.NetworkModule.TokenSenderEntryPoint
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlin.String
import kotlin.text.get

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            TokenSenderEntryPoint::class.java
        )
        if (remoteMessage.data.isNotEmpty()) {
            Log.d("FCM", "Message data payload: ${remoteMessage.data}")
        }
        remoteMessage.notification?.let {
            // Handle notification payload
            Log.d("FCM", "Message Notification Body: ${it.body}")
        }
        val type = remoteMessage.data["type"] // Assume FCM payload contains "type" field
        if (type == "vital_completed") {
            val vitalId = remoteMessage.data["vital_id"]
            if (!vitalId.isNullOrEmpty()) {
                entryPoint.tokenSender().fetchVitalsFromServer(vitalId)
            } else {
                Log.e("Vitals", "Missing responseKey in FCM")
            }
            Log.d("FCM", "Vitals submitted notification received")
        }
    }

    override fun onNewToken(token: String) {
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            TokenSenderEntryPoint::class.java
        )
        super.onNewToken(token)
        Log.d("FCM", "Refreshed token: $token")
        entryPoint.tokenSender().sendFcmTokenToServer(token)
    }
}