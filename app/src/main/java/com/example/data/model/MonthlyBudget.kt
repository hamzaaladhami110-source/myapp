package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "monthly_budgets")
data class MonthlyBudget(
    @PrimaryKey val monthKey: String, // Format: YYYY-MM e.g. "2026-07"
    val amount: Double,
    val currency: String = "ر.س",
    val alertThresholdPercent: Int = 80,
    val reminderEnabled: Boolean = true
)
