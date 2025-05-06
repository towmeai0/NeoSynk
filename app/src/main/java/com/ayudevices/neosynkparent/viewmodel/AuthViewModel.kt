package com.ayudevices.neosynkparent.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.ayudevices.neosynkparent.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    var authState by mutableStateOf<AuthState>(AuthState.Idle)
        private set

    fun signUp(email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        authState = AuthState.Authenticating
        authRepository.signUp(email, password) { success, error ->
            if (success) {
                authState = AuthState.Authenticated
                onSuccess()
            } else {
                authState = AuthState.Error
                onError(error ?: "Sign-up failed")
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