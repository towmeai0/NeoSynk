package com.ayudevices.neosynkparent.data.database.chatdatabase

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val sender: String
)