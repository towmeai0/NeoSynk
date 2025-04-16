package com.ayudevices.neosynkparent.data.model

import com.google.gson.annotations.SerializedName

data class ChatResponse(
    val response: ResponseDetail

data class ResponseDetail(
    @SerializedName("response_text")val responseText: String,
    val intent: String
)
