package com.ayudevices.neosynkparent.data.network

import com.ayudevices.neosynkparent.data.model.ChatRequest
import com.ayudevices.neosynkparent.data.model.ChatResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface ChatApiService {
    @POST("chat")
    suspend fun sendMessage(@Body request: ChatRequest): ChatResponse
}
