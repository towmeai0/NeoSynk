package com.ayudevices.neosynkparent.viewmodel


import android.content.Context
import android.util.AttributeSet
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoFrame

class CustomSurfaceViewRenderer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : SurfaceViewRenderer(context, attrs) {

    var onFirstFrameCallback: (() -> Unit)? = null
    private var hasSetFirstFrame = false

    init {
        setEnableHardwareScaler(true)
        setMirror(false)
    }

    override fun onFrame(frame: VideoFrame?) {
        if (!hasSetFirstFrame && frame != null) {
            hasSetFirstFrame = true
            onFirstFrameCallback?.invoke() // Notify ViewModel when the first frame is received
        }
        super.onFrame(frame)
    }
}
