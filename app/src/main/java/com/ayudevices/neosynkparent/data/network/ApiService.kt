package com.ayudevices.neosynkparent.data.network

import com.ayudevices.neosynkparent.data.model.AyuReportResponse
import com.ayudevices.neosynkparent.data.model.MedicalReportResponse
import com.ayudevices.neosynkparent.data.model.MedicalReportUploadResponse
import com.ayudevices.neosynkparent.data.model.MileStoneDataResponse
import com.ayudevices.neosynkparent.data.model.ParentInfoRequest
import com.ayudevices.neosynkparent.data.model.VitalData
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiService {
    @GET("milestones/{userId}")
    suspend fun fetchMilestone(@Path("userId") userId: String): Response<MileStoneDataResponse>

    @POST("register-parent")
    suspend fun sendParentInfo(@Body request: ParentInfoRequest): Response<Void>

    @GET("request-pdf/{userId}")
    suspend fun getAllAyuReports(@Path("userId") userId: String): Response<AyuReportResponse>

    @Multipart
    @POST("upload-file")
    suspend fun uploadMedicalReport(
        @Part("parent_id") parentId: RequestBody,
        @Part file: MultipartBody.Part
    ): Response<MedicalReportUploadResponse>

    @GET("files/{parent_id}")
    suspend fun getMedicalReports(@Path("parent_id") parentId: String): Response<List<MedicalReportResponse>>
}

