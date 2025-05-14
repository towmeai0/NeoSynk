package com.ayudevices.neosynkparent.data.network

import com.ayudevices.neosynkparent.data.model.BaseResponse
import com.ayudevices.neosynkparent.data.model.DeviceBodyRequest
import com.ayudevices.neosynkparent.data.model.FcmTokenRequest
import com.ayudevices.neosynkparent.data.model.VitalData
import com.ayudevices.neosynkparent.data.model.VitalsBodyRequest
import com.ayudevices.neosynkparent.data.model.VitalsRequestResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface FcmApiService {
    @POST("update-fcm")
    suspend fun updateFcmToken(@Body request: FcmTokenRequest): Response<BaseResponse>

    @POST("vital-request")
    suspend fun requestVitals(@Body request: VitalsBodyRequest): Response<VitalsRequestResponse>

    @POST("device-request")
    suspend fun requestDevice(@Body request: DeviceBodyRequest): Response<VitalsRequestResponse>

    @GET("vitals/{vitalId}")
    suspend fun fetchVitals(@Path("vitalId") responseKey: String): Response<VitalData>

}

