package com.ayudevices.neosynkparent.data.model

import com.google.gson.annotations.SerializedName

data class VitalData(
    val status: String,
    val vital: Vital
)

data class Vital(
    @SerializedName("_id") val id: String,
    @SerializedName("child_id") val childId: String,
    @SerializedName("vital_type") val vitalType: String,
    val value: Double,
    val unit: String,
    @SerializedName("recorded_at") val recordedAt: String,
    @SerializedName("recorded_by") val recordedBy: String,
    @SerializedName("origin_request_id") val originRequestId: String
)
