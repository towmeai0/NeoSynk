package com.ayudevices.neosynkparent.data.database.chatdatabase

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters


@Entity(tableName = "chat_messages")
@TypeConverters(OptionsTypeConverter::class)
data class ChatEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val message: String,
    val sender: String,
    val timestamp: Long = System.currentTimeMillis(),
    val options: List<String> = emptyList(), // For Yes/No/Skip buttons
    val isAnswered: Boolean = false
)