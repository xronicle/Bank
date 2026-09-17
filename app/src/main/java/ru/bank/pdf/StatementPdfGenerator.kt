package ru.bank.pdf

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import ru.bank.data.entity.AccountEntity
import ru.bank.data.entity.ClientEntity
import ru.bank.data.entity.OperationEntity
import ru.bank.data.entity.OperationType
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StatementPdfGenerator(private val context: Context) {

    private val pageWidth = 595
    private val pageHeight = 842
    private val margin = 48f
    private val dateFmt = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("ru"))

    fun generate(
        client: ClientEntity,
        account: AccountEntity,
        branchName: String,
        operations: List<OperationEntity>
    ): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas
        var y = margin

        y = drawHeader(canvas, branchName, y)
        y = drawClientBlock(canvas, client, account, y)
        y = drawTableHeader(canvas, y)

        val bodyPaint = textPaint(11f, Typeface.NORMAL)
        val incomePaint = textPaint(11f, Typeface.NORMAL).apply { color = 0xFF1E6E55.toInt() }
        val expensePaint = textPaint(11f, Typeface.NORMAL).apply { color = 0xFF9B3B33.toInt() }

        var income = 0.0
        var expense = 0.0

        operations.sortedBy { it.date }.forEach { op ->
            if (y > pageHeight - margin - 40) {
                document.finishPage(page)
                val newPageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, document.pages.size + 1).create()
                page = document.startPage(newPageInfo)
                canvas = page.canvas
                y = margin
                y = drawTableHeader(canvas, y)
            }
            val isIncome = op.type == OperationType.INCOME
            if (isIncome) income += op.amount else expense += op.amount

            canvas.drawText(dateFmt.format(Date(op.date)), margin, y, bodyPaint)
            canvas.drawText(if (isIncome) "Пополнение" else "Списание", margin + 150, y, bodyPaint)
            val amountText = (if (isIncome) "+" else "−") + "%.2f %s".format(op.amount, account.currencyCode)
            canvas.drawText(amountText, pageWidth - margin - 110, y, if (isIncome) incomePaint else expensePaint)
            y += 20f
        }

        drawSummary(canvas, income, expense, account, y + 12f)
        document.finishPage(page)

        val dir = File(context.filesDir, "statements").apply { mkdirs() }
        val file = File(dir, "statement_${account.accountNumber}_${System.currentTimeMillis()}.pdf")
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()
        return file
    }

    private fun drawHeader(canvas: Canvas, branchName: String, startY: Float): Float {
        var y = startY
        canvas.drawText("Официальная выписка по лицевому счёту", margin, y, textPaint(16f, Typeface.BOLD))
        y += 20f
        canvas.drawText(branchName, margin, y, textPaint(11f, Typeface.NORMAL))
        y += 24f
        canvas.drawLine(margin, y, pageWidth - margin, y, textPaint(1f, Typeface.NORMAL).apply { strokeWidth = 1f })
        return y + 20f
    }

    private fun drawClientBlock(canvas: Canvas, client: ClientEntity, account: AccountEntity, startY: Float): Float {
        var y = startY
        val label = textPaint(10.5f, Typeface.NORMAL).apply { color = 0xFF6B6E76.toInt() }
        val value = textPaint(12f, Typeface.BOLD)

        listOf(
            "Клиент" to client.fullName,
            "Паспорт" to client.passportData,
            "Лицевой счёт" to account.accountNumber,
            "Вид счёта" to account.accountType,
            "Договор" to account.contractNumber,
            "Открыт" to dateFmt.format(Date(account.openedAt)),
            "Валюта" to account.currencyCode,
            "Сформировано" to dateFmt.format(Date())
        ).forEach { (l, v) ->
            canvas.drawText(l.uppercase(), margin, y, label)
            canvas.drawText(v, margin + 150, y, value)
            y += 18f
        }
        return y + 14f
    }

    private fun drawTableHeader(canvas: Canvas, startY: Float): Float {
        val header = textPaint(10.5f, Typeface.BOLD)
        canvas.drawText("ДАТА", margin, startY, header)
        canvas.drawText("ОПЕРАЦИЯ", margin + 150, startY, header)
        canvas.drawText("СУММА", pageWidth - margin - 110, startY, header)
        return startY + 18f
    }

    private fun drawSummary(canvas: Canvas, income: Double, expense: Double, account: AccountEntity, startY: Float) {
        var y = startY
        canvas.drawLine(margin, y, pageWidth - margin, y, textPaint(1f, Typeface.NORMAL).apply { strokeWidth = 1f })
        y += 20f
        canvas.drawText(
            "Приход: +%.2f %s".format(income, account.currencyCode), margin, y,
            textPaint(11.5f, Typeface.NORMAL).apply { color = 0xFF1E6E55.toInt() }
        )
        y += 18f
        canvas.drawText(
            "Расход: −%.2f %s".format(expense, account.currencyCode), margin, y,
            textPaint(11.5f, Typeface.NORMAL).apply { color = 0xFF9B3B33.toInt() }
        )
        y += 18f
        canvas.drawText(
            "Текущий остаток по счёту: %.2f %s".format(account.balance, account.currencyCode),
            margin, y, textPaint(12.5f, Typeface.BOLD)
        )
    }

    private fun textPaint(size: Float, style: Int) = Paint().apply {
        isAntiAlias = true
        textSize = size
        typeface = Typeface.create(Typeface.SANS_SERIF, style)
        color = 0xFF16181D.toInt()
    }
}
