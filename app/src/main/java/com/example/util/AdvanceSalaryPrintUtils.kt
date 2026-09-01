package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
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
import com.example.ui.AdvanceSalaryFormState
import java.io.File
import java.io.FileOutputStream

object AdvanceSalaryPrintUtils {

    fun printAdvanceSalary(
        context: Context,
        topState: AdvanceSalaryFormState,
        bottomState: AdvanceSalaryFormState? = null,
        position: PrintPosition = PrintPosition.TOP
    ) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
        val dateLabel = topState.dateString.replace("/", "-").ifBlank { "Advance" }
        val empLabel = topState.applicantName.replace(" ", "_").ifBlank { "Employee" }
        val jobName = "Advance_Salary_${empLabel}_$dateLabel"

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
                val pdfDocument = createAdvanceSalaryPdfDocument(
                    topState = topState,
                    bottomState = bottomState ?: topState,
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

    fun shareAdvanceSalaryPdf(
        context: Context,
        topState: AdvanceSalaryFormState,
        bottomState: AdvanceSalaryFormState? = null,
        position: PrintPosition = PrintPosition.TOP
    ) {
        try {
            val pdfDocument = createAdvanceSalaryPdfDocument(
                topState = topState,
                bottomState = bottomState ?: topState,
                position = position
            )

            val dateLabel = topState.dateString.replace("/", "-").ifBlank { "Advance" }
            val empLabel = topState.applicantName.replace(" ", "_").ifBlank { "Employee" }
            val cacheDir = File(context.cacheDir, "advance_salaries").apply { mkdirs() }
            val pdfFile = File(cacheDir, "Advance_Salary_${empLabel}_$dateLabel.pdf")
            if (pdfFile.exists()) pdfFile.delete()

            FileOutputStream(pdfFile).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()

            val authority = "${context.packageName}.fileprovider"
            val uri: Uri = FileProvider.getUriForFile(context, authority, pdfFile)

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Advance Salary Application - ${topState.applicantName}")
                putExtra(Intent.EXTRA_TEXT, "Advance Salary Application for ${topState.applicantName} (${topState.dateString})")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(intent, "Share Advance Salary PDF"))
        } catch (e: Exception) {
            Toast.makeText(context, "PDF creation failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    fun createAdvanceSalaryPdfDocument(
        topState: AdvanceSalaryFormState?,
        bottomState: AdvanceSalaryFormState?,
        position: PrintPosition = PrintPosition.TOP
    ): PdfDocument {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Standard A4 (72 DPI)
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val bgPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, 595f, 842f, bgPaint)

        val resolvedTop = topState ?: bottomState
        val resolvedBottom = bottomState ?: topState

        when (position) {
            PrintPosition.TOP -> {
                resolvedTop?.let {
                    drawSingleVoucherOnCanvas(canvas = canvas, startY = 0f, state = it)
                }
            }
            PrintPosition.BOTTOM -> {
                resolvedBottom?.let {
                    drawSingleVoucherOnCanvas(canvas = canvas, startY = 421f, state = it)
                }
            }
            PrintPosition.BOTH -> {
                resolvedTop?.let {
                    drawSingleVoucherOnCanvas(canvas = canvas, startY = 0f, state = it)
                }
                resolvedBottom?.let {
                    drawSingleVoucherOnCanvas(canvas = canvas, startY = 421f, state = it)
                }

                // Perforation line between top and bottom
                val dashPaint = Paint().apply {
                    color = Color.rgb(180, 180, 180)
                    strokeWidth = 0.8f
                    style = Paint.Style.STROKE
                    pathEffect = DashPathEffect(floatArrayOf(6f, 4f), 0f)
                }
                canvas.drawLine(15f, 421f, 580f, 421f, dashPaint)
            }
        }

        pdfDocument.finishPage(page)
        return pdfDocument
    }

    /**
     * Matches the exact page size, dimension and orientation of the Food Bill Cash Memo:
     * - A4 half-page landscape voucher block rotated -90 deg
     * - Spans full width of the A4 page (local Y: 18f to ~550f across 595 pt page width)
     * - tableLeft = 12f, tableRight = 408f (width = 396 pt across 421 pt half-page height)
     * - Top Box Header with Company Name & Subtitle
     * - Full-width separator underline
     * - Document Title Banner & Ref No / Date row
     * - Structured Employee Details & Advance Repayment tables in English
     * - Undertaking declaration and 3 signature blocks (Applicant, Accounts, Authorized)
     */
    private fun drawSingleVoucherOnCanvas(
        canvas: android.graphics.Canvas,
        startY: Float,
        state: AdvanceSalaryFormState
    ) {
        canvas.save()
        canvas.translate(15f, startY + 421f)
        canvas.rotate(-90f)

        val localStartY = 16f
        val tableLeft = 12f
        val tableRight = 408f
        val tableWidth = tableRight - tableLeft
        val bannerCenterX = tableLeft + (tableWidth / 2f)

        // 1. Header Box (Top Rectangular Frame)
        val headerHeight = if (state.companySubtitle.isNotBlank()) 56f else 46f
        val headerRect = RectF(tableLeft, localStartY, tableRight, localStartY + headerHeight)
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

        // Header Title (Company Name)
        val titleText = state.companyName.ifBlank { "Al-Baraka General Store" }
        val titleTextPaint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(titleText, bannerCenterX, localStartY + 26f, titleTextPaint)

        // Subtitle (if available)
        if (state.companySubtitle.isNotBlank()) {
            val subtitleTextPaint = Paint().apply {
                isAntiAlias = true
                color = Color.rgb(50, 50, 50)
                textSize = 10.5f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(state.companySubtitle, bannerCenterX, localStartY + 45f, subtitleTextPaint)
        }

        // Header Separator Line
        val headerLinePaint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            strokeWidth = 1.5f
        }
        val lineY = localStartY + headerHeight + 6f
        canvas.drawLine(tableLeft, lineY, tableRight, lineY, headerLinePaint)

        var curY = lineY + 10f

        // Document Banner Title: ADVANCE SALARY REQUISITION VOUCHER
        val bannerH = 26f
        val bannerRect = RectF(tableLeft + 30f, curY, tableRight - 30f, curY + bannerH)
        val bannerPaint = Paint().apply {
            isAntiAlias = true
            color = Color.rgb(240, 240, 240)
            style = Paint.Style.FILL
        }
        val bannerBorder = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        canvas.drawRect(bannerRect, bannerPaint)
        canvas.drawRect(bannerRect, bannerBorder)

        val bannerTextPaint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("ADVANCE SALARY REQUISITION VOUCHER", bannerCenterX, curY + 17.5f, bannerTextPaint)

        curY += bannerH + 10f

        // Metadata Row: Date
        val metaPaintRight = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }

        canvas.drawText("Date: ${state.dateString}", tableRight - 2f, curY + 12f, metaPaintRight)

        curY += 22f

        // Paints for sections
        val labelPaint = Paint().apply {
            isAntiAlias = true
            color = Color.rgb(30, 30, 30)
            textSize = 10.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val valuePaint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = 10.5f
            typeface = Typeface.DEFAULT
        }
        val valueBoldPaint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val sectionBgPaint = Paint().apply {
            color = Color.rgb(245, 245, 245)
            style = Paint.Style.FILL
        }
        val borderThinPaint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        val secTitlePaint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        // Section 1: Employee Information Table
        val hasMobile = state.contactNumber.isNotBlank()
        val hasSalary = state.monthlySalary > 0

        val empBoxH = 82f
        val empRect = RectF(tableLeft, curY, tableRight, curY + empBoxH)
        canvas.drawRect(empRect, borderThinPaint)

        // Section Title Header Bar
        val secHeaderRect = RectF(tableLeft, curY, tableRight, curY + 18f)
        canvas.drawRect(secHeaderRect, sectionBgPaint)
        canvas.drawLine(tableLeft, curY + 18f, tableRight, curY + 18f, borderThinPaint)
        canvas.drawText("1. APPLICANT / EMPLOYEE DETAILS", tableLeft + 6f, curY + 13f, secTitlePaint)

        var empRowY = curY + 36f

        // Row 1: Employee Name & Designation
        canvas.drawText("Name:", tableLeft + 6f, empRowY, labelPaint)
        canvas.drawText(state.applicantName.ifBlank { "N/A" }, tableLeft + 50f, empRowY, valueBoldPaint)

        canvas.drawText("Designation:", tableLeft + 215f, empRowY, labelPaint)
        canvas.drawText(state.designation.ifBlank { "Staff" }, tableLeft + 290f, empRowY, valueBoldPaint)

        empRowY += 28f

        // Row 2: Mobile No & Basic Salary
        if (hasMobile) {
            canvas.drawText("Mobile No:", tableLeft + 6f, empRowY, labelPaint)
            canvas.drawText(state.contactNumber, tableLeft + 68f, empRowY, valuePaint)
        } else {
            canvas.drawText("Status:", tableLeft + 6f, empRowY, labelPaint)
            canvas.drawText("Permanent Staff", tableLeft + 52f, empRowY, valuePaint)
        }

        if (hasSalary) {
            canvas.drawText("Basic Salary:", tableLeft + 215f, empRowY, labelPaint)
            canvas.drawText("Tk. ${EnglishUtils.formatEnglishCurrency(state.monthlySalary)}/-", tableLeft + 290f, empRowY, valuePaint)
        } else {
            canvas.drawText("Branch:", tableLeft + 215f, empRowY, labelPaint)
            canvas.drawText("Main Branch", tableLeft + 290f, empRowY, valuePaint)
        }

        curY += empBoxH + 14f

        // Section 2: Advance & Repayment Details Table
        val advBoxH = 176f
        val advRect = RectF(tableLeft, curY, tableRight, curY + advBoxH)
        canvas.drawRect(advRect, borderThinPaint)

        val sec2HeaderRect = RectF(tableLeft, curY, tableRight, curY + 18f)
        canvas.drawRect(sec2HeaderRect, sectionBgPaint)
        canvas.drawLine(tableLeft, curY + 18f, tableRight, curY + 18f, borderThinPaint)
        canvas.drawText("2. ADVANCE & REPAYMENT TERMS", tableLeft + 6f, curY + 13f, secTitlePaint)

        var advRowY = curY + 36f

        // Advance Amount
        canvas.drawText("Advance Amount:", tableLeft + 6f, advRowY, labelPaint)
        val amountStr = "Tk. ${EnglishUtils.formatEnglishCurrency(state.advanceAmount)}/-"
        canvas.drawText(amountStr, tableLeft + 105f, advRowY, valueBoldPaint)

        advRowY += 26f

        // Amount in Words
        canvas.drawText("In Words:", tableLeft + 6f, advRowY, labelPaint)
        val inWords = state.advanceAmountInWords.ifBlank {
            EnglishUtils.amountToEnglishWords(state.advanceAmount)
        }
        val wrapWords = CanvasTextUtils.wrapText(inWords, valuePaint, tableWidth - 75f)
        if (wrapWords.isNotEmpty()) {
            canvas.drawText(wrapWords[0], tableLeft + 65f, advRowY, valuePaint)
            if (wrapWords.size > 1) {
                advRowY += 15f
                canvas.drawText(wrapWords[1], tableLeft + 65f, advRowY, valuePaint)
            }
        }

        advRowY += 26f

        // Reason
        canvas.drawText("Reason:", tableLeft + 6f, advRowY, labelPaint)
        val reasonText = state.reason.ifBlank { "Personal Emergency" }
        canvas.drawText(reasonText, tableLeft + 65f, advRowY, valuePaint)

        advRowY += 26f

        // Repayment
        canvas.drawText("Repayment:", tableLeft + 6f, advRowY, labelPaint)
        val repayStr = if (state.repaymentType == "installments") {
            "Monthly Installments: ${state.installmentCount} Months (Tk. ${EnglishUtils.formatEnglishCurrency(state.installmentAmountPerMonth)}/- per month)"
        } else {
            "One-time full deduction from salary"
        }
        canvas.drawText(repayStr, tableLeft + 78f, advRowY, valueBoldPaint)

        advRowY += 26f

        // Deduction Start & Previous Pending
        val dedStart = state.deductionStartMonth.ifBlank { "Next Month" }
        canvas.drawText("Deduction Starts: $dedStart", tableLeft + 6f, advRowY, valuePaint)
        if (state.previousAdvancePending > 0) {
            canvas.drawText("Previous Due: Tk. ${EnglishUtils.formatEnglishCurrency(state.previousAdvancePending)}/-", tableLeft + 215f, advRowY, valuePaint)
        }

        curY += advBoxH + 14f

        // Section 3: Declaration / Undertaking
        val declBoxH = 38f
        val declRect = RectF(tableLeft, curY, tableRight, curY + declBoxH)
        canvas.drawRect(declRect, borderThinPaint)

        val declPaint = Paint().apply {
            isAntiAlias = true
            color = Color.rgb(60, 60, 60)
            textSize = 8f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        }
        canvas.drawText("Undertaking: I hereby request the above advance against my salary and authorize deduction from my", tableLeft + 6f, curY + 15f, declPaint)
        canvas.drawText("monthly salary as stated above. I agree to abide by the company rules, terms & repayment schedule.", tableLeft + 6f, curY + 29f, declPaint)

        curY += declBoxH + 46f

        // Section 4: Signatures Row (2 Signatures: Applicant and Authorized)
        if (state.showSignatures) {
            val sigLinePaint = Paint().apply {
                isAntiAlias = true
                color = Color.BLACK
                strokeWidth = 1f
            }
            val sigTextPaint = Paint().apply {
                isAntiAlias = true
                color = Color.BLACK
                textSize = 10f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }

            val sig1Center = tableLeft + 80f
            val sig2Center = tableRight - 80f

            val sigLineY = curY + 10f

            // 1. Applicant Signature (Left)
            canvas.drawLine(tableLeft + 15f, sigLineY, tableLeft + 145f, sigLineY, sigLinePaint)
            canvas.drawText("Applicant's Signature", sig1Center, sigLineY + 14f, sigTextPaint)

            // 2. Authorized / Approved By (Right)
            canvas.drawLine(tableRight - 145f, sigLineY, tableRight - 15f, sigLineY, sigLinePaint)
            canvas.drawText("Authorized Signature", sig2Center, sigLineY + 14f, sigTextPaint)
        }

        canvas.restore()
    }
}
