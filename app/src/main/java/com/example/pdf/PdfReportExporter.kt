package com.example.pdf

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.model.Expense
import com.example.data.model.ImportanceLevel
import com.example.data.model.MonthlyBudget
import com.example.data.repository.SpendingEvaluation
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportExporter {

    fun generateAndSharePdfReport(
        context: Context,
        monthKey: String,
        budget: MonthlyBudget?,
        expenses: List<Expense>,
        totalSpent: Double,
        evaluation: SpendingEvaluation
    ): File? {
        val pdfDocument = PdfDocument()

        // Standard A4 dimensions in points (595 x 842)
        val pageWidth = 595
        val pageHeight = 842
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val currency = budget?.currency ?: "ر.س"
        val budgetAmount = budget?.amount ?: 0.0
        val remaining = budgetAmount - totalSpent

        val textPaint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
            isAntiAlias = true
        }

        val titlePaint = Paint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 20f
            isAntiAlias = true
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val subtitlePaint = Paint().apply {
            color = Color.parseColor("#334155")
            textSize = 12f
            isAntiAlias = true
        }

        val headerBoxPaint = Paint().apply {
            color = Color.parseColor("#F1F5F9")
            style = Paint.Style.FILL
        }

        val borderPaint = Paint().apply {
            color = Color.parseColor("#CBD5E1")
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }

        var y = 40f

        // Draw Document Header
        canvas.drawRoundRect(20f, y, (pageWidth - 20).toFloat(), y + 60f, 12f, 12f, headerBoxPaint)
        canvas.drawText("تقرير الميزانية والمصاريف الشهرية - $monthKey", 35f, y + 30f, titlePaint)
        
        val sdfDate = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.US)
        canvas.drawText("تاريخ التقرير: ${sdfDate.format(Date())}", 35f, y + 48f, subtitlePaint)

        y += 80f

        // Draw Summary Stats Cards (3 Columns)
        val cardWidth = (pageWidth - 60f) / 3f
        
        // 1. Budget Card
        drawCard(
            canvas, 20f, y, cardWidth, 65f,
            "الميزانية المحددة",
            "${String.format(Locale.US, "%.1f", budgetAmount)} $currency",
            "#059669", headerBoxPaint, borderPaint
        )

        // 2. Spent Card
        drawCard(
            canvas, 20f + cardWidth + 10f, y, cardWidth, 65f,
            "إجمالي المصروفات",
            "${String.format(Locale.US, "%.1f", totalSpent)} $currency",
            "#DC2626", headerBoxPaint, borderPaint
        )

        // 3. Remaining Card
        val remainingColor = if (remaining >= 0) "#2563EB" else "#B91C1C"
        drawCard(
            canvas, 20f + (cardWidth * 2) + 20f, y, cardWidth, 65f,
            "المبلغ المتبقي",
            "${String.format(Locale.US, "%.1f", remaining)} $currency",
            remainingColor, headerBoxPaint, borderPaint
        )

        y += 85f

        // Draw Evaluation Box
        canvas.drawRoundRect(20f, y, (pageWidth - 20).toFloat(), y + 55f, 8f, 8f, headerBoxPaint)
        val evalTitlePaint = Paint().apply {
            color = Color.parseColor("#1E293B")
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("التقييم المالي: ${evaluation.ratingTitle}", 32f, y + 24f, evalTitlePaint)

        val evalMsgPaint = Paint().apply {
            color = Color.parseColor("#475569")
            textSize = 10f
            isAntiAlias = true
        }
        val safeMsg = if (evaluation.message.length > 90) evaluation.message.substring(0, 90) + "..." else evaluation.message
        canvas.drawText(safeMsg, 32f, y + 42f, evalMsgPaint)

        y += 75f

        // Draw Importance Breakdown Summary Table
        val essentialSum = expenses.filter { it.importance == ImportanceLevel.ESSENTIAL }.sumOf { it.amount }
        val needSum = expenses.filter { it.importance == ImportanceLevel.NEED }.sumOf { it.amount }
        val luxurySum = expenses.filter { it.importance == ImportanceLevel.LUXURY }.sumOf { it.amount }

        val sectionTitlePaint = Paint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("توزيع المصاريف حسب الأهمية:", 20f, y, sectionTitlePaint)
        y += 15f

        val tableHeaderPaint = Paint().apply {
            color = Color.parseColor("#E2E8F0")
            style = Paint.Style.FILL
        }
        canvas.drawRect(20f, y, (pageWidth - 20).toFloat(), y + 25f, tableHeaderPaint)

        val colPaint = Paint().apply {
            color = Color.parseColor("#1E293B")
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        canvas.drawText("الأهمية", 30f, y + 17f, colPaint)
        canvas.drawText("المبلغ الإجمالي", 200f, y + 17f, colPaint)
        canvas.drawText("النسبة المئوية", 400f, y + 17f, colPaint)

        y += 25f

        val rowTextPaint = Paint().apply {
            color = Color.parseColor("#334155")
            textSize = 10f
            isAntiAlias = true
        }

        fun drawImpRow(label: String, amount: Double) {
            val ratio = if (totalSpent > 0) (amount / totalSpent) * 100 else 0.0
            canvas.drawText(label, 30f, y + 18f, rowTextPaint)
            canvas.drawText("${String.format(Locale.US, "%.1f", amount)} $currency", 200f, y + 18f, rowTextPaint)
            canvas.drawText("${String.format(Locale.US, "%.1f", ratio)}%", 400f, y + 18f, rowTextPaint)
            canvas.drawLine(20f, y + 25f, (pageWidth - 20).toFloat(), y + 25f, borderPaint)
            y += 25f
        }

        drawImpRow("أساسي (ضرورة قصوى)", essentialSum)
        drawImpRow("حاجة (هامة)", needSum)
        drawImpRow("كماليات (ترفيه/زائد)", luxurySum)

        y += 20f

        // Draw Detailed Items Header
        canvas.drawText("جدول المصروفات التفصيلي (${expenses.size} عملية):", 20f, y, sectionTitlePaint)
        y += 15f

        canvas.drawRect(20f, y, (pageWidth - 20).toFloat(), y + 25f, tableHeaderPaint)
        canvas.drawText("#", 25f, y + 17f, colPaint)
        canvas.drawText("المشتريات / البند", 50f, y + 17f, colPaint)
        canvas.drawText("الفئة", 220f, y + 17f, colPaint)
        canvas.drawText("الأهمية", 330f, y + 17f, colPaint)
        canvas.drawText("المبلغ", 440f, y + 17f, colPaint)
        canvas.drawText("التاريخ", 510f, y + 17f, colPaint)

        y += 25f

        val itemSdf = SimpleDateFormat("dd/MM HH:mm", Locale.US)
        expenses.take(20).forEachIndexed { index, exp ->
            if (y > pageHeight - 50) return@forEachIndexed

            canvas.drawText("${index + 1}", 25f, y + 16f, rowTextPaint)
            val titleText = if (exp.title.length > 22) exp.title.substring(0, 22) + ".." else exp.title
            canvas.drawText(titleText, 50f, y + 16f, rowTextPaint)
            val catText = if (exp.category.length > 12) exp.category.substring(0, 12) + ".." else exp.category
            canvas.drawText(catText, 220f, y + 16f, rowTextPaint)
            
            val impShort = when(exp.importance) {
                ImportanceLevel.ESSENTIAL -> "أساسي"
                ImportanceLevel.NEED -> "حاجة"
                ImportanceLevel.LUXURY -> "كماليات"
            }
            canvas.drawText(impShort, 330f, y + 16f, rowTextPaint)
            canvas.drawText("${String.format(Locale.US, "%.1f", exp.amount)}", 440f, y + 16f, rowTextPaint)
            canvas.drawText(itemSdf.format(Date(exp.timestamp)), 510f, y + 16f, rowTextPaint)

            canvas.drawLine(20f, y + 22f, (pageWidth - 20).toFloat(), y + 22f, borderPaint)
            y += 22f
        }

        pdfDocument.finishPage(page)

        // Save PDF file
        return try {
            val reportsDir = File(context.cacheDir, "reports")
            if (!reportsDir.exists()) reportsDir.mkdirs()
            val file = File(reportsDir, "report_$monthKey.pdf")
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()
            outputStream.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }

    private fun drawCard(
        canvas: Canvas, x: Float, y: Float, width: Float, height: Float,
        title: String, value: String, valueColor: String, bgPaint: Paint, borderPaint: Paint
    ) {
        canvas.drawRoundRect(x, y, x + width, y + height, 8f, 8f, bgPaint)
        canvas.drawRoundRect(x, y, x + width, y + height, 8f, 8f, borderPaint)

        val tPaint = Paint().apply {
            color = Color.parseColor("#64748B")
            textSize = 10f
            isAntiAlias = true
        }
        val vPaint = Paint().apply {
            color = Color.parseColor(valueColor)
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        canvas.drawText(title, x + 10f, y + 22f, tPaint)
        canvas.drawText(value, x + 10f, y + 48f, vPaint)
    }

    fun sharePdfFile(context: Context, pdfFile: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "مشاركة تقرير المصاريف PDF"))
    }
}
