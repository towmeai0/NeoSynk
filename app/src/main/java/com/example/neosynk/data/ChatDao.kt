package com.example.neosynk.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {

    @Query("SELECT * FROM chat_table ORDER BY id ASC")
    fun getAllMessages(): Flow<List<ChatEntity>>

    @Insert
    suspend fun insert(message: ChatEntity)

    @Query("DELETE FROM chat_table")
    suspend fun clearAll()
}
