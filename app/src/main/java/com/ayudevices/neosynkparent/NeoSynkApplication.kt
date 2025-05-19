package com.ayudevices.neosynkparent

import android.app.Application
import android.util.Log
import com.ayudevices.neosynkparent.data.network.TokenSender
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject


@HiltAndroidApp
class NeoSynkApplication: Application(){
    @Inject lateinit var tokenSender: TokenSender
    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d("FCM", "FCM Token: $token")
                tokenSender.sendFcmTokenToServer(token)
            } else {
                Log.e("FCM", "Fetching FCM token failed", task.exception)
            }
        }
    }
}