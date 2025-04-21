package com.ayudevices.neosynkparent.data.database.chatdatabase

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PendingIntentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setPendingIntent(intent: PendingIntentEntity)

    @Query("SELECT * FROM pending_intents WHERE id = 0")
    suspend fun getPendingIntent(): PendingIntentEntity?

    @Query("DELETE FROM pending_intents")
    suspend fun clearPendingIntent()
}
