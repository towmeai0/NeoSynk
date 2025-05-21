package com.ayudevices.neosynkparent.data.database.chatdatabase

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class OptionsTypeConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromOptionsList(options: List<String>): String {
        return gson.toJson(options)
    }

    @TypeConverter
    fun toOptionsList(optionsString: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return try {
            gson.fromJson(optionsString, listType) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}