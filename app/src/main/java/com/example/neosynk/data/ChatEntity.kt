package com.example.neosynk.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_table")
data class ChatEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val message: String,
    val isUser: Boolean
)
