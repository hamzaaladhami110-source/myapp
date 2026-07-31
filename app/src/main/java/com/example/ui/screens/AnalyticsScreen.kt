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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Expense
import com.example.data.model.MonthlyBudget
import com.example.data.repository.ComparisonResult
import com.example.data.repository.SpendingEvaluation
import com.example.pdf.PdfReportExporter
import com.example.ui.MainViewModel
import com.example.ui.components.CategoryBarChart
import com.example.ui.components.ImportanceDonutChart
import java.util.Locale

@Composable
fun AnalyticsScreen(
    viewModel: MainViewModel,
    budget: MonthlyBudget?,
    expenses: List<Expense>,
    evaluation: SpendingEvaluation,
    comparison: ComparisonResult
) {
    val context = LocalContext.current
    val currency = budget?.currency ?: "ر.س"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        // Header Title
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Assessment,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "تقييم المدفوعات والرسوم البيانية",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // 1. Evaluation Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "تقييم أداء الصرف لهذا الشهر",
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = evaluation.ratingTitle,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Score Badge
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        evaluation.score >= 80 -> Color(0xFF059669)
                                        evaluation.score >= 50 -> Color(0xFFD97706)
                                        else -> Color(0xFFDC2626)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${evaluation.score}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "من 100",
                                    fontSize = 9.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = evaluation.message,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        // 2. Precise Comparison with Previous Months Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CompareArrows,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "مقارنة بمتوسط الأشهر السابقة",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (comparison.sampleMonthsCount == 0) {
                        Text(
                            text = "هذا هو شهرك الأول في التطبيق! ستظهر المقارنة الدقيقة فور توفر بيانات للأشهر القادمة.",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("إنفاقك الحالي", fontSize = 11.sp, color = Color.Gray)
                                Text(
                                    "${String.format(Locale.US, "%.1f", comparison.currentSpent)} $currency",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Column {
                                Text("متوسط الإنفاق السابق", fontSize = 11.sp, color = Color.Gray)
                                Text(
                                    "${String.format(Locale.US, "%.1f", comparison.previousAverageSpent)} $currency",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Column {
                                Text("الفارق المئوي", fontSize = 11.sp, color = Color.Gray)
                                val diffText = if (comparison.isHigherThanAverage) {
                                    "+${String.format(Locale.US, "%.1f%%", comparison.percentageChange)}"
                                } else {
                                    "${String.format(Locale.US, "%.1f%%", comparison.percentageChange)}"
                                }
                                val diffColor = if (comparison.isHigherThanAverage) Color(0xFFDC2626) else Color(0xFF059669)

                                Text(
                                    diffText,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = diffColor
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (comparison.isHigherThanAverage) {
                                "⚠️ إنفاقك هذا الشهر أعلى بنسبة ${String.format(Locale.US, "%.1f%%", comparison.percentageChange)} مقارنة بمتوسط السجلات السابقة (${comparison.sampleMonthsCount} أشهر)."
                            } else {
                                "🎉 ممتاز! إنفاقك هذا الشهر أقل بنسبة ${String.format(Locale.US, "%.1f%%", Math.abs(comparison.percentageChange))} مقارنة بمتوسط السجلات السابقة."
                            },
                            fontSize = 12.sp,
                            color = if (comparison.isHigherThanAverage) Color(0xFFDC2626) else Color(0xFF059669),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // 3. Importance Donut Chart
        item {
            ImportanceDonutChart(expenses = expenses)
        }

        // 4. Category Breakdown Bar Chart
        item {
            CategoryBarChart(expenses = expenses, currency = currency)
        }

        // 5. PDF Export Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "تصدير تقرير شهري شامل (PDF)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "يتضمن التقرير ملخص الميزانية، والتقييم، وجدول التفاصيل كاملاً مع إمكانية المشاركة والطباعة.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            val pdfFile = viewModel.exportPdfReport(context)
                            if (pdfFile != null) {
                                PdfReportExporter.sharePdfFile(context, pdfFile)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("توليد ومشاركة ملف PDF")
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}
