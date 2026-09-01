package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.BillItem
import java.io.File
import java.io.FileOutputStream
import java.text.DecimalFormat

enum class PrintPosition(val label: String, val description: String) {
    TOP("উপরে (Top)", "A4 কাগজের উপরের অর্ধেকাংশে"),
    BOTTOM("নিচে (Bottom)", "A4 কাগজের নিচের অর্ধেকাংশে"),
    BOTH("উভয় অংশ (২টি মেমো)", "A4 পাতায় ২টি মেমো একসাথে (উপরে ও নিচে)")
}

data class PrintMemoData(
    val memoId: Long = 0L,
    val centerName: String = "",
    val subtitle: String = "",
    val dateString: String = "",
    val purchaserName: String = "",
    val purchaserLabel: String = "ক্রয়কারীর স্বাক্ষর",
    val items: List<BillItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val billType: String = "market"
)

object PrintUtils {

    fun printFoodBillDual(
        context: Context,
        topMemo: PrintMemoData?,
        bottomMemo: PrintMemoData?,
        position: PrintPosition = PrintPosition.TOP
    ) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
        val dateLabel = topMemo?.dateString?.replace("/", "-") ?: "Bill"
        val jobName = "Food_Bill_$dateLabel"

        val printAdapter = object : PrintDocumentAdapter() {
            override fun onLayout(
                oldAttributes: PrintAttributes?,
                newAttributes: PrintAttributes?,
                cancellationSignal: CancellationSignal?,
                callback: LayoutResultCallback?,
                extras: Bundle?
            ) {
                if (cancellationSignal?.isCanceled == true) {
                    callback?.onLayoutCancelled()
                    return
                }
                val info = PrintDocumentInfo.Builder("$jobName.pdf")
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(1)
                    .build()
                callback?.onLayoutFinished(info, true)
            }

            override fun onWrite(
                pages: Array<out PageRange>?,
                destination: ParcelFileDescriptor?,
                cancellationSignal: CancellationSignal?,
                callback: WriteResultCallback?
            ) {
                if (destination == null) return
                val pdfDocument = createFoodBillPdfDocument(
                    topMemo = topMemo,
                    bottomMemo = bottomMemo,
                    position = position
                )
                try {
                    FileOutputStream(destination.fileDescriptor).use { out ->
                        pdfDocument.writeTo(out)
                    }
                    callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                } catch (e: Exception) {
                    callback?.onWriteFailed(e.message)
                } finally {
                    pdfDocument.close()
                }
            }
        }

        val builder = PrintAttributes.Builder()
        builder.setMediaSize(PrintAttributes.MediaSize.ISO_A4.asPortrait())
        builder.setMinMargins(PrintAttributes.Margins(0, 0, 0, 0))

        printManager?.print(jobName, printAdapter, builder.build())
    }

    fun shareFoodBillPdfDual(
        context: Context,
        topMemo: PrintMemoData?,
        bottomMemo: PrintMemoData?,
        position: PrintPosition = PrintPosition.TOP
    ) {
        try {
            val pdfDocument = createFoodBillPdfDocument(
                topMemo = topMemo,
                bottomMemo = bottomMemo,
                position = position
            )

            val dateLabel = topMemo?.dateString?.replace("/", "-") ?: "Bill"
            val cacheDir = File(context.cacheDir, "food_bills").apply { mkdirs() }
            val pdfFile = File(cacheDir, "Food_Bill_$dateLabel.pdf")
            if (pdfFile.exists()) pdfFile.delete()

            FileOutputStream(pdfFile).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()

            sharePdfFile(context, pdfFile, topMemo?.dateString ?: "", topMemo?.centerName ?: "")
        } catch (e: Exception) {
            Toast.makeText(context, "পিডিএফ ফাইল তৈরি ব্যর্থ: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    fun printFoodBill(
        context: Context,
        centerName: String = "প্রতিষ্ঠানের নাম লিখুন",
        subtitle: String = "দৈনিক খাবার বিল",
        dateString: String,
        items: List<BillItem>,
        totalAmount: Double,
        purchaserLabel: String = "ক্রেতার স্বাক্ষর",
        approverLabel: String = "অনুমোদনকারীর স্বাক্ষর",
        position: PrintPosition = PrintPosition.TOP
    ) {
        val memo = PrintMemoData(
            centerName = centerName,
            subtitle = subtitle,
            dateString = dateString,
            purchaserLabel = purchaserLabel,
            items = items,
            totalAmount = totalAmount
        )
        printFoodBillDual(context, topMemo = memo, bottomMemo = memo, position = position)
    }

    fun shareFoodBillPdf(
        context: Context,
        centerName: String = "প্রতিষ্ঠানের নাম লিখুন",
        subtitle: String = "দৈনিক খাবার বিল",
        dateString: String,
        items: List<BillItem>,
        totalAmount: Double,
        purchaserLabel: String = "ক্রেতার স্বাক্ষর",
        approverLabel: String = "অনুমোদনকারীর স্বাক্ষর",
        position: PrintPosition = PrintPosition.TOP
    ) {
        val memo = PrintMemoData(
            centerName = centerName,
            subtitle = subtitle,
            dateString = dateString,
            purchaserLabel = purchaserLabel,
            items = items,
            totalAmount = totalAmount
        )
        shareFoodBillPdfDual(context, topMemo = memo, bottomMemo = memo, position = position)
    }

    fun saveFoodBillImage(context: Context, memo: PrintMemoData): Boolean {
        return FoodBillImageExporter.saveMemoImageToGallery(context, memo)
    }

    fun shareFoodBillImage(context: Context, memo: PrintMemoData) {
        FoodBillImageExporter.shareMemoImage(context, memo)
    }

    private fun createFoodBillPdfDocument(
        topMemo: PrintMemoData?,
        bottomMemo: PrintMemoData?,
        position: PrintPosition
    ): PdfDocument {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val bgPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, 595f, 842f, bgPaint)

        val resolvedTop = topMemo ?: bottomMemo
        val resolvedBottom = bottomMemo ?: topMemo

        when (position) {
            PrintPosition.TOP -> {
                resolvedTop?.let {
                    drawSingleVoucherOnCanvas(canvas = canvas, startY = 0f, memo = it)
                }
            }
            PrintPosition.BOTTOM -> {
                resolvedBottom?.let {
                    drawSingleVoucherOnCanvas(canvas = canvas, startY = 421f, memo = it)
                }
            }
            PrintPosition.BOTH -> {
                resolvedTop?.let {
                    drawSingleVoucherOnCanvas(canvas = canvas, startY = 0f, memo = it)
                }
                resolvedBottom?.let {
                    drawSingleVoucherOnCanvas(canvas = canvas, startY = 421f, memo = it)
                }
            }
        }

        pdfDocument.finishPage(page)
        return pdfDocument
    }

    private fun drawSingleVoucherOnCanvas(
        canvas: android.graphics.Canvas,
        startY: Float,
        memo: PrintMemoData
    ) {
        canvas.save()
        canvas.translate(15f, startY + 423f)
        canvas.rotate(-90f)

        val localStartY = 0f

        val tableLeft = 10f
        val tableRight = 410f
        val tableWidth = tableRight - tableLeft
        val bannerCenterX = tableLeft + (tableWidth / 2f)

        // Header Box
        val headerRect = RectF(tableLeft, localStartY, tableRight, localStartY + 44f)
        val headerBoxBorderPaint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
        }
        val headerBgPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawRect(headerRect, headerBgPaint)
        canvas.drawRect(headerRect, headerBoxBorderPaint)

        // Header Title
        if (memo.centerName.isNotBlank()) {
            val titleTextPaint = Paint().apply {
                isAntiAlias = true
                color = Color.BLACK
                textSize = 18f
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(memo.centerName, bannerCenterX, localStartY + 24f, titleTextPaint)
        }

        // Subtitle
        if (memo.subtitle.isNotBlank()) {
            val subtitleTextPaint = Paint().apply {
                isAntiAlias = true
                color = Color.BLACK
                textSize = 11f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(memo.subtitle, bannerCenterX, localStartY + 38f, subtitleTextPaint)
        }

        // Header Separator Line
        val headerLinePaint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            strokeWidth = 1.5f
        }
        canvas.drawLine(tableLeft, localStartY + 48f, tableRight, localStartY + 48f, headerLinePaint)

        // Metadata Row (Date)
        val metaPaintRight = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = 11.5f
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }
        val bnDate = BengaliUtils.toBengaliDigits(memo.dateString)
        canvas.drawText("তারিখ : $bnDate", tableRight, localStartY + 66f, metaPaintRight)

        // Table Grid Config
        val tableTop = localStartY + 76f
        val colWidths = floatArrayOf(45f, 168f, 62f, 48f, 77f)

        // Table Header Box
        val headerHeight = 24f
        val tableHeaderRect = RectF(tableLeft, tableTop, tableRight, tableTop + headerHeight)
        canvas.drawRect(tableHeaderRect, headerBgPaint)
        canvas.drawRect(tableHeaderRect, headerBoxBorderPaint)

        val headerTextPaint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = 11f
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val colTitles = if (memo.billType == "transport") {
            arrayOf("ক্র. নং", "বিবরণ / রুট", "মাধ্যম", "যাত্রী", "টাকা")
        } else {
            arrayOf("ক্র. নং", "খাবারের নাম / বিবরণ", "পরিমাণ", "দর", "টাকা")
        }
        var currentX = tableLeft
        for (i in 0 until 5) {
            val colCenterX = currentX + colWidths[i] / 2f
            canvas.drawText(colTitles[i], colCenterX, tableTop + 16f, headerTextPaint)
            currentX += colWidths[i]
        }

        // Table Rows
        val validItems = memo.items.filter { it.name.isNotBlank() || it.amount > 0 }
        val totalRows = maxOf(14, validItems.size.coerceAtMost(18))
        val gridTop = tableTop + headerHeight
        val baseRowHeight = 27.1f // same density as the original fixed 14-row layout
        val itemFontSize = 11.5f
        val nameLineHeight = itemFontSize + 3.5f
        val nameVerticalPad = 6f
        val namePadLeft = 6f
        val namePadRight = 4f
        val maxNameWidth = colWidths[1] - namePadLeft - namePadRight

        val gridBorderPaint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 1.2f
        }
        val rowDashPaint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            strokeWidth = 0.6f
            pathEffect = android.graphics.DashPathEffect(floatArrayOf(3f, 3f), 0f)
        }

        val itemTextPaint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = itemFontSize
        }
        val itemBoldPaint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = itemFontSize
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        }

        // Pre-compute wrapped name lines & the resulting height for every row first,
        // so long names grow the row instead of shrinking font size / overlapping.
        val rowNameLines = ArrayList<List<String>>(totalRows)
        val rowHeights = FloatArray(totalRows)
        for (r in 0 until totalRows) {
            if (r < validItems.size) {
                val lines = CanvasTextUtils.wrapText(validItems[r].name, itemBoldPaint, maxNameWidth)
                rowNameLines.add(lines)
                val neededHeight = (lines.size * nameLineHeight) + nameVerticalPad
                rowHeights[r] = maxOf(baseRowHeight, neededHeight)
            } else {
                rowNameLines.add(listOf(""))
                rowHeights[r] = baseRowHeight
            }
        }

        var rowY = gridTop
        for (r in 0 until totalRows) {
            val rowHeight = rowHeights[r]
            val slNo = BengaliUtils.toBengaliDigits(String.format("%02d", r + 1))
            val centerY = rowY + (rowHeight / 2f) + (itemFontSize * 0.35f)

            itemBoldPaint.textAlign = Paint.Align.CENTER
            canvas.drawText(slNo, tableLeft + colWidths[0] / 2f, centerY, itemBoldPaint)

            if (r < validItems.size) {
                val item = validItems[r]

                // Name / Description - wraps onto multiple lines, growing the row.
                itemBoldPaint.textAlign = Paint.Align.LEFT
                val lines = rowNameLines[r]
                val firstLineY = rowY + nameVerticalPad / 2f + itemFontSize
                for ((i, line) in lines.withIndex()) {
                    canvas.drawText(line, tableLeft + colWidths[0] + namePadLeft, firstLineY + (i * nameLineHeight), itemBoldPaint)
                }

                itemTextPaint.textAlign = Paint.Align.CENTER
                val bnQty = BengaliUtils.toBengaliDigits(item.quantity)
                canvas.drawText(bnQty, tableLeft + colWidths[0] + colWidths[1] + colWidths[2] / 2f, centerY, itemTextPaint)

                val bnRate = if (item.rate == "0" || item.rate.isBlank()) "" else BengaliUtils.toBengaliDigits(item.rate)
                canvas.drawText(bnRate, tableLeft + colWidths[0] + colWidths[1] + colWidths[2] + colWidths[3] / 2f, centerY, itemTextPaint)

                itemBoldPaint.textAlign = Paint.Align.RIGHT
                val bnAmount = if (item.amount <= 0) "—" else "${BengaliUtils.toBengaliDigits(DecimalFormat("#,##0").format(item.amount))}/-"
                canvas.drawText(bnAmount, tableRight - 6f, centerY, itemBoldPaint)
            }

            canvas.drawLine(tableLeft, rowY + rowHeight, tableRight, rowY + rowHeight, rowDashPaint)
            rowY += rowHeight
        }

        val totalRowY = rowY
        canvas.drawLine(tableLeft, totalRowY, tableRight, totalRowY, gridBorderPaint)

        val totalLabelPaint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = 12f
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("মোট —", tableLeft + colWidths[0] + colWidths[1] + colWidths[2] + colWidths[3] - 6f, totalRowY + 16f, totalLabelPaint)

        val totalValPaint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = 12.5f
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }
        val bnTotal = if (memo.totalAmount <= 0) "0/-" else "${BengaliUtils.formatBengaliCurrency(memo.totalAmount)}/-"
        canvas.drawText(bnTotal, tableRight - 6f, totalRowY + 16f, totalValPaint)

        val tableBottomY = totalRowY + 22f
        canvas.drawLine(tableLeft, tableBottomY, tableRight, tableBottomY, gridBorderPaint)

        canvas.drawRect(tableLeft, tableTop, tableRight, tableBottomY, gridBorderPaint)
        
        var lineX = tableLeft
        for (i in 0 until 4) {
            lineX += colWidths[i]
            canvas.drawLine(lineX, tableTop, lineX, tableBottomY, gridBorderPaint)
        }

        // Footer (positioned relative to the table's actual bottom, since the
        // table height now varies depending on how many lines the names wrap to)
        val sigLineY = tableBottomY + 45f
        val sigLineWidth = 150f

        val linePaint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            strokeWidth = 1f
        }

        val sigPaint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            textSize = 11f
        }

        canvas.drawLine(tableLeft, sigLineY, tableLeft + sigLineWidth, sigLineY, linePaint)
        val labelText = memo.purchaserLabel.ifBlank { "ক্রয়কারীর স্বাক্ষর" }
        canvas.drawText(labelText, tableLeft + (sigLineWidth / 2f), sigLineY + 14f, sigPaint)

        canvas.restore()
    }

    private fun sharePdfFile(context: Context, pdfFile: File, dateString: String, centerName: String = "") {
        try {
            val authority = "${context.packageName}.fileprovider"
            val contentUri: Uri = FileProvider.getUriForFile(context, authority, pdfFile)
            val displayName = centerName.ifBlank { "খাবার বিল" }

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, "খাবার বিল - $dateString")
                putExtra(Intent.EXTRA_TEXT, "$displayName - $dateString তারিখের খাবার বিল।")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "পিডিএফ শেয়ার করুন (WhatsApp / আদার্স)")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "শেয়ার করতে সমস্যা হয়েছে: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun escapeHtml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }

    private fun renderMemoCardHtml(memo: PrintMemoData): String {
        val safeCenterName = escapeHtml(memo.centerName)
        val safeSubtitle = escapeHtml(memo.subtitle)
        val safePurchaserLabel = escapeHtml(memo.purchaserLabel.ifBlank { "ক্রয়কারীর স্বাক্ষর" })
        val bengaliDate = BengaliUtils.toBengaliDigits(memo.dateString)
        val bengaliTotal = if (memo.totalAmount <= 0) "0/-" else "${BengaliUtils.formatBengaliCurrency(memo.totalAmount)}/-"

        val validItems = memo.items.filter { it.name.isNotBlank() || it.amount > 0 }
        val totalRowsCount = maxOf(14, validItems.size.coerceAtMost(18))
        val rowHeightCss = if (totalRowsCount > 14) "${(350 / totalRowsCount)}px" else "25px"
        val fontSizeCss = if (totalRowsCount > 14) "10px" else "11.5px"
        val cellPaddingCss = if (totalRowsCount > 14) "2px 3px" else "3px 4px"

        val rowsHtml = StringBuilder()
        for (i in 0 until totalRowsCount) {
            val slNo = BengaliUtils.toBengaliDigits(String.format("%02d", i + 1))
            if (i < validItems.size) {
                val item = validItems[i]
                val bnQty = BengaliUtils.toBengaliDigits(item.quantity)
                val bnRate = if (item.rate == "0" || item.rate.isBlank()) "" else BengaliUtils.toBengaliDigits(item.rate)
                val bnAmount = if (item.amount <= 0) "—" else "${BengaliUtils.toBengaliDigits(DecimalFormat("#,##0").format(item.amount))}/-"

                rowsHtml.append("""
                    <tr>
                        <td class="sl-col">$slNo</td>
                        <td class="item-col">${escapeHtml(item.name)}</td>
                        <td class="qty-col">$bnQty</td>
                        <td class="rate-col">$bnRate</td>
                        <td class="amount-col">$bnAmount</td>
                    </tr>
                """.trimIndent())
            } else {
                rowsHtml.append("""
                    <tr>
                        <td class="sl-col">$slNo</td>
                        <td class="item-col"></td>
                        <td class="qty-col"></td>
                        <td class="rate-col"></td>
                        <td class="amount-col"></td>
                    </tr>
                """.trimIndent())
            }
        }

        return """
            <div class="memo-half">
                <div class="memo-card">
                    <div class="header-banner">
                        ${if (safeCenterName.isNotBlank()) "<h1>$safeCenterName</h1>" else ""}
                        ${if (safeSubtitle.isNotBlank()) "<p>$safeSubtitle</p>" else ""}
                    </div>
                    <div class="sawtooth-bar"></div>

                    <div class="meta-row">
                        <div>তারিখ : $bengaliDate</div>
                    </div>

                    <div class="table-wrapper">
                        <table class="memo-table">
                            <thead>
                                <tr>
                                    <th style="width: 12%;">ক্র. নং</th>
                                    <th style="width: 43%;">${if (memo.billType == "transport") "বিবরণ / রুট" else "খাবারের নাম / বিবরণ"}</th>
                                    <th style="width: 15%;">${if (memo.billType == "transport") "মাধ্যম" else "পরিমাণ"}</th>
                                    <th style="width: 12%;">${if (memo.billType == "transport") "যাত্রী" else "দর"}</th>
                                    <th style="width: 18%;">টাকা</th>
                                </tr>
                            </thead>
                            <tbody>
                                $rowsHtml
                                <tr class="total-row">
                                    <td colspan="4" class="total-label">মোট —</td>
                                    <td class="total-amount">$bengaliTotal</td>
                                </tr>
                            </tbody>
                        </table>
                    </div>

                    <div class="footer-section">
                        <div class="signatures-row">
                            <div class="sig-box">
                                $safePurchaserLabel
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        """.trimIndent()
    }

    fun generateHtmlVoucher(
        topMemo: PrintMemoData?,
        bottomMemo: PrintMemoData?,
        position: PrintPosition = PrintPosition.TOP
    ): String {
        val resolvedTop = topMemo ?: bottomMemo ?: PrintMemoData()
        val resolvedBottom = bottomMemo ?: topMemo ?: PrintMemoData()

        val topHtml = renderMemoCardHtml(resolvedTop)
        val bottomHtml = renderMemoCardHtml(resolvedBottom)
        val emptyHalfHtml = """<div class="empty-half"></div>""".trimIndent()

        val contentBodyHtml = when (position) {
            PrintPosition.TOP -> """
                $topHtml
                $emptyHalfHtml
            """.trimIndent()
            PrintPosition.BOTTOM -> """
                $emptyHalfHtml
                $bottomHtml
            """.trimIndent()
            PrintPosition.BOTH -> """
                $topHtml
                $bottomHtml
            """.trimIndent()
        }

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
                <style>
                    @page {
                        size: A4 portrait;
                        margin: 0;
                    }
                    body {
                        font-family: 'SolaimanLipi', 'Kalpurush', 'Noto Sans Bengali', Arial, sans-serif;
                        margin: 0;
                        padding: 0;
                        background-color: #FFFFFF;
                        color: #000000;
                        -webkit-print-color-adjust: exact;
                        print-color-adjust: exact;
                    }
                    .memo-half {
                        width: 210mm;
                        height: 148mm;
                        padding: 4mm 6mm;
                        box-sizing: border-box;
                        position: relative;
                        overflow: hidden;
                    }
                    .empty-half {
                        width: 210mm;
                        height: 148mm;
                    }
                    .memo-card {
                        width: 100%;
                        height: 100%;
                        background: #FFFFFF;
                        box-sizing: border-box;
                    }
                    .header-banner {
                        background-color: #FFFFFF;
                        color: #000000;
                        text-align: center;
                        padding: 8px 6px 6px 6px;
                        border: 1.5px solid #000000;
                        border-radius: 4px;
                    }
                    .header-banner h1 {
                        margin: 0;
                        font-size: 22px;
                        font-weight: bold;
                        color: #000000;
                        letter-spacing: 0.5px;
                    }
                    .header-banner p {
                        margin: 2px 0 0 0;
                        font-size: 12px;
                        font-weight: normal;
                        color: #000000;
                    }
                    .sawtooth-bar {
                        height: 2px;
                        background-color: #000000;
                        margin-top: 4px;
                        margin-bottom: 6px;
                    }
                    .meta-row {
                        display: flex;
                        justify-content: flex-end;
                        align-items: center;
                        padding: 4px 6px;
                        font-size: 12px;
                        font-weight: bold;
                        color: #000000;
                    }
                    .table-wrapper {
                        padding: 0 6px;
                    }
                    .memo-table {
                        width: 100%;
                        border-collapse: collapse;
                        border: 1.8px solid #000000;
                        border-radius: 4px;
                        overflow: hidden;
                    }
                    .memo-table th {
                        background-color: #FFFFFF;
                        color: #000000;
                        font-weight: bold;
                        font-size: 11px;
                        padding: 4px 2px;
                        border-right: 1.2px solid #000000;
                        border-bottom: 1.8px solid #000000;
                        text-align: center;
                        white-space: nowrap;
                    }
                    .memo-table th:last-child {
                        border-right: none;
                    }
                    .memo-table td {
                        border-right: 1.2px solid #000000;
                        border-bottom: 1px dashed #666666;
                        padding: 2px 3px;
                        font-size: 11px;
                        color: #000000;
                    }
                    .memo-table td:last-child {
                        border-right: none;
                    }
                    .sl-col { text-align: center; font-weight: bold; width: 12%; }
                    .item-col { text-align: left; font-weight: bold; width: 43%; }
                    .qty-col { text-align: center; width: 15%; }
                    .rate-col { text-align: center; width: 12%; }
                    .amount-col { text-align: right; font-weight: bold; width: 18%; }

                    .total-row td {
                        border-top: 1.8px solid #000000;
                        border-bottom: none;
                        font-weight: bold;
                    }
                    .total-label {
                        text-align: right;
                        font-size: 12px;
                        color: #000000;
                        padding-right: 10px;
                        border-right: 1.2px solid #000000;
                    }
                    .total-amount {
                        text-align: right;
                        font-size: 13px;
                        color: #000000;
                        font-weight: bold;
                    }
                    .footer-section {
                        padding: 10px 6px 0 6px;
                        font-size: 11px;
                        color: #000000;
                    }
                    .signatures-row {
                        display: flex;
                        justify-content: space-between;
                        align-items: flex-end;
                        margin-top: 12px;
                    }
                    .sig-box {
                        font-weight: bold;
                        color: #000000;
                        border-top: 1px solid #000000;
                        width: 165px;
                        text-align: center;
                        padding-top: 4px;
                        font-size: 11px;
                    }
                </style>
            </head>
            <body>
                $contentBodyHtml
            </body>
            </html>
        """.trimIndent()
    }
}
