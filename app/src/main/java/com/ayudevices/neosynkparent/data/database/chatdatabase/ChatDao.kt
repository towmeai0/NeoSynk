package com.ayudevices.neosynkparent.data.database.chatdatabase

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatEntity)

    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatEntity>>

    @Query("DELETE FROM chat_messages")
    suspend fun clearChat()

    @Query("SELECT * FROM chat_messages ORDER BY timestamp DESC LIMIT 1")
    fun getLatestMessage(): Flow<ChatEntity?>

    @Query("UPDATE chat_messages SET isAnswered = 1 WHERE id = :messageId")
    suspend fun markMessageAsAnswered(messageId: Int)

    // Find the most recent message with options that hasn't been answered
    @Query("SELECT * FROM chat_messages WHERE options != '[]' AND isAnswered = 0 ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestUnansweredOptionsMessage(): ChatEntity?
}