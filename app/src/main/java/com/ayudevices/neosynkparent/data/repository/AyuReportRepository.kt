package com.ayudevices.neosynkparent.data.repository

import android.util.Log
import com.ayudevices.neosynkparent.data.model.AyuReportResponse
import com.ayudevices.neosynkparent.data.network.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AyuReportRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun getAllAyuReports(uid: String): Result<AyuReportResponse> {
        return try {
            Log.d("USERSID","USER ID $uid")
            val response = apiService.getAllAyuReports(uid)
            Log.d("USERSID","Response ID $response")

            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                Result.failure(Exception("API Error: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}