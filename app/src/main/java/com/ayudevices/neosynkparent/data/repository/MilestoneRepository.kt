package com.ayudevices.neosynkparent.data.repository

import com.ayudevices.neosynkparent.data.database.chatdatabase.MilestoneDao
import com.ayudevices.neosynkparent.data.database.chatdatabase.MilestoneResponseEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class MilestoneRepository @Inject constructor(
    private val milestoneDao: MilestoneDao
) {
    // Save response to the database
    suspend fun saveResponse(response: MilestoneResponseEntity) {
        milestoneDao.insertResponse(response)
    }

    // Get single response
    fun getResponse(leap: Int, category: String, question: String): Flow<MilestoneResponseEntity?> {
        return milestoneDao.getResponse(leap, category, question)
    }

    // Get all responses
    fun getAllResponses(): Flow<List<MilestoneResponseEntity>> {
        return milestoneDao.getAllResponses()
    }
    // In MilestoneRepository
    suspend fun getResponsesByLeap(leap: Int): List<MilestoneResponseEntity> {
        return milestoneDao.getResponsesByLeap(leap)
    }


    // Get responses for specific leap and category
    fun getResponsesForCategory(leap: Int, category: String): Flow<List<MilestoneResponseEntity>> {
        return milestoneDao.getResponsesForCategory(leap, category)
    }
}
