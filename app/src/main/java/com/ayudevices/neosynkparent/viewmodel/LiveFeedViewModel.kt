package com.ayudevices.neosynkparent.viewmodel

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import org.webrtc.VideoTrack

@HiltViewModel
class LiveFeedViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel()
{

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    private val _connectionStatus = MutableStateFlow("Disconnected")
    val connectionStatus: StateFlow<String> = _connectionStatus

    private val _isViewing = MutableStateFlow(false)
    val isViewing: StateFlow<Boolean> = _isViewing

    private var webRtcManager: WebRTCManager? = null

    private var statusListener: ValueEventListener? = null

    private val userId: String = auth.currentUser?.uid ?: ""

    private val localUserId: String = "parent001" // Parent app's user ID

    // Child's remoteUserId
    private val remoteUserId: String = "child001" // Child app's user ID

    private val signalingRef = database.getReference("NeoSynk").child("signaling")

    init {
        if (userId.isNotEmpty()) {
            listenToStatus()
        }
    }

    private fun listenToStatus() {
        val statusRef = database.getReference("NeoSynk").child("status")
        statusListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val status = snapshot.getValue(Boolean::class.java) ?: false
                _connectionStatus.value = if (status) "Connected" else "Disconnected"
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

        // Set the status to "true" (connected) in the signaling node under userId
        database.getReference("NeoSynk").child("status").setValue(true)

        // Set offer data for signaling if needed
        if (webRtcManager == null) {
            webRtcManager = WebRTCManager(
                context = context,
                database.getReference("NeoSynk").child("signaling")
            )
        }
    }


    private fun stopViewing() {
        if (userId.isEmpty()) return
        _connectionStatus.value = "Disconnected"
        _isViewing.value = false

        // Update the status to "false" (disconnected) in the signaling node under userId
        database.getReference("NeoSynk").child("status").setValue(false)

        // Cleanup the WebRTC manager
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
            database.getReference("status").removeEventListener(it)
        }
        stopViewing()
    }
}
