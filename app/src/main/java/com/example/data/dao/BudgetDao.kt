package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.MonthlyBudget
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Query("SELECT * FROM monthly_budgets WHERE monthKey = :monthKey LIMIT 1")
    fun getBudgetForMonth(monthKey: String): Flow<MonthlyBudget?>

    @Query("SELECT * FROM monthly_budgets WHERE monthKey = :monthKey LIMIT 1")
    suspend fun getBudgetForMonthSync(monthKey: String): MonthlyBudget?

    @Query("SELECT * FROM monthly_budgets ORDER BY monthKey DESC")
    fun getAllBudgets(): Flow<List<MonthlyBudget>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateBudget(budget: MonthlyBudget)
}
