package com.ayudevices.neosynkparent.data.database.chatdatabase

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VitalsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVitals(vitals: VitalsEntity)

    @Query("SELECT * FROM vitals WHERE childId = :childId ORDER BY timestamp DESC LIMIT 1")
    fun getLatestVitals(childId: String): Flow<VitalsEntity?>

    @Query("SELECT * FROM vitals WHERE childId = :childId ORDER BY timestamp DESC")
    fun getAllVitals(childId: String): Flow<List<VitalsEntity>>

    @Query("DELETE FROM vitals WHERE childId = :childId")
    suspend fun clearVitals(childId: String)
}