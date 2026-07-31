package com.example.data

import androidx.room.TypeConverter
import com.example.data.model.ImportanceLevel

class Converters {
    @TypeConverter
    fun fromImportanceLevel(importanceLevel: ImportanceLevel): String {
        return importanceLevel.name
    }

    @TypeConverter
    fun toImportanceLevel(value: String): ImportanceLevel {
        return ImportanceLevel.fromString(value)
    }
}
