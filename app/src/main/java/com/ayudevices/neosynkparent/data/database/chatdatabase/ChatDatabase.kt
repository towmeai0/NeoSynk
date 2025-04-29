package com.ayudevices.neosynkparent.data.database.chatdatabase

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [ChatEntity::class, PendingIntentEntity::class, VitalsEntity::class], version = 3, exportSchema = false)
@TypeConverters(Converters::class)
abstract class ChatDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun pendingIntentDao(): PendingIntentDao
    abstract fun vitalsDao(): VitalsDao
}
