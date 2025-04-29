package com.ayudevices.neosynkparent.data.database.chatdatabase

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "vitals")
data class VitalsEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val weight: String,
    val height: String,
    val heartRate: String,
    val spo2: String,
    val timestamp: Long = System.currentTimeMillis(),
    val childId: String // To identify which child these vitals belong to
)