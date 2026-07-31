package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

val categoriesList = listOf(
    "طعام ومؤن",
    "فواتير وسكن",
    "مواصلات وسيارة",
    "صحة وعلاج",
    "تسوق وكماليات",
    "ترفيه ورعايات",
    "تعليم وتطوير",
    "أخرى"
)

@Composable
fun AddExpenseScreen(
    viewModel: MainViewModel,
    budget: MonthlyBudget?,
    expenses: List<Expense>
) {
    val context = LocalContext.current
    var titleText by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(categoriesList[0]) }
    var selectedImportance by remember { mutableStateOf(ImportanceLevel.ESSENTIAL) }
    var notesText by remember { mutableStateOf("") }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    var selectedFilterImportance by remember { mutableStateOf<ImportanceLevel?>(null) }

    val currency = budget?.currency ?: "ر.س"

    val sdfFull = SimpleDateFormat("EEEE - yyyy/MM/dd hh:mm a", Locale("ar"))
    val currentDateTimeStr = remember { sdfFull.format(Date()) }

    val filteredExpenses = expenses.filter { exp ->
        selectedFilterImportance == null || exp.importance == selectedFilterImportance
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        // Input Form Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "تسجيل مصروف جديد 🛒",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Auto captured Date & Time info banner
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "التاريخ والوقت المحفوظ تلقائياً:",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = currentDateTimeStr,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Title Field
                    OutlinedTextField(
                        value = titleText,
                        onValueChange = { titleText = it },
                        label = { Text("اسم الشغلة / المشتريات") },
                        placeholder = { Text("مثال: شراء مؤن سوبرماركت، فاتورة كهرباء...") },
                        leadingIcon = { Icon(Icons.Default.ShoppingBag, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Amount Field
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("السعر / المبلغ ($currency)") },
                        placeholder = { Text("0.0") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        leadingIcon = { Icon(Icons.Default.Label, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Category Selector
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedCategory,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("الفئة") },
                            leadingIcon = { Icon(Icons.Default.Category, contentDescription = null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { categoryDropdownExpanded = true },
                            enabled = false,
                            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = MaterialTheme.colorScheme.outline,
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { categoryDropdownExpanded = true }
                        )
                        DropdownMenu(
                            expanded = categoryDropdownExpanded,
                            onDismissRequest = { categoryDropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.85f)
                        ) {
                            categoriesList.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category) },
                                    onClick = {
                                        selectedCategory = category
                                        categoryDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Importance Level Selector
                    Column {
                        Text(
                            text = "مدى أهمية هذا المصروف:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ImportanceChip(
                                label = "أساسي",
                                subLabel = "ضروري",
                                color = EssentialColor,
                                selected = selectedImportance == ImportanceLevel.ESSENTIAL,
                                onClick = { selectedImportance = ImportanceLevel.ESSENTIAL },
                                modifier = Modifier.weight(1f)
                            )

                            ImportanceChip(
                                label = "حاجة",
                                subLabel = "هام",
                                color = NeedColor,
                                selected = selectedImportance == ImportanceLevel.NEED,
                                onClick = { selectedImportance = ImportanceLevel.NEED },
                                modifier = Modifier.weight(1f)
                            )

                            ImportanceChip(
                                label = "كماليات",
                                subLabel = "ترفيه",
                                color = LuxuryColor,
                                selected = selectedImportance == ImportanceLevel.LUXURY,
                                onClick = { selectedImportance = ImportanceLevel.LUXURY },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Notes Field
                    OutlinedTextField(
                        value = notesText,
                        onValueChange = { notesText = it },
                        label = { Text("ملاحظات إضافية (اختياري)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )

                    // Submit Button
                    Button(
                        onClick = {
                            val amountVal = amountText.toDoubleOrNull()
                            if (titleText.isNotBlank() && amountVal != null && amountVal > 0) {
                                viewModel.addExpense(
                                    title = titleText.trim(),
                                    amount = amountVal,
                                    category = selectedCategory,
                                    importance = selectedImportance,
                                    notes = notesText.trim(),
                                    context = context
                                )
                                // Clear inputs
                                titleText = ""
                                amountText = ""
                                notesText = ""
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        enabled = titleText.isNotBlank() && (amountText.toDoubleOrNull() ?: 0.0) > 0
                    ) {
                        Icon(imageVector = Icons.Default.AddCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "حفظ وإضافة المصروف", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Expenses List Section
        item {
            Column {
                Text(
                    text = "سجل المصاريف الشهرية (${filteredExpenses.size})",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Filter Chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedFilterImportance == null,
                        onClick = { selectedFilterImportance = null },
                        label = { Text("الكل") }
                    )
                    FilterChip(
                        selected = selectedFilterImportance == ImportanceLevel.ESSENTIAL,
                        onClick = { selectedFilterImportance = ImportanceLevel.ESSENTIAL },
                        label = { Text("أساسي") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = EssentialColor.copy(alpha = 0.2f))
                    )
                    FilterChip(
                        selected = selectedFilterImportance == ImportanceLevel.NEED,
                        onClick = { selectedFilterImportance = ImportanceLevel.NEED },
                        label = { Text("حاجة") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = NeedColor.copy(alpha = 0.2f))
                    )
                    FilterChip(
                        selected = selectedFilterImportance == ImportanceLevel.LUXURY,
                        onClick = { selectedFilterImportance = ImportanceLevel.LUXURY },
                        label = { Text("كماليات") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = LuxuryColor.copy(alpha = 0.2f))
                    )
                }
            }
        }

        if (filteredExpenses.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "لا توجد نتائج مسجلة لهذه الفئة",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        } else {
            items(filteredExpenses, key = { it.id }) { expense ->
                ExpenseItemCard(
                    expense = expense,
                    currency = currency,
                    onDelete = { viewModel.deleteExpense(expense) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
fun ImportanceChip(
    label: String,
    subLabel: String,
    color: Color,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (selected) color else color.copy(alpha = 0.12f)
    val contentColor = if (selected) Color.White else color

    Card(
        modifier = modifier
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 10.dp, horizontal = 8.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            Text(
                text = subLabel,
                fontSize = 10.sp,
                color = contentColor.copy(alpha = 0.8f)
            )
        }
    }
}
