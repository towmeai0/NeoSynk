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

    private val signalingRef = database.getReference("NeoSynk").child("signaling")

    init {
        if (userId.isNotEmpty()) {
            listenToStatus()
            database.getReference("NeoSynk").child("status").setValue(false)
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

    /* fun toggleViewing() {
         if (_isViewing.value) {
             stopViewing(context)
         } else {
             startViewing(context)
         }
     }*/

    internal fun startViewing() {
        if (userId.isEmpty()) return

        _connectionStatus.value = "Connecting..."
        _isViewing.value = true

        val signalingRoot = database.getReference("NeoSynk").child("signaling")
        signalingRoot.child("child001").removeValue()
        signalingRoot.child("parent001").removeValue()

        database.getReference("NeoSynk").child("status").setValue(true)

        if (webRtcManager == null) {
            webRtcManager = WebRTCManager(
                context = context,
                signalingRef = signalingRoot
            )
        }
        webRtcManager?.startStreaming()
    }

    internal fun stopViewing() {
        if (userId.isEmpty()) return

        _connectionStatus.value = "Disconnected"
        _isViewing.value = false

        database.getReference("NeoSynk").child("status").setValue(false)
        webRtcManager?.stopStreaming()

        // Clean up signaling data
        val signalingRoot = database.getReference("NeoSynk").child("signaling")
        signalingRoot.child("child001").removeValue()
        signalingRoot.child("parent001").removeValue()
    }


    fun setRemoteRenderer(renderer: CustomSurfaceViewRenderer) {
        webRtcManager?.setRemoteRenderer(renderer)
    }

    override fun onCleared() {
        super.onCleared()
        // Clean up listeners and WebRTC
        statusListener?.let {
            database.getReference("NeoSynk").child("status").removeEventListener(it)
        }
        stopViewing()
        webRtcManager?.cleanup()
        webRtcManager = null
    }
}