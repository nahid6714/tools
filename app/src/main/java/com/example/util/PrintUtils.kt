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
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.BillItem
import java.io.File
import java.io.FileOutputStream
import java.text.DecimalFormat

enum class PrintPosition(val label: String, val description: String) {
    TOP("উপরে (Top)", "A4 কাগজের উপরের অর্ধেকাংশে"),
    BOTTOM("নিচে (Bottom)", "A4 কাগজের নিচের অর্ধেকাংশে"),
    BOTH("উভয় অংশ (2 Copies)", "A4 পাতায় ২টি কপি একসাথে (উপরে ও নিচে)")
}

object PrintUtils {

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
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
        val jobName = "Food_Bill_${dateString.replace("/", "-")}"

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
                    centerName = centerName,
                    subtitle = subtitle,
                    dateString = dateString,
                    items = items,
                    totalAmount = totalAmount,
                    purchaserLabel = purchaserLabel,
                    approverLabel = approverLabel,
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
        try {
            val pdfDocument = createFoodBillPdfDocument(
                centerName = centerName,
                subtitle = subtitle,
                dateString = dateString,
                items = items,
                totalAmount = totalAmount,
                purchaserLabel = purchaserLabel,
                approverLabel = approverLabel,
                position = position
            )

            val cacheDir = File(context.cacheDir, "food_bills").apply { mkdirs() }
            val pdfFile = File(cacheDir, "Food_Bill_${dateString.replace("/", "-")}.pdf")
            if (pdfFile.exists()) pdfFile.delete()

            FileOutputStream(pdfFile).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()

            sharePdfFile(context, pdfFile, dateString, centerName)
        } catch (e: Exception) {
            Toast.makeText(context, "পিডিএফ ফাইল তৈরি ব্যর্থ: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createFoodBillPdfDocument(
        centerName: String,
        subtitle: String,
        dateString: String,
        items: List<BillItem>,
        totalAmount: Double,
        purchaserLabel: String,
        approverLabel: String,
        position: PrintPosition
    ): PdfDocument {
        val pdfDocument = PdfDocument()
        // Standard A4 dimensions in points: 595 x 842
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        // Pure white paper background for ink-saving printing
        val bgPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, 595f, 842f, bgPaint)

        when (position) {
            PrintPosition.TOP -> {
                drawSingleVoucherOnCanvas(
                    canvas = canvas,
                    startY = 0f,
                    centerName = centerName,
                    subtitle = subtitle,
                    dateString = dateString,
                    items = items,
                    totalAmount = totalAmount,
                    purchaserLabel = purchaserLabel,
                    approverLabel = approverLabel
                )
            }
            PrintPosition.BOTTOM -> {
                drawSingleVoucherOnCanvas(
                    canvas = canvas,
                    startY = 421f,
                    centerName = centerName,
                    subtitle = subtitle,
                    dateString = dateString,
                    items = items,
                    totalAmount = totalAmount,
                    purchaserLabel = purchaserLabel,
                    approverLabel = approverLabel
                )
            }
            PrintPosition.BOTH -> {
                drawSingleVoucherOnCanvas(
                    canvas = canvas,
                    startY = 0f,
                    centerName = centerName,
                    subtitle = subtitle,
                    dateString = dateString,
                    items = items,
                    totalAmount = totalAmount,
                    purchaserLabel = purchaserLabel,
                    approverLabel = approverLabel
                )
                drawSingleVoucherOnCanvas(
                    canvas = canvas,
                    startY = 421f,
                    centerName = centerName,
                    subtitle = subtitle,
                    dateString = dateString,
                    items = items,
                    totalAmount = totalAmount,
                    purchaserLabel = purchaserLabel,
                    approverLabel = approverLabel
                )
            }
        }

        pdfDocument.finishPage(page)
        return pdfDocument
    }

    private fun drawSingleVoucherOnCanvas(
        canvas: android.graphics.Canvas,
        startY: Float,
        centerName: String,
        subtitle: String,
        dateString: String,
        items: List<BillItem>,
        totalAmount: Double,
        purchaserLabel: String,
        approverLabel: String = ""
    ) {
        canvas.save()
        // Position at X=15pt, Y=startY + 423pt and rotate -90 degrees counter-clockwise
        // This maps Canvas Y to Page X (595pt width) and Canvas X to Page Y (421pt half-page height)
        canvas.translate(15f, startY + 423f)
        canvas.rotate(-90f)

        val localStartY = 0f

        // Table horizontal bounds (Canvas X axis, maps to Page Y, max 421f height)
        val tableLeft = 10f
        val tableRight = 410f
        val tableWidth = tableRight - tableLeft // 400f
        val bannerCenterX = tableLeft + (tableWidth / 2f) // 210f

        // 1. Header Box (Ink-Saving B&W Outline: White background with sharp black border)
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

        // Header Title (Pure Black)
        val titleTextPaint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = 18f
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(centerName, bannerCenterX, localStartY + 24f, titleTextPaint)

        // Subtitle (Pure Black)
        val subtitleTextPaint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = 11f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(subtitle, bannerCenterX, localStartY + 38f, subtitleTextPaint)

        // Header Separator Line
        val headerLinePaint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            strokeWidth = 1.5f
        }
        canvas.drawLine(tableLeft, localStartY + 48f, tableRight, localStartY + 48f, headerLinePaint)

        // 2. Metadata Row (Date - Pure Black, Bold)
        val metaPaintRight = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = 11.5f
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }
        val bnDate = BengaliUtils.toBengaliDigits(dateString)
        canvas.drawText("তারিখ : $bnDate", tableRight, localStartY + 66f, metaPaintRight)

        // 3. TABLE GRID CONFIG
        val tableTop = localStartY + 76f
        val colWidths = floatArrayOf(45f, 168f, 62f, 48f, 77f) // Total width = 400f

        // Table Header Box (White background with sharp Black Border)
        val headerHeight = 24f
        val tableHeaderRect = RectF(tableLeft, tableTop, tableRight, tableTop + headerHeight)
        canvas.drawRect(tableHeaderRect, headerBgPaint)
        canvas.drawRect(tableHeaderRect, headerBoxBorderPaint)

        // Table Header Text (Pure Black, Bold)
        val headerTextPaint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = 11f
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val colTitles = arrayOf("ক্র. নং", "খাবারের নাম / বিবরণ", "পরিমাণ", "দর", "টাকা")
        var currentX = tableLeft
        for (i in 0 until 5) {
            val colCenterX = currentX + colWidths[i] / 2f
            canvas.drawText(colTitles[i], colCenterX, tableTop + 16f, headerTextPaint)
            currentX += colWidths[i]
        }

        // Table Rows
        val validItems = items.filter { it.name.isNotBlank() || it.amount > 0 }
        val totalRows = maxOf(14, validItems.size.coerceAtMost(18))
        val gridTop = tableTop + headerHeight // 100f
        val gridBottom = 480f
        val totalGridHeight = gridBottom - gridTop // 380f
        val rowHeight = totalGridHeight / totalRows
        val itemFontSize = if (totalRows > 14) 11f else 12.5f
        val textYOffset = rowHeight * 0.68f

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

        var rowY = gridTop
        for (r in 0 until totalRows) {
            val slNo = BengaliUtils.toBengaliDigits(String.format("%02d", r + 1))
            
            // Draw Sl No
            itemBoldPaint.textAlign = Paint.Align.CENTER
            canvas.drawText(slNo, tableLeft + colWidths[0] / 2f, rowY + textYOffset, itemBoldPaint)

            if (r < validItems.size) {
                val item = validItems[r]
                // Name
                itemBoldPaint.textAlign = Paint.Align.LEFT
                val origTextSize = itemBoldPaint.textSize
                if (item.name.length > 35) {
                    itemBoldPaint.textSize = origTextSize * 0.75f
                } else if (item.name.length > 20) {
                    itemBoldPaint.textSize = origTextSize * 0.88f
                }
                canvas.drawText(item.name, tableLeft + colWidths[0] + 6f, rowY + textYOffset, itemBoldPaint)
                itemBoldPaint.textSize = origTextSize

                // Qty
                itemTextPaint.textAlign = Paint.Align.CENTER
                val bnQty = BengaliUtils.toBengaliDigits(item.quantity)
                canvas.drawText(bnQty, tableLeft + colWidths[0] + colWidths[1] + colWidths[2] / 2f, rowY + textYOffset, itemTextPaint)

                // Rate
                val bnRate = if (item.rate == "0" || item.rate.isBlank()) "" else BengaliUtils.toBengaliDigits(item.rate)
                canvas.drawText(bnRate, tableLeft + colWidths[0] + colWidths[1] + colWidths[2] + colWidths[3] / 2f, rowY + textYOffset, itemTextPaint)

                // Amount
                itemBoldPaint.textAlign = Paint.Align.RIGHT
                val bnAmount = if (item.amount <= 0) "—" else "${BengaliUtils.toBengaliDigits(DecimalFormat("#,##0").format(item.amount))}/-"
                canvas.drawText(bnAmount, tableRight - 6f, rowY + textYOffset, itemBoldPaint)
            }

            // Row Dashed Bottom Line
            canvas.drawLine(tableLeft, rowY + rowHeight, tableRight, rowY + rowHeight, rowDashPaint)
            rowY += rowHeight
        }

        // Table Total Row
        val totalRowY = rowY
        canvas.drawLine(tableLeft, totalRowY, tableRight, totalRowY, gridBorderPaint)

        // Total Label
        val totalLabelPaint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = 12f
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("মোট —", tableLeft + colWidths[0] + colWidths[1] + colWidths[2] + colWidths[3] - 6f, totalRowY + 16f, totalLabelPaint)

        // Total Value
        val totalValPaint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = 12.5f
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }
        val bnTotal = if (totalAmount <= 0) "0/-" else "${BengaliUtils.formatBengaliCurrency(totalAmount)}/-"
        canvas.drawText(bnTotal, tableRight - 6f, totalRowY + 16f, totalValPaint)

        val tableBottomY = totalRowY + 22f
        canvas.drawLine(tableLeft, tableBottomY, tableRight, tableBottomY, gridBorderPaint)

        // Outer Table Rect Border & Vertical Column Grid Lines
        canvas.drawRect(tableLeft, tableTop, tableRight, tableBottomY, gridBorderPaint)
        
        var lineX = tableLeft
        for (i in 0 until 4) {
            lineX += colWidths[i]
            canvas.drawLine(lineX, tableTop, lineX, tableBottomY, gridBorderPaint)
        }

        // 4. FOOTER BELOW TABLE
        val sigLineY = 550f
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

        // Single Signature Line (Left side)
        canvas.drawLine(tableLeft, sigLineY, tableLeft + sigLineWidth, sigLineY, linePaint)
        val labelText = purchaserLabel.ifBlank { "ক্রয়কারীর স্বাক্ষর" }
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

    /**
     * Escapes text that will be inserted into HTML, so that characters typed by the user
     * (e.g. "<", ">", "&", quotes in an item name or center name) can never break the
     * voucher's layout or be interpreted as markup.
     */
    private fun escapeHtml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }

    fun generateHtmlVoucher(
        centerName: String,
        subtitle: String,
        dateString: String,
        items: List<BillItem>,
        totalAmount: Double,
        purchaserLabel: String,
        approverLabel: String,
        position: PrintPosition = PrintPosition.TOP
    ): String {
        val safeCenterName = escapeHtml(centerName)
        val safeSubtitle = escapeHtml(subtitle)
        val safePurchaserLabel = escapeHtml(purchaserLabel)
        val bengaliDate = BengaliUtils.toBengaliDigits(dateString)
        val bengaliTotal = if (totalAmount <= 0) "0/-" else "${BengaliUtils.formatBengaliCurrency(totalAmount)}/-"

        val validItems = items.filter { it.name.isNotBlank() || it.amount > 0 }
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

        val memoCardHtml = """
            <div class="memo-half">
                <div class="memo-card">
                    <div class="header-banner">
                        <h1>$safeCenterName</h1>
                        <p>$safeSubtitle</p>
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
                                    <th style="width: 43%;">খাবারের নাম / বিবরণ</th>
                                    <th style="width: 15%;">পরিমাণ</th>
                                    <th style="width: 12%;">দর</th>
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
                                ${safePurchaserLabel.ifBlank { "ক্রয়কারীর স্বাক্ষর" }}
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        """.trimIndent()

        val emptyHalfHtml = """
            <div class="empty-half"></div>
        """.trimIndent()

        val contentBodyHtml = when (position) {
            PrintPosition.TOP -> """
                $memoCardHtml
                $emptyHalfHtml
            """.trimIndent()
            PrintPosition.BOTTOM -> """
                $emptyHalfHtml
                $memoCardHtml
            """.trimIndent()
            PrintPosition.BOTH -> """
                $memoCardHtml
                $memoCardHtml
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
                    .dashed-divider {
                        border-bottom: 1.2px dashed #000000;
                        margin: 0 6px 8px 6px;
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
                        padding: $cellPaddingCss;
                        font-size: $fontSizeCss;
                        color: #000000;
                        height: $rowHeightCss;
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
                    .words-row {
                        margin-bottom: 16px;
                        font-weight: 500;
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
