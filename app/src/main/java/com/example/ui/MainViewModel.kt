package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.Expense
import com.example.data.model.ImportanceLevel
import com.example.data.model.MonthlyBudget
import com.example.data.repository.BudgetRepository
import com.example.data.repository.ComparisonResult
import com.example.data.repository.SpendingEvaluation
import com.example.notification.NotificationHelper
import com.example.pdf.PdfReportExporter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class MainViewModel(private val repository: BudgetRepository) : ViewModel() {

    private val _currentMonthKey = MutableStateFlow(repository.getCurrentMonthKey())
    val currentMonthKey: StateFlow<String> = _currentMonthKey.asStateFlow()

    val budget: StateFlow<MonthlyBudget?> = _currentMonthKey.flatMapLatest { monthKey ->
        repository.getBudgetForMonth(monthKey)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val expenses: StateFlow<List<Expense>> = _currentMonthKey.flatMapLatest { monthKey ->
        repository.getExpensesForMonth(monthKey)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val totalSpent: StateFlow<Double> = expenses.combine(_currentMonthKey) { expList, _ ->
        expList.sumOf { it.amount }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    val spendingEvaluation: StateFlow<SpendingEvaluation> = combine(expenses, budget) { expList, budgetObj ->
        repository.evaluateSpending(expList, budgetObj?.amount ?: 0.0)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SpendingEvaluation(100, "بدون بيانات", "قم بإضافة المصاريف للبدء", 0f)
    )

    val comparisonResult: StateFlow<ComparisonResult> = _currentMonthKey.flatMapLatest { monthKey ->
        repository.getComparisonWithPreviousMonths(monthKey)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ComparisonResult(0.0, 0.0, 0.0, false, 0)
    )

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    fun clearMessage() {
        _userMessage.value = null
    }

    fun setSalaryAndBudget(amount: Double, currency: String = "ر.س", alertPercent: Int = 80) {
        viewModelScope.launch {
            val monthKey = _currentMonthKey.value
            val newBudget = MonthlyBudget(
                monthKey = monthKey,
                amount = amount,
                currency = currency,
                alertThresholdPercent = alertPercent
            )
            repository.saveBudget(newBudget)
            _userMessage.value = "تم تحديد الراتب/الميزانية الشهرية بنجاح ($amount $currency)"
        }
    }

    fun addExpense(
        title: String,
        amount: Double,
        category: String,
        importance: ImportanceLevel,
        notes: String = "",
        context: Context? = null
    ) {
        viewModelScope.launch {
            val monthKey = _currentMonthKey.value
            val expense = Expense(
                title = title,
                amount = amount,
                category = category,
                importance = importance,
                timestamp = System.currentTimeMillis(),
                notes = notes,
                monthKey = monthKey
            )
            repository.addExpense(expense)
            _userMessage.value = "تمت إضافة $title بمبلغ $amount بنجاح"

            // Check budget alert threshold
            context?.let { checkBudgetAlert(it, amount) }
        }
    }

    private fun checkBudgetAlert(context: Context, newAmount: Double) {
        val currentBudget = budget.value ?: return
        val currentSpent = totalSpent.value + newAmount
        val budgetAmount = currentBudget.amount
        if (budgetAmount <= 0) return

        val percentUsed = (currentSpent / budgetAmount) * 100

        if (currentSpent > budgetAmount) {
            NotificationHelper.showBudgetAlertNotification(
                context,
                "تنبيه تجاوز الميزانية! 🚨",
                "تجاوزت مصروفاتك ميزانية الشهر الحالي (${currentBudget.amount} ${currentBudget.currency}). بلغ الصرف الكلي ${String.format("%.1f", currentSpent)}."
            )
        } else if (percentUsed >= currentBudget.alertThresholdPercent) {
            NotificationHelper.showBudgetAlertNotification(
                context,
                "تنبيه اقتراب حد الميزانية! ⚠️",
                "وصل إنفاقك إلى ${String.format("%.0f", percentUsed)}% من الميزانية المحددة لشهر ${currentBudget.monthKey}."
            )
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
            _userMessage.value = "تم حذف المصروف بنجاح"
        }
    }

    fun exportPdfReport(context: Context): File? {
        val monthKey = _currentMonthKey.value
        val budgetObj = budget.value
        val expList = expenses.value
        val spent = totalSpent.value
        val eval = spendingEvaluation.value

        return PdfReportExporter.generateAndSharePdfReport(
            context = context,
            monthKey = monthKey,
            budget = budgetObj,
            expenses = expList,
            totalSpent = spent,
            evaluation = eval
        )
    }

    class Factory(private val repository: BudgetRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                return MainViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
