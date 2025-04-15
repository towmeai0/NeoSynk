package com.ayudevices.neosynkparent

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.ayudevices.neosynkparent.data.network.TokenSender
import com.ayudevices.neosynkparent.ui.NeoSynkApp
import com.ayudevices.neosynkparent.ui.theme.NeoSynkTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var tokenSender: TokenSender
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        FirebaseApp.initializeApp(this)
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                tokenSender.sendFcmTokenToServer(token)
                Log.d("FCM", "FCM Token: $token")
                // TODO: Send token to your server if needed
            } else {
                Log.e("FCM", "Fetching FCM token failed", task.exception)
            }
        }

        setContent {
            NeoSynkTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    NeoSynkApp()
                }
            }
        }
    }
}
