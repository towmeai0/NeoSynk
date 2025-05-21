package com.ayudevices.neosynkparent.data.database.chatdatabase

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_intents")
data class PendingIntentEntity(
    @PrimaryKey val id: Int = 1,
    val vitalType: String? = null,
    val isAwaitingResponse: Boolean = false,
    val intentType: String? = null // Can store the original intent type if needed
)