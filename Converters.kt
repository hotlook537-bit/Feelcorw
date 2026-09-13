package com.example.data.local

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return value?.joinToString(";;;") ?: ""
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrEmpty()) return emptyList()
        return value.split(";;;").filter { it.isNotEmpty() }
    }

    @TypeConverter
    fun fromStringMap(value: Map<String, String>?): String {
        return value?.entries?.joinToString(";;;") { "${it.key}:::${it.value}" } ?: ""
    }

    @TypeConverter
    fun toStringMap(value: String?): Map<String, String> {
        if (value.isNullOrEmpty()) return emptyMap()
        return value.split(";;;").mapNotNull { entry ->
            val parts = entry.split(":::")
            if (parts.size == 2) parts[0] to parts[1] else null
        }.toMap()
    }
}
