package com.ayudevices.neosynkparent.data.repository

import com.ayudevices.neosynkparent.data.model.MileStoneDataResponse
import com.ayudevices.neosynkparent.data.network.ApiService
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class MilestoneRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun fetchMilestoneData(userId: String): Result<MileStoneDataResponse> {
        return try {

             val response = apiService.fetchMilestone(userId)
             if (response.isSuccessful) {
                 response.body()?.let { milestoneData ->
                     Result.success(milestoneData)
                 } ?: Result.failure(Exception("Empty response body"))
             } else {
                 Result.failure(Exception("API Error: ${response.code()} - ${response.message()}"))
             }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
