package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Expense
import com.example.data.model.ImportanceLevel
import com.example.ui.theme.EssentialColor
import com.example.ui.theme.LuxuryColor
import com.example.ui.theme.NeedColor
import java.util.Locale

@Composable
fun ImportanceDonutChart(
    expenses: List<Expense>,
    modifier: Modifier = Modifier
) {
    val totalSpent = expenses.sumOf { it.amount }
    val essentialSpent = expenses.filter { it.importance == ImportanceLevel.ESSENTIAL }.sumOf { it.amount }
    val needSpent = expenses.filter { it.importance == ImportanceLevel.NEED }.sumOf { it.amount }
    val luxurySpent = expenses.filter { it.importance == ImportanceLevel.LUXURY }.sumOf { it.amount }

    val essentialRatio = if (totalSpent > 0) (essentialSpent / totalSpent).toFloat() else 0f
    val needRatio = if (totalSpent > 0) (needSpent / totalSpent).toFloat() else 0f
    val luxuryRatio = if (totalSpent > 0) (luxurySpent / totalSpent).toFloat() else 0f

    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(expenses) {
        animProgress.animateTo(1f, animationSpec = tween(1000))
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "توزيع المصاريف حسب الأهمية",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (totalSpent <= 0) {
                Text(
                    text = "لا توجد بيانات كافية لرسم المخطط",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 32.dp)
                )
            } else {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(180.dp)
                ) {
                    Canvas(modifier = Modifier.size(170.dp)) {
                        val strokeWidth = 32.dp.toPx()
                        val arcSize = size.minDimension - strokeWidth
                        val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                        var startAngle = -90f

                        // 1. Essential Arc
                        val essentialSweep = 360f * essentialRatio * animProgress.value
                        if (essentialSweep > 0) {
                            drawArc(
                                color = EssentialColor,
                                startAngle = startAngle,
                                sweepAngle = essentialSweep,
                                useCenter = false,
                                topLeft = topLeft,
                                size = Size(arcSize, arcSize),
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                            )
                            startAngle += essentialSweep
                        }

                        // 2. Need Arc
                        val needSweep = 360f * needRatio * animProgress.value
                        if (needSweep > 0) {
                            drawArc(
                                color = NeedColor,
                                startAngle = startAngle,
                                sweepAngle = needSweep,
                                useCenter = false,
                                topLeft = topLeft,
                                size = Size(arcSize, arcSize),
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                            )
                            startAngle += needSweep
                        }

                        // 3. Luxury Arc
                        val luxurySweep = 360f * luxuryRatio * animProgress.value
                        if (luxurySweep > 0) {
                            drawArc(
                                color = LuxuryColor,
                                startAngle = startAngle,
                                sweepAngle = luxurySweep,
                                useCenter = false,
                                topLeft = topLeft,
                                size = Size(arcSize, arcSize),
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "الإجمالي",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = String.format(Locale.US, "%.0f", totalSpent),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Legend
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    LegendItem(
                        color = EssentialColor,
                        label = "أساسي",
                        value = String.format(Locale.US, "%.0f%%", essentialRatio * 100)
                    )
                    LegendItem(
                        color = NeedColor,
                        label = "حاجة",
                        value = String.format(Locale.US, "%.0f%%", needRatio * 100)
                    )
                    LegendItem(
                        color = LuxuryColor,
                        label = "كماليات",
                        value = String.format(Locale.US, "%.0f%%", luxuryRatio * 100)
                    )
                }
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun CategoryBarChart(
    expenses: List<Expense>,
    currency: String,
    modifier: Modifier = Modifier
) {
    val categoryTotals = expenses.groupBy { it.category }
        .mapValues { entry -> entry.value.sumOf { it.amount } }
        .toList()
        .sortedByDescending { it.second }

    val maxAmount = categoryTotals.maxOfOrNull { it.second }?.coerceAtLeast(1.0) ?: 1.0

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "تحليل الإنفاق حسب الفئات",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (categoryTotals.isEmpty()) {
                Text(
                    text = "لا توجد مصاريف مسجلة للعرض",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                categoryTotals.take(6).forEach { (category, total) ->
                    val fraction = (total / maxAmount).toFloat()
                    Column(modifier = Modifier.padding(vertical = 6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = category,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${String.format(Locale.US, "%.1f", total)} $currency",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(Color(0xFFE2E8F0))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fraction)
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }
                    }
                }
            }
        }
    }
}
