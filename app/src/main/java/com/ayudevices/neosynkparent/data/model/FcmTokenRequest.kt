package com.ayudevices.neosynkparent.data.model

import com.google.gson.annotations.SerializedName

data class FcmTokenRequest(
    @SerializedName("user_id") val userId: String,
    @SerializedName("token") val fcmToken: String,
    @SerializedName("app_type") val appType: String
)
