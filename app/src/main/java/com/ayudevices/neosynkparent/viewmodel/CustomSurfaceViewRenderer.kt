package com.ayudevices.neosynkparent.viewmodel


import android.content.Context
import android.util.AttributeSet
import org.webrtc.SurfaceViewRenderer

class CustomSurfaceViewRenderer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : SurfaceViewRenderer(context, attrs) {
    init {
        setEnableHardwareScaler(true)
        setMirror(false)
    }
}