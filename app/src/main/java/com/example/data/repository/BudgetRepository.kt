package com.example.data.repository

import com.example.data.dao.BudgetDao
import com.example.data.dao.ExpenseDao
import com.example.data.model.Expense
import com.example.data.model.ImportanceLevel
import com.example.data.model.MonthlyBudget
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class ImportanceBreakdown(
    val essentialTotal: Double = 0.0,
    val needTotal: Double = 0.0,
    val luxuryTotal: Double = 0.0,
    val essentialCount: Int = 0,
    val needCount: Int = 0,
    val luxuryCount: Int = 0
)

data class SpendingEvaluation(
    val score: Int, // 0 - 100
    val ratingTitle: String,
    val message: String,
    val luxuryRatio: Float
)

data class ComparisonResult(
    val currentSpent: Double,
    val previousAverageSpent: Double,
    val percentageChange: Double,
    val isHigherThanAverage: Boolean,
    val sampleMonthsCount: Int
)

class BudgetRepository(
    private val expenseDao: ExpenseDao,
    private val budgetDao: BudgetDao
) {

    fun getCurrentMonthKey(): String {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.US)
        return sdf.format(Date())
    }

    fun getBudgetForMonth(monthKey: String): Flow<MonthlyBudget?> {
        return budgetDao.getBudgetForMonth(monthKey)
    }

    fun getExpensesForMonth(monthKey: String): Flow<List<Expense>> {
        return expenseDao.getExpensesForMonth(monthKey)
    }

    fun getTotalSpentForMonth(monthKey: String): Flow<Double> {
        return combine(expenseDao.getExpensesForMonth(monthKey)) { expensesList ->
            expensesList.firstOrNull()?.sumOf { it.amount } ?: 0.0
        }
    }

    suspend fun saveBudget(budget: MonthlyBudget) {
        budgetDao.insertOrUpdateBudget(budget)
    }

    suspend fun addExpense(expense: Expense): Long {
        return expenseDao.insertExpense(expense)
    }

    suspend fun updateExpense(expense: Expense) {
        expenseDao.updateExpense(expense)
    }

    suspend fun deleteExpense(expense: Expense) {
        expenseDao.deleteExpense(expense)
    }

    suspend fun deleteExpenseById(id: Long) {
        expenseDao.deleteExpenseById(id)
    }

    // Evaluation logic based on spending distribution and budget status
    fun evaluateSpending(expenses: List<Expense>, budgetAmount: Double): SpendingEvaluation {
        if (expenses.isEmpty()) {
            return SpendingEvaluation(
                score = 100,
                ratingTitle = "لم تبدأ الصرف بعد",
                message = "لم تقم بتسجيل أي مصاريف لهذا الشهر حتى الآن. استمر في المتابعة والحفاظ على الميزانية!",
                luxuryRatio = 0f
            )
        }

        val totalSpent = expenses.sumOf { it.amount }
        val luxurySpent = expenses.filter { it.importance == ImportanceLevel.LUXURY }.sumOf { it.amount }
        val essentialSpent = expenses.filter { it.importance == ImportanceLevel.ESSENTIAL }.sumOf { it.amount }

        val luxuryRatio = if (totalSpent > 0) (luxurySpent / totalSpent).toFloat() else 0f
        val budgetUsageRatio = if (budgetAmount > 0) (totalSpent / budgetAmount).toFloat() else 0f

        return when {
            budgetAmount > 0 && totalSpent > budgetAmount -> {
                SpendingEvaluation(
                    score = 25,
                    ratingTitle = "تجاوزت الميزانية! ⚠️",
                    message = "لقد تجاوزت الميزانية المحددة للشهر الحالي بمقدار ${String.format(Locale.US, "%.1f", totalSpent - budgetAmount)}. يُنصح بتقليل الكماليات فوراً.",
                    luxuryRatio = luxuryRatio
                )
            }
            budgetUsageRatio > 0.85f -> {
                SpendingEvaluation(
                    score = 50,
                    ratingTitle = "إنفاق مرتفع وشك على النفاذ ⚡",
                    message = "استهلكت أكثر من 85% من ميزانيتك. حاول تأجيل أي عمليات شراء غير ضرورية حتى الشهر القادم.",
                    luxuryRatio = luxuryRatio
                )
            }
            luxuryRatio > 0.40f -> {
                SpendingEvaluation(
                    score = 60,
                    ratingTitle = "نسبة الكماليات مرتفعة 🛒",
                    message = "تشكل الكماليات والترفيه أكثر من 40% من إجمالي مصروفاتك. إعادة توجيه جزء منها للادخار سيحسن وضعك المالي.",
                    luxuryRatio = luxuryRatio
                )
            }
            essentialSpent / (totalSpent.coerceAtLeast(1.0)) >= 0.60 -> {
                SpendingEvaluation(
                    score = 90,
                    ratingTitle = "إدارة ممتازة ومتوازنة ✨",
                    message = "ممتاز! معظم مصاريفك موجهة نحو الاحتياجات والأساسيات والضروريات. استمر بهذه الخطة المالية الحكيمة.",
                    luxuryRatio = luxuryRatio
                )
            }
            else -> {
                SpendingEvaluation(
                    score = 75,
                    ratingTitle = "إنفاق معتدل ومتزن 👍",
                    message = "إنفاقك في الحدود المقبولة ومتناسب مع احتياجاتك الشخصية.",
                    luxuryRatio = luxuryRatio
                )
            }
        }
    }

    // Comparison of current month expenses vs average of previous months
    fun getComparisonWithPreviousMonths(currentMonthKey: String): Flow<ComparisonResult> {
        return flow {
            val currentExpenses = expenseDao.getExpensesForMonthKeys(listOf(currentMonthKey))
            val currentSpent = currentExpenses.sumOf { it.amount }

            // Get previous months keys
            val cal = Calendar.getInstance()
            val sdf = SimpleDateFormat("yyyy-MM", Locale.US)
            val previousMonthKeys = mutableListOf<String>()

            // Go back up to 6 previous months
            for (i in 1..6) {
                cal.time = Date()
                cal.add(Calendar.MONTH, -i)
                previousMonthKeys.add(sdf.format(cal.time))
            }

            val previousExpenses = expenseDao.getExpensesForMonthKeys(previousMonthKeys)

            if (previousExpenses.isEmpty()) {
                emit(
                    ComparisonResult(
                        currentSpent = currentSpent,
                        previousAverageSpent = 0.0,
                        percentageChange = 0.0,
                        isHigherThanAverage = false,
                        sampleMonthsCount = 0
                    )
                )
                return@flow
            }

            val distinctPreviousMonths = previousExpenses.map { it.monthKey }.distinct()
            val monthsCount = distinctPreviousMonths.size.coerceAtLeast(1)
            val totalPreviousSpent = previousExpenses.sumOf { it.amount }
            val previousAverageSpent = totalPreviousSpent / monthsCount

            val diff = currentSpent - previousAverageSpent
            val percentageChange = if (previousAverageSpent > 0) {
                (diff / previousAverageSpent) * 100.0
            } else {
                0.0
            }

            emit(
                ComparisonResult(
                    currentSpent = currentSpent,
                    previousAverageSpent = previousAverageSpent,
                    percentageChange = percentageChange,
                    isHigherThanAverage = diff > 0,
                    sampleMonthsCount = monthsCount
                )
            )
        }
    }
}
