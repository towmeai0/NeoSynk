package com.ayudevices.neosynkparent.data.network

import com.ayudevices.neosynkparent.data.model.VitalData
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("milestone/{milestoneId}")
    suspend fun fetchMilestone(@Path("milestoneId") milestoneId: String): Response<VitalData>
}
