package com.ayudevices.neosynkparent.data.database.chatdatabase

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "milestone_responses")
data class MilestoneResponseEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val leap: Int,
    val category: String,
    val question: String,
    val answer: Boolean
)
