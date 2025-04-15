package com.ayudevices.neosynkparent.fcm

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.util.Log

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Handle FCM messages here
        if (remoteMessage.data.isNotEmpty()) {
            // Handle data payload
            Log.d("FCM", "Message data payload: ${remoteMessage.data}")
        }

        remoteMessage.notification?.let {
            // Handle notification payload
            Log.d("FCM", "Message Notification Body: ${it.body}")
        }
    }

}
