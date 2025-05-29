package com.ayudevices.neosynkparent.data.network

import com.ayudevices.neosynkparent.data.model.ParentInfoRequest
import com.ayudevices.neosynkparent.data.model.VitalData
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @GET("milestone/{Id}")
    suspend fun fetchMilestone(@Path("milestoneId") milestoneId: String): Response<VitalData>

    @POST("register-parent")
    suspend fun sendParentInfo(@Body request: ParentInfoRequest): Response<Void>
}

