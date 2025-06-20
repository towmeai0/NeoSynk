package com.ayudevices.neosynkparent.data.database.milestonedatabase

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ayudevices.neosynkparent.data.database.chatdatabase.MilestoneDao
import com.ayudevices.neosynkparent.data.database.chatdatabase.MilestoneResponseEntity

@Database(entities = [MilestoneResponseEntity::class], version = 1, exportSchema = false)
abstract class MilestoneDatabase : RoomDatabase() {
    abstract fun milestoneDao(): MilestoneDao
}
