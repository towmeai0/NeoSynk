package com.example.neosynk.network

import com.example.neosynk.model.BotResponse
import retrofit2.Response
import retrofit2.http.GET

interface JsonBinService {
    @GET("b/YOUR_BIN_ID")
    suspend fun getBotReply(): Response<JsonBinWrapper>
}

data class JsonBinWrapper(
    val record: BotResponse
)
