package com.ayudevices.neosynkparent.data.database.chatdatabase

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_intents")
data class PendingIntentEntity(
    @PrimaryKey val id: Int = 0,
    val vitalType: String?, // e.g., "weight", "height"
    val isAwaitingResponse: Boolean
)