package com.ayudevices.neosynkparent.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class LiveFeedViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val remoteUserId: String
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    private val _connectionStatus = MutableStateFlow("Disconnected")
    val connectionStatus: StateFlow<String> = _connectionStatus

    private val _isViewing = MutableStateFlow(false)
    val isViewing: StateFlow<Boolean> = _isViewing

    private var webRtcManager: WebRTCManager? = null

    private var statusListener: ValueEventListener? = null

    private val userId: String = auth.currentUser?.uid ?: ""

    init {
        if (userId.isNotEmpty()) {
            listenToStatus()
        }
    }

    private fun listenToStatus() {
        val statusRef = database.getReference("status").child(userId)
        statusListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val status = snapshot.getValue(String::class.java) ?: "Disconnected"
                _connectionStatus.value = status
            }

            override fun onCancelled(error: DatabaseError) {
                _connectionStatus.value = "Error: ${error.message}"
            }
        }
        statusRef.addValueEventListener(statusListener as ValueEventListener)
    }

    fun toggleViewing() {
        if (_isViewing.value) {
            stopViewing()
        } else {
            startViewing()
        }
    }

    private fun startViewing() {
        if (userId.isEmpty()) return
        _connectionStatus.value = "Connecting..."
        _isViewing.value = true

        // Set request flag in Firebase
        database.getReference("requests").child(userId).setValue(true)

        if (webRtcManager == null) {
            webRtcManager = WebRTCManager(
                context = context,
                signalingRef = database.getReference("signaling"),
                localUserId = userId,
                remoteUserId = remoteUserId
            )
        }
    }

    private fun stopViewing() {
        if (userId.isEmpty()) return
        _connectionStatus.value = "Disconnected"
        _isViewing.value = false

        // Remove request flag in Firebase
        database.getReference("requests").child(userId).setValue(false)
        webRtcManager?.cleanup()
        webRtcManager = null
    }

    fun setRemoteRenderer(renderer: CustomSurfaceViewRenderer) {
        webRtcManager?.setRemoteRenderer(renderer)
    }


    override fun onCleared() {
        super.onCleared()
        // Clean up listeners and WebRTC
        statusListener?.let {
            database.getReference("status").child(userId).removeEventListener(it)
        }
        stopViewing()
    }
}
