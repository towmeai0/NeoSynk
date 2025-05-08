package com.ayudevices.neosynkparent.viewmodel

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.firebase.database.*
import org.webrtc.*
import java.util.concurrent.Executors

class WebRTCManager(
    private val context: Context,
    private val firebaseRef: DatabaseReference,
    private val userId: String,
    private val isCaller: Boolean = false
) {
    private val executor = Executors.newSingleThreadExecutor()
    private val TAG = "WebRTCManager"

    // WebRTC components
    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var peerConnection: PeerConnection? = null
    internal var eglBase: EglBase? = null
    internal var videoTrack: VideoTrack? = null

    // ICE servers configuration
    private val iceServers = listOf(
        PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer()
    )

    // PeerConnection observer
    private val peerConnectionObserver = object : PeerConnection.Observer {
        override fun onIceCandidate(candidate: IceCandidate) {
            Log.d(TAG, "onIceCandidate: ${candidate.sdpMid}")
            sendIceCandidate(candidate)
        }

        override fun onIceConnectionChange(state: PeerConnection.IceConnectionState?) {
            Log.d(TAG, "onIceConnectionChange: $state")
        }

        override fun onConnectionChange(newState: PeerConnection.PeerConnectionState) {
            Log.d(TAG, "onConnectionChange: $newState")
        }

        override fun onAddStream(stream: MediaStream) {
            Log.d(TAG, "onAddStream: ${stream.id}")
            videoTrack = stream.videoTracks.firstOrNull()
        }

        // Other required overrides
        override fun onIceCandidatesRemoved(candidates: Array<IceCandidate>) {}
        override fun onDataChannel(channel: DataChannel) {}
        override fun onIceConnectionReceivingChange(receiving: Boolean) {}
        override fun onIceGatheringChange(state: PeerConnection.IceGatheringState?) {}
        override fun onAddTrack(receiver: RtpReceiver?, streams: Array<MediaStream>?) {}
        override fun onRemoveStream(stream: MediaStream?) {}
        override fun onRenegotiationNeeded() {}
        override fun onSignalingChange(state: PeerConnection.SignalingState?) {}
    }

    init {
        initializePeerConnectionFactory()
    }

    private fun initializePeerConnectionFactory() {
        executor.execute {
            try {
                PeerConnectionFactory.initialize(
                    PeerConnectionFactory.InitializationOptions.builder(context)
                        .setEnableInternalTracer(true)
                        .createInitializationOptions()
                )

                eglBase = EglBase.create()

                val options = PeerConnectionFactory.Options()
                peerConnectionFactory = PeerConnectionFactory.builder()
                    .setOptions(options)
                    .setVideoDecoderFactory(DefaultVideoDecoderFactory(eglBase!!.eglBaseContext))
                    .setVideoEncoderFactory(DefaultVideoEncoderFactory(
                        eglBase!!.eglBaseContext,
                        true, true
                    ))
                    .createPeerConnectionFactory()

                Log.d(TAG, "PeerConnectionFactory initialized")
            } catch (e: Exception) {
                Log.e(TAG, "initializePeerConnectionFactory error", e)
            }
        }
    }

    fun initializePeerConnection(renderer: SurfaceViewRenderer) {
        executor.execute {
            try {
                val rtcConfig = PeerConnection.RTCConfiguration(iceServers).apply {
                    sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
                    continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
                }

                peerConnection = peerConnectionFactory?.createPeerConnection(
                    rtcConfig,
                    peerConnectionObserver
                ) ?: throw IllegalStateException("PeerConnectionFactory not initialized")

                renderer.init(eglBase?.eglBaseContext, null)
                videoTrack?.addSink(renderer)

                if (isCaller) {
                    createOffer()
                } else {
                    listenForOffer()
                }
                listenForIceCandidates()

                Log.d(TAG, "PeerConnection initialized")
            } catch (e: Exception) {
                Log.e(TAG, "initializePeerConnection error", e)
            }
        }
    }

    private fun createOffer() {
        executor.execute {
            val constraints = MediaConstraints().apply {
                mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
            }

            peerConnection?.createOffer(object : SdpObserver {
                override fun onCreateSuccess(desc: SessionDescription) {
                    Log.d(TAG, "createOffer onCreateSuccess")
                    peerConnection?.setLocalDescription(object : SdpObserver {
                        override fun onSetSuccess() {
                            Log.d(TAG, "setLocalDescription onSetSuccess")
                            sendOffer(desc.description)
                        }
                        override fun onSetFailure(error: String) {
                            Log.e(TAG, "setLocalDescription onSetFailure: $error")
                        }
                        override fun onCreateSuccess(desc: SessionDescription?) {}
                        override fun onCreateFailure(error: String?) {}
                    }, desc)
                }
                override fun onCreateFailure(error: String) {
                    Log.e(TAG, "createOffer onCreateFailure: $error")
                }
                override fun onSetSuccess() {}
                override fun onSetFailure(error: String) {}
            }, constraints)
        }
    }

    private fun sendOffer(offer: String) {
        firebaseRef.child("signaling").child(userId).child("offer")
            .setValue(offer)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Offer sent successfully")
                } else {
                    Log.e(TAG, "Failed to send offer")
                }
            }
    }

    private fun listenForOffer() {
        firebaseRef.child("signaling").child(userId).child("offer")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.getValue(String::class.java)?.let { offer ->
                        handleOffer(offer)
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "listenForOffer cancelled: ${error.message}")
                }
            })
    }

    private fun handleOffer(offer: String) {
        executor.execute {
            peerConnection?.setRemoteDescription(object : SdpObserver {
                override fun onSetSuccess() {
                    Log.d(TAG, "setRemoteDescription onSetSuccess")
                    createAnswer()
                }
                override fun onSetFailure(error: String) {
                    Log.e(TAG, "setRemoteDescription onSetFailure: $error")
                }
                override fun onCreateSuccess(desc: SessionDescription?) {}
                override fun onCreateFailure(error: String?) {}
            }, SessionDescription(SessionDescription.Type.OFFER, offer))
        }
    }

    private fun createAnswer() {
        executor.execute {
            peerConnection?.createAnswer(object : SdpObserver {
                override fun onCreateSuccess(desc: SessionDescription) {
                    Log.d(TAG, "createAnswer onCreateSuccess")
                    peerConnection?.setLocalDescription(object : SdpObserver {
                        override fun onSetSuccess() {
                            Log.d(TAG, "setLocalDescription onSetSuccess")
                            sendAnswer(desc.description)
                        }
                        override fun onSetFailure(error: String) {
                            Log.e(TAG, "setLocalDescription onSetFailure: $error")
                        }
                        override fun onCreateSuccess(desc: SessionDescription?) {}
                        override fun onCreateFailure(error: String?) {}
                    }, desc)
                }
                override fun onCreateFailure(error: String) {
                    Log.e(TAG, "createAnswer onCreateFailure: $error")
                }
                override fun onSetSuccess() {}
                override fun onSetFailure(error: String) {}
            }, MediaConstraints())
        }
    }

    private fun sendAnswer(answer: String) {
        firebaseRef.child("signaling").child(userId).child("answer")
            .setValue(answer)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Answer sent successfully")
                } else {
                    Log.e(TAG, "Failed to send answer")
                }
            }
    }

    private fun sendIceCandidate(candidate: IceCandidate) {
        val candidateMap = mapOf(
            "sdpMid" to candidate.sdpMid,
            "sdpMLineIndex" to candidate.sdpMLineIndex,
            "sdp" to candidate.sdp
        )

        firebaseRef.child("signaling").child(userId).child("iceCandidates")
            .push().setValue(candidateMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "ICE candidate sent")
                } else {
                    Log.e(TAG, "Failed to send ICE candidate")
                }
            }
    }

    fun listenForIceCandidates() {
        firebaseRef.child("signaling").child(userId).child("iceCandidates")
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, prevKey: String?) {
                    try {
                        val candidate = snapshot.getValue(Map::class.java)?.let {
                            IceCandidate(
                                it["sdpMid"] as String,
                                (it["sdpMLineIndex"] as Long).toInt(),
                                it["sdp"] as String
                            )
                        }
                        candidate?.let {
                            executor.execute {
                                peerConnection?.addIceCandidate(it)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "onChildAdded error", e)
                    }
                }
                override fun onChildChanged(snapshot: DataSnapshot, prevKey: String?) {}
                override fun onChildRemoved(snapshot: DataSnapshot) {}
                override fun onChildMoved(snapshot: DataSnapshot, prevKey: String?) {}
                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "listenForIceCandidates cancelled: ${error.message}")
                }
            })
    }

    fun setupRemoteRenderer(remoteRenderer: SurfaceViewRenderer) {
        Handler(Looper.getMainLooper()).post {
            remoteRenderer.init(eglBase?.eglBaseContext, null)
            remoteRenderer.setMirror(false)
            remoteRenderer.setEnableHardwareScaler(true)
            Log.d(TAG, "Remote renderer initialized on main thread")
        }
    }

    private var isFactoryDisposed = false

    fun cleanup() {
        // Avoid duplicate cleanup
        if (isFactoryDisposed) return

        try {
            peerConnection?.close()
            peerConnection = null

            peerConnectionFactory?.dispose()
            isFactoryDisposed = true

            // Also clean up any other references
            peerConnectionFactory = null

            Log.d(TAG, "WebRTC resources cleaned up successfully.")

        } catch (e: Exception) {
            Log.e(TAG, "cleanup error", e)
        }
    }

}