package com.ayudevices.neosynkparent.data.model

import com.google.gson.annotations.SerializedName

data class ChatRequest(
    @SerializedName("user_id") val userId: String,
      val message: String
)
