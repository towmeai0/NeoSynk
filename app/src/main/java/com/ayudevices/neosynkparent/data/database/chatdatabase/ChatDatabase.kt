package com.ayudevices.neosynkparent.data.database.chatdatabase

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ChatEntity::class ,PendingIntentEntity::class], version = 2, exportSchema = false)
abstract class ChatDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun pendingIntentDao(): PendingIntentDao
}