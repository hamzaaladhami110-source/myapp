package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Expense
import com.example.data.model.ImportanceLevel
import com.example.data.model.MonthlyBudget
import com.example.ui.MainViewModel
import com.example.ui.theme.EssentialColor
import com.example.ui.theme.LuxuryColor
import com.example.ui.theme.NeedColor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    budget: MonthlyBudget?,
    totalSpent: Double,
    expenses: List<Expense>,
    onNavigateToAdd: () -> Unit,
    onNavigateToAnalytics: () -> Unit
) {
    val context = LocalContext.current
    var showSalaryDialog by remember { mutableStateOf(false) }

    val budgetAmount = budget?.amount ?: 0.0
    val currency = budget?.currency ?: "ر.س"
    val remaining = budgetAmount - totalSpent
    val progress = if (budgetAmount > 0) (totalSpent / budgetAmount).toFloat().coerceIn(0f, 1f) else 0f

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        // Hero Card: Budget & Salary
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ميزانية الشهر (${budget?.monthKey ?: "الرواتب"})",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        IconButton(onClick = { showSalaryDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "تعديل الراتب والميزانية",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (budgetAmount <= 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "لم تقم بتحديد راتب/ميزانية هذا الشهر بعد 💡",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { showSalaryDialog = true },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("إدخال الراتب والميزانية الان")
                                }
                            }
                        }
                    } else {
                        // 3 Column Stats
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatBox(
                                label = "الميزانية الكلية",
                                value = "${String.format(Locale.US, "%.0f", budgetAmount)} $currency",
                                textColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            StatBox(
                                label = "المصروفات",
                                value = "${String.format(Locale.US, "%.0f", totalSpent)} $currency",
                                textColor = Color(0xFFDC2626)
                            )
                            StatBox(
                                label = "المتبقي",
                                value = "${String.format(Locale.US, "%.0f", remaining)} $currency",
                                textColor = if (remaining >= 0) Color(0xFF059669) else Color(0xFFB91C1C)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Progress bar
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "نسبة الاستهلاك: ${String.format(Locale.US, "%.0f%%", progress * 100)}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                if (totalSpent > budgetAmount) {
                                    Text(
                                        text = "⚠️ تجاوزت الميزانية!",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Red
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp)),
                                color = if (progress > 0.9f) Color.Red else MaterialTheme.colorScheme.primary,
                                trackColor = Color.White.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }

        // Action Buttons Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onNavigateToAdd,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "إضافة مصروف جديد", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        val pdfFile = viewModel.exportPdfReport(context)
                        if (pdfFile != null) {
                            com.example.pdf.PdfReportExporter.sharePdfFile(context, pdfFile)
                        }
                    },
                    modifier = Modifier.height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "تصدير PDF", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }
        }

        // Reminder Status Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "التنبيهات التلقائية مفعلة 🔔",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "سيرسل لك التطبيق إشعاراً تلقائياً كل أول شهر لتحديث الراتب والميزانية.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // Recent Expenses List Title
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "أحدث المصاريف المسجلة",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                TextButton(onClick = onNavigateToAdd) {
                    Text("عرض الكل (${expenses.size})")
                }
            }
        }

        if (expenses.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "لا توجد مصاريف مسجلة هذا الشهر حتى الآن 🛒",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        } else {
            items(expenses.take(6), key = { it.id }) { expense ->
                ExpenseItemCard(
                    expense = expense,
                    currency = currency,
                    onDelete = { viewModel.deleteExpense(expense) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }

    // Set Salary Dialog
    if (showSalaryDialog) {
        SetSalaryDialog(
            currentAmount = budgetAmount,
            currentCurrency = currency,
            onDismiss = { showSalaryDialog = false },
            onConfirm = { amount, curr, alertPct ->
                viewModel.setSalaryAndBudget(amount, curr, alertPct)
                showSalaryDialog = false
            }
        )
    }
}

@Composable
fun StatBox(label: String, value: String, textColor: Color) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textColor)
    }
}

@Composable
fun ExpenseItemCard(
    expense: Expense,
    currency: String,
    onDelete: () -> Unit
) {
    val sdf = SimpleDateFormat("dd MMMM - hh:mm a", Locale("ar"))
    val dateStr = sdf.format(Date(expense.timestamp))

    val (impColor, impLabel) = when (expense.importance) {
        ImportanceLevel.ESSENTIAL -> EssentialColor to "أساسي"
        ImportanceLevel.NEED -> NeedColor to "حاجة"
        ImportanceLevel.LUXURY -> LuxuryColor to "كماليات"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(impColor)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = expense.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${expense.category} • ",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = impLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = impColor
                        )
                    }
                    Text(
                        text = dateStr,
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${String.format(Locale.US, "%.1f", expense.amount)} $currency",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "حذف المصروف",
                        tint = Color.Red.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
fun SetSalaryDialog(
    currentAmount: Double,
    currentCurrency: String,
    onDismiss: () -> Unit,
    onConfirm: (Double, String, Int) -> Unit
) {
    var amountText by remember { mutableStateOf(if (currentAmount > 0) currentAmount.toString() else "") }
    var selectedCurrency by remember { mutableStateOf(currentCurrency) }
    var alertThresholdText by remember { mutableStateOf("80") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إدخال الراتب والميزانية الشهرية 💵") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("يرجى إدخال مبلغ الراتب أول الشهر لتحديد الميزانية الكلية للتطبيق.")

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("مبلغ الراتب / الميزانية") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = selectedCurrency,
                    onValueChange = { selectedCurrency = it },
                    label = { Text("العملة (مثال: ر.س، $، د.إ)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = alertThresholdText,
                    onValueChange = { alertThresholdText = it },
                    label = { Text("نسبة التنبيه عند الاقتراب (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    val alertPct = alertThresholdText.toIntOrNull() ?: 80
                    onConfirm(amt, selectedCurrency, alertPct)
                }
            ) {
                Text("حفظ الميزانية")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
