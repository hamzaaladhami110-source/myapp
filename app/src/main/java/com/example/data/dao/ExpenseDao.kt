package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Expense
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses WHERE monthKey = :monthKey ORDER BY timestamp DESC")
    fun getExpensesForMonth(monthKey: String): Flow<List<Expense>>

    @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Query("SELECT SUM(amount) FROM expenses WHERE monthKey = :monthKey")
    fun getTotalSpentForMonth(monthKey: String): Flow<Double?>

    @Query("SELECT DISTINCT monthKey FROM expenses WHERE monthKey != :currentMonthKey ORDER BY monthKey DESC")
    fun getPreviousMonthKeys(currentMonthKey: String): Flow<List<String>>

    @Query("SELECT * FROM expenses WHERE monthKey IN (:monthKeys)")
    suspend fun getExpensesForMonthKeys(monthKeys: List<String>): List<Expense>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense): Long

    @Update
    suspend fun updateExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpenseById(id: Long)
}
