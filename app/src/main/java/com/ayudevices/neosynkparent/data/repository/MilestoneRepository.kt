package com.ayudevices.neosynkparent.data.repository

import com.ayudevices.neosynkparent.data.database.chatdatabase.MilestoneDao
import com.ayudevices.neosynkparent.data.database.chatdatabase.MilestoneResponseEntity
import com.ayudevices.neosynkparent.data.network.ApiService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class MilestoneRepository @Inject constructor(
    private val milestoneDao: MilestoneDao,
    private val apiService: ApiService
) {
    suspend fun saveResponse(response: MilestoneResponseEntity) {
        milestoneDao.insertResponse(response)
    }

    fun getAllResponses(): Flow<List<MilestoneResponseEntity>> {
        return milestoneDao.getAllResponses()
    }

    suspend fun fetchAndSaveMilestone(milestoneId: String): Result<Unit> {
        return try {
            val response = apiService.fetchMilestone(milestoneId)
            if (response.isSuccessful) {
                response.body()?.let { vitalData ->
                    Result.success(Unit)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                Result.failure(Exception("API Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}
