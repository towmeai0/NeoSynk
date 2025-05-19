package com.ayudevices.neosynkparent.viewmodel

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.firebase.database.*
import org.webrtc.*
import java.util.concurrent.Executors
import org.webrtc.PeerConnection

data class SignalingMessage(
    val type: String? = null,
    val sdp: String? = null
)

class WebRTCManager(
    private val context: Context,
    private val signalingRef: DatabaseReference
)
{
    private val executor = Executors.newSingleThreadExecutor()
    private lateinit var peerConnectionFactory: PeerConnectionFactory
    private lateinit var peerConnection: PeerConnection
    private var remoteRenderer: SurfaceViewRenderer? = null
    private var eglBase: EglBase = EglBase.create()
    private val track: VideoTrack? = null

    init {
        executor.execute {
            initializePeerConnectionFactory()
            initializePeerConnection()
            listenForOffer()
            listenForIceCandidates()
        }
    }

    private fun initializePeerConnectionFactory() {
        val initializationOptions = PeerConnectionFactory.InitializationOptions.builder(context)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(initializationOptions)

        val options = PeerConnectionFactory.Options()
        val encoderFactory = DefaultVideoEncoderFactory(eglBase.eglBaseContext, true, true)
        val decoderFactory = DefaultVideoDecoderFactory(eglBase.eglBaseContext)

        peerConnectionFactory = PeerConnectionFactory.builder()
            .setOptions(options)
            .setVideoEncoderFactory(encoderFactory)
            .setVideoDecoderFactory(decoderFactory)
            .createPeerConnectionFactory()
    }

    private fun initializePeerConnection() {
        val iceServers = listOf(
            PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer()
        )

        val rtcConfig = PeerConnection.RTCConfiguration(iceServers)
        peerConnection = peerConnectionFactory.createPeerConnection(rtcConfig, object : PeerConnection.Observer {
            override fun onIceCandidate(candidate: IceCandidate) {
                sendIceCandidate(candidate)
            }

            override fun onIceCandidatesRemoved(p0: Array<out IceCandidate?>?) {}

            override fun onAddTrack(receiver: RtpReceiver?, mediaStreams: Array<out MediaStream>?) {
                val track = receiver?.track()
                if (track is VideoTrack) {
                    // Handling video track: add it to the remote renderer (UI thread update)
                    Handler(Looper.getMainLooper()).post {
                        track.addSink(remoteRenderer)
                    }
                }
            }
            override fun onSignalingChange(newState: PeerConnection.SignalingState?) {
                Log.d("WebRTC", "Signaling state changed: $newState")
            }

            override fun onIceConnectionChange(newState: PeerConnection.IceConnectionState?) {
                Log.d("WebRTC", "ICE connection state changed: $newState")
                if (newState == PeerConnection.IceConnectionState.CONNECTED) {
                    Log.d("WebRTC", "PeerConnection established successfully")
                } else if (newState == PeerConnection.IceConnectionState.FAILED) {
                    Log.e("WebRTC", "PeerConnection failed to connect")
                }
            }

            override fun onIceGatheringChange(newState: PeerConnection.IceGatheringState?) {
                Log.d("WebRTC", "ICE gathering state changed: $newState")
            }
            override fun onIceConnectionReceivingChange(receiving: Boolean) {}
            override fun onRemoveStream(stream: MediaStream?) {}
            override fun onDataChannel(channel: DataChannel?) {}
            override fun onRenegotiationNeeded() {}
            override fun onAddStream(stream: MediaStream?) {}
            override fun onTrack(transceiver: RtpTransceiver?) {}
        })!!
    }

    // This method is now being used to set the renderer for remote video
    fun setRemoteRenderer(renderer: SurfaceViewRenderer) {
        remoteRenderer = renderer
        renderer.init(eglBase.eglBaseContext, null)

    }

    private fun listenForOffer() {
        signalingRef.child("child001").child("offer")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val offerData = snapshot.getValue(SignalingMessage::class.java)
                    if (offerData != null && offerData.sdp != null && offerData.type != null) {
                        val offer = SessionDescription(
                            SessionDescription.Type.fromCanonicalForm(offerData.type!!),
                            offerData.sdp
                        )
                        Log.d("WebRTC", "Offer received: $offer")

                        if (::peerConnection.isInitialized) {
                            peerConnection.setRemoteDescription(object : SdpObserver {
                                override fun onSetSuccess() {
                                    createAndSendAnswer()
                                }

                                override fun onSetFailure(error: String?) {
                                    Log.e("WebRTC", "Failed to set remote description: $error")
                                }

                                override fun onCreateSuccess(p0: SessionDescription?) {}
                                override fun onCreateFailure(p0: String?) {}
                            }, offer)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("WebRTC", "Offer listener cancelled: ${error.message}")
                }
            })
    }


    private fun createAndSendAnswer() {
        val mediaConstraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "false"))
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
        }

        peerConnection.createAnswer(object : SdpObserver {
            override fun onCreateSuccess(sessionDescription: SessionDescription) {
                peerConnection.setLocalDescription(object : SdpObserver {
                    override fun onSetSuccess() {
                        signalingRef.child("parent001").child("answer")
                            .setValue(sessionDescription)
                    }

                    override fun onSetFailure(error: String?) {
                        Log.e("WebRTC", "Failed to set local description: $error")
                    }

                    override fun onCreateSuccess(p0: SessionDescription?) {}
                    override fun onCreateFailure(p0: String?) {}
                }, sessionDescription)
            }

            override fun onSetSuccess() {}
            override fun onSetFailure(error: String?) {
                Log.e("WebRTC", "Failed to set local description: $error")
            }

            override fun onCreateFailure(error: String?) {
                Log.e("WebRTC", "Failed to create answer: $error")
            }
        }, mediaConstraints)
    }

    private fun sendIceCandidate(candidate: IceCandidate) {
        val candidateMap = mapOf(
            "sdpMid" to candidate.sdpMid,
            "sdpMLineIndex" to candidate.sdpMLineIndex,
            "candidate" to candidate.sdp
        )
        signalingRef.child("parent001").child("iceCandidates").push().setValue(candidateMap)
    }

    private fun listenForIceCandidates() {
        signalingRef.child("child001").child("iceCandidates")
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val candidateMap = snapshot.getValue(object : GenericTypeIndicator<Map<String, Any>>() {}) // Use GenericTypeIndicator here
                    if (candidateMap != null) {
                        val sdpMid = candidateMap["sdpMid"] as? String
                        val sdpMLineIndex = (candidateMap["sdpMLineIndex"] as? Long)?.toInt()
                        val sdp = candidateMap["candidate"] as? String

                        if (sdpMid != null && sdpMLineIndex != null && sdp != null) {
                            val candidate = IceCandidate(sdpMid, sdpMLineIndex, sdp)
                            Log.d("WebRTC", "ICE Candidate received: $candidate")
                            if (::peerConnection.isInitialized) {
                                peerConnection.addIceCandidate(candidate)
                            }
                        }
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildRemoved(snapshot: DataSnapshot) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onCancelled(error: DatabaseError) {
                    Log.e("WebRTC", "ICE Candidate listener cancelled: ${error.message}")
                }
            })
    }


    /*fun cleanup() {
        executor.execute {
            peerConnection.dispose()
            peerConnectionFactory.dispose()
            remoteRenderer?.release() // Release the remote renderer resources
        }
    }*/

    fun stopstreaming() {
        track?.removeSink (remoteRenderer)
        remoteRenderer?.release()
    }
}
