package com.ayudevices.neosynkparent.data.database.chatdatabase

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MilestoneDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResponse(response: MilestoneResponseEntity)

    @Query("SELECT * FROM milestone_responses WHERE leap = :leap AND category = :category AND question = :question")
    fun getResponse(leap: Int, category: String, question: String): Flow<MilestoneResponseEntity?>

    @Query("SELECT * FROM milestone_responses")
    fun getAllResponses(): Flow<List<MilestoneResponseEntity>>

    @Query("SELECT * FROM milestone_responses WHERE leap = :leap AND category = :category")
    fun getResponsesForCategory(leap: Int, category: String): Flow<List<MilestoneResponseEntity>>

    @Query("DELETE FROM milestone_responses")
    suspend fun clearMilestones()

    // In your MilestoneDao interface
    @Query("SELECT * FROM milestone_responses WHERE leap = :leap")
    suspend fun getResponsesByLeap(leap: Int): List<MilestoneResponseEntity>


}