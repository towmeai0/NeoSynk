package com.ayudevices.neosynkparent.data.model

import com.google.gson.annotations.SerializedName

data class VitalsRequestResponse(
    val status: String,
    @SerializedName("request_id") val requestId: String
)