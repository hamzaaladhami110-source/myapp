package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ImportanceLevel(val labelAr: String, val weight: Int) {
    ESSENTIAL("أساسي (ضرورة قصوى)", 3),
    NEED("حاجة (هامة)", 2),
    LUXURY("كماليات (ترفيه/زائد)", 1);

    companion object {
        fun fromString(value: String): ImportanceLevel {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: ESSENTIAL
        }
    }
}

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Double,
    val category: String,
    val importance: ImportanceLevel,
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = "",
    val monthKey: String
)
