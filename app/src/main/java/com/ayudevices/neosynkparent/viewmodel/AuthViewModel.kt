package com.ayudevices.neosynkparent.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.ayudevices.neosynkparent.data.repository.AuthRepository
import com.ayudevices.neosynkparent.di.NetworkModule.TokenSenderEntryPoint
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    @ApplicationContext context: Context
) : ViewModel() {
    var authState by mutableStateOf<AuthState>(AuthState.Idle)
        private set

    val entryPoint = EntryPointAccessors.fromApplication(
        context,
        TokenSenderEntryPoint::class.java
    )

    fun signUp(email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        authState = AuthState.Authenticating
        authRepository.signUp(email, password) { success, error ->
            if (success) {
                authState = AuthState.Authenticated
                val userId = authRepository.getCurrentUserId()
                if (!userId.isNullOrEmpty()) {
                    sendParentInfoToServer(userId, email)
                } else {
                    Log.e("Login", "UserId is null after login.")
                }
                onSuccess()
            } else {
                authState = AuthState.Error
                onError(error ?: "Sign-up failed")
            }
        }
    }
    private fun sendParentInfoToServer(uid: String, email: String) {
        val apiService = entryPoint.apiService()
        val request = com.ayudevices.neosynkparent.data.model.ParentInfoRequest(
            parent_id = uid,
            email = email
        )
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            try {
                val response = apiService.sendParentInfo(request)
                if (response.isSuccessful) {
                    Log.d("Retrofit", "Parent info sent successfully")
                    FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                        Log.d("Login", "FCM token: $token")
                        entryPoint.tokenSender().sendFcmTokenToServer(token)
                    }
                } else {
                    Log.e("Retrofit", "Failed to send parent info: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("Retrofit", "Error sending parent info: ${e.localizedMessage}")
            }
        }
    }



    fun signIn(email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        authState = AuthState.Authenticating
        authRepository.signIn(email, password) { success, error ->
            if (success) {
                authState = AuthState.Authenticated
                onSuccess()
            } else {
                authState = AuthState.Error
                onError(error ?: "Login failed")
            }
        }
    }

    fun signOut() {
        authRepository.signOut()
        authState = AuthState.Idle
    }

    fun getCurrentUser(): FirebaseUser? = authRepository.getCurrentUser()
}

sealed class AuthState {
    object Idle : AuthState()
    object Authenticating : AuthState()
    object Authenticated : AuthState()
    object Error : AuthState()
}