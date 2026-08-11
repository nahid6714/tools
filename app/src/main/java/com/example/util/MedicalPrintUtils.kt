package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintManager
import androidx.core.content.FileProvider
import com.example.data.MedicalRecordEntity
import com.example.ui.AnalysisSummary
import java.io.File
import java.io.FileOutputStream

object MedicalPrintUtils {

    private fun formatDateShort(dateStr: String): String {
        return try {
            if (dateStr.contains("-")) {
                val parts = dateStr.split("-")
                if (parts.size == 3) {
                    val year = parts[0].takeLast(2)
                    val month = parts[1].padStart(2, '0')
                    val day = parts[2].padStart(2, '0')
                    "$day/$month/$year"
                } else dateStr
            } else dateStr
        } catch (e: Exception) {
            dateStr
        }
    }

    fun printDailyReport(
        context: Context,
        dateStr: String,
        records: List<MedicalRecordEntity>
    ) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return
        val jobName = "Medical_Daily_Report_$dateStr"

        val printAdapter = object : PrintDocumentAdapter() {
            private var pdfDocument: PdfDocument? = null

            override fun onLayout(
                oldAttributes: PrintAttributes?,
                newAttributes: PrintAttributes?,
                cancellationSignal: android.os.CancellationSignal?,
                callback: LayoutResultCallback?,
                extras: android.os.Bundle?
            ) {
                if (cancellationSignal?.isCanceled == true) {
                    callback?.onLayoutCancelled()
                    return
                }
                val pageCount = calculatePageCount(records.size)
                val builder = android.print.PrintDocumentInfo.Builder(jobName)
                    .setContentType(android.print.PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(pageCount)
                callback?.onLayoutFinished(builder.build(), true)
            }

            override fun onWrite(
                pages: Array<out android.print.PageRange>?,
                destination: android.os.ParcelFileDescriptor?,
                cancellationSignal: android.os.CancellationSignal?,
                callback: WriteResultCallback?
            ) {
                pdfDocument = createDailyReportPdfDocument(dateStr, records)
                try {
                    FileOutputStream(destination?.fileDescriptor).use { out ->
                        pdfDocument?.writeTo(out)
                    }
                    callback?.onWriteFinished(arrayOf(android.print.PageRange.ALL_PAGES))
                } catch (e: Exception) {
                    callback?.onWriteFailed(e.message)
                } finally {
                    pdfDocument?.close()
                }
            }
        }

        printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())
    }

    fun shareDailyReportAsImage(
        context: Context,
        dateStr: String,
        records: List<MedicalRecordEntity>
    ) {
        val bitmap = renderDailyReportBitmap(dateStr, records)
        shareBitmapImage(context, bitmap, "Medical_Report_$dateStr")
    }

    fun shareDailyReportAsPdf(
        context: Context,
        dateStr: String,
        records: List<MedicalRecordEntity>
    ) {
        val pdfDocument = createDailyReportPdfDocument(dateStr, records)
        try {
            val cachePath = File(context.cacheBufferDir(), "documents")
            cachePath.mkdirs()
            val file = File(cachePath, "Medical_Report_$dateStr.pdf")
            FileOutputStream(file).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()

            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Medical PDF Report"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun shareAnalysisReportAsImage(
        context: Context,
        summary: AnalysisSummary
    ) {
        val bitmap = renderAnalysisReportBitmap(summary)
        shareBitmapImage(context, bitmap, "Medical_Analysis_Report")
    }

    private fun calculatePageCount(totalRecords: Int): Int {
        val rowsPerPage = 16
        if (totalRecords == 0) return 1
        return (totalRecords + rowsPerPage - 1) / rowsPerPage
    }

    private fun createDailyReportPdfDocument(
        dateStr: String,
        records: List<MedicalRecordEntity>
    ): PdfDocument {
        val pdfDocument = PdfDocument()
        val width = 1080
        val height = 1920
        val rowsPerPage = 16
        val pageCount = calculatePageCount(records.size)

        for (pageIdx in 0 until pageCount) {
            val pageInfo = PdfDocument.PageInfo.Builder(width, height, pageIdx + 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            canvas.drawColor(Color.WHITE)

            val startIdx = pageIdx * rowsPerPage
            val endIdx = minOf(records.size, (pageIdx + 1) * rowsPerPage)
            val pageRecords = if (startIdx < records.size) records.subList(startIdx, endIdx) else emptyList()

            drawSinglePageDailyReport(canvas, dateStr, pageRecords, startIdx, width.toFloat(), height.toFloat())
            pdfDocument.finishPage(page)
        }
        return pdfDocument
    }

    fun renderDailyReportBitmap(
        dateStr: String,
        records: List<MedicalRecordEntity>
    ): Bitmap {
        val width = 1080
        val baseHeight = 1920
        val rowHeight = 95
        val tableTop = 240
        val minHeight = tableTop + (records.size + 1) * rowHeight + 100
        val totalHeight = maxOf(baseHeight, minHeight)

        val bitmap = Bitmap.createBitmap(width, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        drawSinglePageDailyReport(canvas, dateStr, records, 0, width.toFloat(), totalHeight.toFloat())
        return bitmap
    }

    fun renderAnalysisReportBitmap(summary: AnalysisSummary): Bitmap {
        val width = 850
        val totalHeight = maxOf(700, 220 + (summary.dateBreakdown.size + summary.codeBreakdown.size + 5) * 45 + 120)

        val bitmap = Bitmap.createBitmap(width, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        drawAnalysisReportOnCanvas(canvas, summary, width.toFloat(), totalHeight.toFloat())
        return bitmap
    }

    private fun drawSinglePageDailyReport(
        canvas: Canvas,
        dateStr: String,
        records: List<MedicalRecordEntity>,
        startIndexOffset: Int,
        width: Float,
        height: Float
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Date Header (Top Right)
        val formattedDate = formatDateShort(dateStr)
        paint.color = Color.BLACK
        paint.textSize = 46f
        paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("তারিখ: $formattedDate", width - 60f, 130f, paint)

        // Top Horizontal Divider Line
        paint.textAlign = Paint.Align.LEFT
        paint.strokeWidth = 3.5f
        canvas.drawLine(60f, 175f, width - 60f, 175f, paint)

        // Table Coordinates & Sizes
        val tableLeft = 60f
        val tableRight = width - 60f
        val tableTop = 240f
        val headerHeight = 100f
        val rowHeight = 95f

        val col1Width = 190f
        val col2Width = 385f
        val col3Width = 385f

        val col1X = tableLeft + col1Width                   // First vertical grid line
        val col2X = tableLeft + col1Width + col2Width        // Second vertical grid line

        val centerCol1 = tableLeft + col1Width / 2f
        val centerCol2 = tableLeft + col1Width + col2Width / 2f
        val centerCol3 = tableLeft + col1Width + col2Width + col3Width / 2f

        // Table Headers Text
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 42f
        paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)

        val headerTextY = tableTop + 62f
        canvas.drawText("ক্রমিক", centerCol1, headerTextY, paint)
        canvas.drawText("ID", centerCol2, headerTextY, paint)
        canvas.drawText("কোড", centerCol3, headerTextY, paint)

        // Table Rows Text
        paint.textSize = 38f
        paint.typeface = android.graphics.Typeface.DEFAULT

        records.forEachIndexed { index, record ->
            val rowTopY = tableTop + headerHeight + index * rowHeight
            val textY = rowTopY + 60f
            val serialNum = (startIndexOffset + index + 1).toString()

            canvas.drawText(serialNum, centerCol1, textY, paint)
            canvas.drawText(record.patientId, centerCol2, textY, paint)
            canvas.drawText(record.code, centerCol3, textY, paint)
        }

        // Draw Table Grid Lines (Outer Frame & Cell Dividers)
        val recordCount = records.size
        val tableBottom = tableTop + headerHeight + recordCount * rowHeight

        val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
        }

        // Outer Rectangle
        canvas.drawRect(tableLeft, tableTop, tableRight, tableBottom, gridPaint)

        // Vertical Lines
        canvas.drawLine(col1X, tableTop, col1X, tableBottom, gridPaint)
        canvas.drawLine(col2X, tableTop, col2X, tableBottom, gridPaint)

        // Horizontal Line under Header
        canvas.drawLine(tableLeft, tableTop + headerHeight, tableRight, tableTop + headerHeight, gridPaint)

        // Horizontal Lines under each Row
        for (i in 0 until recordCount) {
            val lineY = tableTop + headerHeight + (i + 1) * rowHeight
            canvas.drawLine(tableLeft, lineY, tableRight, lineY, gridPaint)
        }
    }

    private fun drawAnalysisReportOnCanvas(
        canvas: Canvas,
        summary: AnalysisSummary,
        width: Float,
        height: Float
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Header Background
        paint.color = Color.parseColor("#1A237E")
        canvas.drawRect(0f, 0f, width, 120f, paint)

        // Title
        paint.color = Color.WHITE
        paint.textSize = 24f
        paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        canvas.drawText("মেডিকেল ওয়ার্ক ফিল্টার ও এনালাইসিস রিপোর্ট", 30f, 48f, paint)

        paint.textSize = 14f
        paint.typeface = android.graphics.Typeface.DEFAULT
        canvas.drawText("ফিল্টার: ${summary.filterDescriptionBn}", 30f, 82f, paint)
        canvas.drawText("মোট কাজ: ${BengaliUtils.toBengaliDigits(summary.totalCount.toString())}টি", 30f, 104f, paint)

        var currentY = 160f

        // Date-wise breakdown
        paint.color = Color.parseColor("#1A237E")
        paint.textSize = 17f
        paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        canvas.drawText("১. তারিখ ভিত্তিক কাজ (Date Breakdown)", 30f, currentY, paint)

        currentY += 30f
        paint.color = Color.parseColor("#E8EAF6")
        canvas.drawRect(30f, currentY - 22f, width - 30f, currentY + 12f, paint)

        paint.color = Color.parseColor("#1A237E")
        paint.textSize = 14f
        canvas.drawText("তারিখ (Date)", 50f, currentY, paint)
        canvas.drawText("কাজের সংখ্যা (Work Count)", 450f, currentY, paint)

        currentY += 35f
        paint.typeface = android.graphics.Typeface.DEFAULT

        summary.dateBreakdown.forEachIndexed { index, item ->
            if (index % 2 == 1) {
                val bgPaint = Paint().apply { color = Color.parseColor("#FAFAFA") }
                canvas.drawRect(30f, currentY - 20f, width - 30f, currentY + 12f, bgPaint)
            }
            paint.color = Color.parseColor("#333333")
            canvas.drawText(item.formattedDateBn + " (${item.date})", 50f, currentY, paint)
            val countBn = BengaliUtils.toBengaliDigits(item.count.toString()) + " টি"
            canvas.drawText(countBn, 450f, currentY, paint)

            currentY += 32f
        }

        // Code-wise breakdown
        currentY += 25f
        paint.color = Color.parseColor("#1A237E")
        paint.textSize = 17f
        paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        canvas.drawText("২. কোড ভিত্তিক কাজ (Code Breakdown)", 30f, currentY, paint)

        currentY += 30f
        paint.color = Color.parseColor("#E8EAF6")
        canvas.drawRect(30f, currentY - 22f, width - 30f, currentY + 12f, paint)

        paint.color = Color.parseColor("#1A237E")
        paint.textSize = 14f
        canvas.drawText("কোড (Code)", 50f, currentY, paint)
        canvas.drawText("কাজের সংখ্যা (Work Count)", 450f, currentY, paint)

        currentY += 35f
        paint.typeface = android.graphics.Typeface.DEFAULT

        summary.codeBreakdown.forEachIndexed { index, item ->
            if (index % 2 == 1) {
                val bgPaint = Paint().apply { color = Color.parseColor("#FAFAFA") }
                canvas.drawRect(30f, currentY - 20f, width - 30f, currentY + 12f, bgPaint)
            }
            paint.color = Color.parseColor("#333333")
            canvas.drawText("${item.code} (${item.codeName})", 50f, currentY, paint)
            val countBn = BengaliUtils.toBengaliDigits(item.count.toString()) + " টি"
            canvas.drawText(countBn, 450f, currentY, paint)

            currentY += 32f
        }
    }

    private fun shareBitmapImage(context: Context, bitmap: Bitmap, filename: String) {
        try {
            val cachePath = File(context.cacheBufferDir(), "images")
            cachePath.mkdirs()
            val file = File(cachePath, "$filename.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Medical Report Image"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun Context.cacheBufferDir(): File {
        return externalCacheDir ?: cacheDir
    }
}

