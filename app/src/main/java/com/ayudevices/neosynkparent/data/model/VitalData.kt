package com.ayudevices.neosynkparent.data.model

import com.google.gson.annotations.SerializedName

data class VitalData(
    @SerializedName("vital_type") val vitalType: String,
    @SerializedName("value") val value: Float,
    @SerializedName("unit") val unit: String,
    @SerializedName("recorded_at") val timestamp: String
)
