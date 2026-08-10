package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintManager
import androidx.core.content.FileProvider
import com.example.data.MedicalRecordEntity
import com.example.ui.AnalysisSummary
import java.io.File
import java.io.FileOutputStream

object MedicalPrintUtils {

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
                val builder = android.print.PrintDocumentInfo.Builder(jobName)
                    .setContentType(android.print.PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(1)
                callback?.onLayoutFinished(builder.build(), true)
            }

            override fun onWrite(
                pages: Array<out android.print.PageRange>?,
                destination: android.os.ParcelFileDescriptor?,
                cancellationSignal: android.os.CancellationSignal?,
                callback: WriteResultCallback?
            ) {
                pdfDocument = PdfDocument()
                val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 at 72dpi
                val page = pdfDocument?.startPage(pageInfo)

                page?.canvas?.let { canvas ->
                    drawDailyReportOnCanvas(canvas, dateStr, records, 595f, 842f)
                }

                pdfDocument?.finishPage(page)

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

    fun shareAnalysisReportAsImage(
        context: Context,
        summary: AnalysisSummary
    ) {
        val bitmap = renderAnalysisReportBitmap(summary)
        shareBitmapImage(context, bitmap, "Medical_Analysis_Report")
    }

    fun renderDailyReportBitmap(
        dateStr: String,
        records: List<MedicalRecordEntity>
    ): Bitmap {
        val width = 800
        val headerHeight = 160
        val rowHeight = 44
        val totalHeight = maxOf(600, headerHeight + (records.size + 2) * rowHeight + 100)

        val bitmap = Bitmap.createBitmap(width, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        drawDailyReportOnCanvas(canvas, dateStr, records, width.toFloat(), totalHeight.toFloat())
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

    private fun drawDailyReportOnCanvas(
        canvas: Canvas,
        dateStr: String,
        records: List<MedicalRecordEntity>,
        width: Float,
        height: Float
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Header Background
        paint.color = Color.parseColor("#0D47A1")
        canvas.drawRect(0f, 0f, width, 110f, paint)

        // Title
        paint.color = Color.WHITE
        paint.textSize = 24f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("মেডিকেল ওয়ার্ক ডেইলি রিপোর্ট (Medical Daily Report)", 30f, 48f, paint)

        paint.textSize = 15f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("তারিখ: $dateStr | মোট কাজ: ${BengaliUtils.toBengaliDigits(records.size.toString())}টি", 30f, 85f, paint)

        // Table Headers
        var currentY = 145f
        paint.color = Color.parseColor("#E3F2FD")
        canvas.drawRect(30f, currentY - 25f, width - 30f, currentY + 15f, paint)

        paint.color = Color.parseColor("#0D47A1")
        paint.textSize = 15f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

        val col1X = 50f   // Sl
        val col2X = 160f  // Patient ID
        val col3X = 450f  // Code

        canvas.drawText("ক্রমিক (Sl)", col1X, currentYf(currentY), paint)
        canvas.drawText("পেশেন্ট আইডি (Patient ID)", col2X, currentYf(currentY), paint)
        canvas.drawText("কোড (Code)", col3X, currentYf(currentY), paint)

        // Divider
        paint.color = Color.parseColor("#BBDEFB")
        paint.strokeWidth = 2f
        canvas.drawLine(30f, currentY + 18f, width - 30f, currentY + 18f, paint)

        currentY += 45f
        paint.typeface = Typeface.DEFAULT
        paint.textSize = 14f

        records.forEachIndexed { index, record ->
            if (index % 2 == 1) {
                val bgPaint = Paint().apply { color = Color.parseColor("#F5F5F5") }
                canvas.drawRect(30f, currentY - 22f, width - 30f, currentY + 14f, bgPaint)
            }

            paint.color = Color.parseColor("#333333")
            val slBn = BengaliUtils.toBengaliDigits((index + 1).toString())
            canvas.drawText(slBn, col1X, currentY, paint)
            canvas.drawText(record.patientId, col2X, currentY, paint)
            canvas.drawText(record.code, col3X, currentY, paint)

            paint.color = Color.parseColor("#E0E0E0")
            paint.strokeWidth = 1f
            canvas.drawLine(30f, currentY + 16f, width - 30f, currentY + 16f, paint)

            currentY += 38f
        }

        // Summary Footer
        currentY += 15f
        paint.color = Color.parseColor("#0D47A1")
        paint.textSize = 15f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("সর্বমোট কাজের সংখ্যা: ${BengaliUtils.toBengaliDigits(records.size.toString())}টি", 30f, currentY, paint)
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
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("মেডিকেল ওয়ার্ক ফিল্টার ও এনালাইসিস রিপোর্ট", 30f, 48f, paint)

        paint.textSize = 14f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("ফিল্টার: ${summary.filterDescriptionBn}", 30f, 82f, paint)
        canvas.drawText("মোট কাজ: ${BengaliUtils.toBengaliDigits(summary.totalCount.toString())}টি", 30f, 104f, paint)

        var currentY = 160f

        // Date-wise breakdown
        paint.color = Color.parseColor("#1A237E")
        paint.textSize = 17f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("১. তারিখ ভিত্তিক কাজ (Date Breakdown)", 30f, currentY, paint)

        currentY += 30f
        paint.color = Color.parseColor("#E8EAF6")
        canvas.drawRect(30f, currentY - 22f, width - 30f, currentY + 12f, paint)

        paint.color = Color.parseColor("#1A237E")
        paint.textSize = 14f
        canvas.drawText("তারিখ (Date)", 50f, currentY, paint)
        canvas.drawText("কাজের সংখ্যা (Work Count)", 450f, currentY, paint)

        currentY += 35f
        paint.typeface = Typeface.DEFAULT

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
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("২. কোড ভিত্তিক কাজ (Code Breakdown)", 30f, currentY, paint)

        currentY += 30f
        paint.color = Color.parseColor("#E8EAF6")
        canvas.drawRect(30f, currentY - 22f, width - 30f, currentY + 12f, paint)

        paint.color = Color.parseColor("#1A237E")
        paint.textSize = 14f
        canvas.drawText("কোড (Code)", 50f, currentY, paint)
        canvas.drawText("কাজের সংখ্যা (Work Count)", 450f, currentY, paint)

        currentY += 35f
        paint.typeface = Typeface.DEFAULT

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

    private fun currentYf(y: Float): Float = y

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
            context.startActivity(Intent.createChooser(shareIntent, "Share Medical Report"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun Context.cacheBufferDir(): File {
        return externalCacheDir ?: cacheDir
    }
}
