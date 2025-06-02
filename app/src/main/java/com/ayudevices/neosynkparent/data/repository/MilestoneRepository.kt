package com.ayudevices.neosynkparent.data.repository

import com.ayudevices.neosynkparent.data.database.chatdatabase.MilestoneDao
import com.ayudevices.neosynkparent.data.database.chatdatabase.MilestoneResponseEntity
import com.ayudevices.neosynkparent.data.model.Cognitive
import com.ayudevices.neosynkparent.data.model.Feeding
import com.ayudevices.neosynkparent.data.model.HeartRate
import com.ayudevices.neosynkparent.data.model.HeightCm
import com.ayudevices.neosynkparent.data.model.MileStoneDataResponse
import com.ayudevices.neosynkparent.data.model.MilestoneResults
import com.ayudevices.neosynkparent.data.model.Motor
import com.ayudevices.neosynkparent.data.model.Sensory
import com.ayudevices.neosynkparent.data.model.Spo2
import com.ayudevices.neosynkparent.data.model.VitalTrends
import com.ayudevices.neosynkparent.data.model.WeightKg
import com.ayudevices.neosynkparent.data.network.ApiService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class MilestoneRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun fetchMilestoneData(userId: String): Result<MileStoneDataResponse> {
        return try {
            // For now, return mock data - replace with actual API call later
            /*val mockData = getMockMilestoneData()
            Result.success(mockData)*/

             //TODO: Replace with actual API call
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
