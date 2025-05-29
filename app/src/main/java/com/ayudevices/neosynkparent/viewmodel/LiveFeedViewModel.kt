package com.ayudevices.neosynkparent.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import org.webrtc.SurfaceViewRenderer

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
    private var surfaceViewRenderer: SurfaceViewRenderer? = null


    private var statusListener: ValueEventListener? = null

    private val userId: String = auth.currentUser?.uid ?: ""

    private val signalingRef = database.getReference("NeoSynk").child("signaling")

    // Adding state to track if we were viewing before pause
    var wasViewingBeforePause: Boolean = false

    // Flag to ensure we only add the lifecycle observer once
    var hasLifecycleObserver: Boolean = false

    private val _hasReceivedFrames = MutableStateFlow(false)
    val hasReceivedFrames: StateFlow<Boolean> = _hasReceivedFrames


    fun setHasReceivedFrames(value: Boolean) {
        _hasReceivedFrames.value = value
    }

    init {
        if (userId.isNotEmpty()) {
            listenToStatus()
            val signalingRoot = database.getReference("NeoSynk").child("signaling").child(userId)
            signalingRoot.child("status").setValue(false)
            //database.getReference("NeoSynk").child("status").setValue(false)
            startViewing()
        }
    }
    private fun listenToStatus() {
        //val statusRef = database.getReference("NeoSynk").child("status")
        val statusRef = database.getReference("NeoSynk").child("signaling").child(userId).child("status")
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



    internal fun startViewing() {
        Log.d("LIVE Feed", "Function called")
        if (userId.isEmpty()) return

        _connectionStatus.value = "Connecting..."
        _isViewing.value = true
        _hasReceivedFrames.value = false  // Reset before each viewing session

        val signalingRoot = database.getReference("NeoSynk").child("signaling").child(userId)
        signalingRoot.child("child").removeValue()
        signalingRoot.child("parent").removeValue()

        signalingRoot.child("status").setValue(true)

        webRtcManager?.cleanup()
        webRtcManager = WebRTCManager(
            context = context,
            signalingRef = signalingRoot
        ).apply {
            // Add this callback to update ViewModel state
            onFirstFrameReceived = {
                _hasReceivedFrames.value = true
            }

            // Restore remote renderer if needed
            surfaceViewRenderer?.let { setRemoteRenderer(it) }

            startStreaming()
        }
    }


    internal fun stopViewing() {
        Log.d("LIVE Feed", "STOP called")
        if (userId.isEmpty()) return

        _connectionStatus.value = "Disconnected"
        _isViewing.value = false

        // Call the stopStreaming method to remove listeners
        webRtcManager?.stopStreaming()
        _hasReceivedFrames.value = false // ✅ Reset frame flag


        //database.getReference("NeoSynk").child("status").setValue(false)
        val signalingRoot = database.getReference("NeoSynk").child("signaling").child(userId)
        signalingRoot.child("status").setValue(false)

        signalingRoot.child("child").removeValue()
        signalingRoot.child("parent").removeValue()

        // Clean up completely - we'll recreate when viewing starts again
        webRtcManager?.cleanup()
        webRtcManager = null

        // Also cleanup the renderer reference
        surfaceViewRenderer = null
    }



    fun setRemoteRenderer(renderer: SurfaceViewRenderer) {
        Log.d("LIVE Feed", "Setting remote renderer")
        surfaceViewRenderer = renderer
        webRtcManager?.setRemoteRenderer(renderer)
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("ON CLEARED","On Cleared Called")
        // Clean up listeners and WebRTC
        statusListener?.let {
            database.getReference("NeoSynk").child("signaling").child(userId).child("status").removeEventListener(it)
            //database.getReference("NeoSynk").child("status").removeEventListener(it)
        }
        stopViewing()
        webRtcManager?.cleanup()
        webRtcManager = null
    }

    fun onFrameReceived() {
        setHasReceivedFrames(true)
    }
}