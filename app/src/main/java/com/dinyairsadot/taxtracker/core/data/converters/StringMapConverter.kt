package com.dinyairsadot.taxtracker.core.data.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class StringMapConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromStringMap(value: Map<String, String>?): String? {
        return if (value.isNullOrEmpty()) null else gson.toJson(value)
    }

    @TypeConverter
    fun toStringMap(value: String?): Map<String, String> {
        return if (value.isNullOrBlank()) {
            emptyMap()
        } else {
            val mapType = object : TypeToken<Map<String, String>>() {}.type
            gson.fromJson(value, mapType) ?: emptyMap()
        }
    }
}
