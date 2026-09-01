package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.ui.AdvanceSalaryFormState
import java.io.File
import java.io.FileOutputStream

object AdvanceSalaryImageExporter {

    fun generateAdvanceSalaryBitmap(
        state: AdvanceSalaryFormState
    ): Bitmap {
        val imageWidth = 900
        val paddingX = 36f
        val paddingTop = 32f
        val paddingBottom = 45f
        val contentWidth = imageWidth - (paddingX * 2)

        val headerHeight = if (state.companySubtitle.isNotBlank()) 92f else 76f
        val headerGap = 12f
        val lineGap = 14f
        val bannerHeight = 42f
        val metaHeight = 36f
        
        val hasMobile = state.contactNumber.isNotBlank()
        val hasSalary = state.monthlySalary > 0
        val empBoxHeight = if (hasMobile && hasSalary) 130f else if (hasMobile || hasSalary) 100f else 95f
        val advBoxHeight = 190f
        val declBoxHeight = 65f
        val sigHeight = if (state.showSignatures) 85f else 15f

        val totalHeight = (paddingTop + headerHeight + headerGap + 2f + lineGap + bannerHeight + 12f +
                metaHeight + 12f + empBoxHeight + 16f + advBoxHeight + 16f + declBoxHeight + 20f +
                sigHeight + paddingBottom).toInt()

        val bitmap = Bitmap.createBitmap(imageWidth, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Pure white background
        canvas.drawColor(Color.WHITE)

        var curY = paddingTop
        val leftX = paddingX
        val rightX = imageWidth - paddingX
        val centerX = imageWidth / 2f

        // 1. Header Box (Top Rectangular Frame matching Food Bill style)
        val headerRect = RectF(leftX, curY, rightX, curY + headerHeight)
        val headerBoxBorderPaint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
        }
        canvas.drawRect(headerRect, headerBoxBorderPaint)

        // Company Name
        val titleText = state.companyName.ifBlank { "Al-Baraka General Store" }
        val titlePaint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = 34f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        if (state.companySubtitle.isNotBlank()) {
            canvas.drawText(titleText, centerX, curY + 44f, titlePaint)
            val subPaint = Paint().apply {
                isAntiAlias = true
                color = Color.rgb(50, 50, 50)
                textSize = 20f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(state.companySubtitle, centerX, curY + 76f, subPaint)
        } else {
            canvas.drawText(titleText, centerX, curY + 50f, titlePaint)
        }

        curY += headerHeight + headerGap

        // Full-width underline under header box
        val fullLinePaint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            strokeWidth = 2.2f
        }
        canvas.drawLine(leftX, curY, rightX, curY, fullLinePaint)

        curY += lineGap

        // 2. Banner: ADVANCE SALARY APPLICATION
        val bannerRect = RectF(leftX + 80f, curY, rightX - 80f, curY + bannerHeight)
        val bannerBg = Paint().apply {
            color = Color.rgb(245, 245, 245)
            style = Paint.Style.FILL
        }
        val bannerBorder = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 1.8f
        }
        canvas.drawRect(bannerRect, bannerBg)
        canvas.drawRect(bannerRect, bannerBorder)

        val bannerTextPaint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("ADVANCE SALARY REQUISITION VOUCHER", centerX, curY + 29f, bannerTextPaint)

        curY += bannerHeight + 14f

        // 3. Metadata Row (Date)
        val metaPaintRight = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }

        canvas.drawText("Date: ${state.dateString}", rightX - 4f, curY + 22f, metaPaintRight)

        curY += metaHeight + 10f

        // Paints for sections
        val boxBorder = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 1.8f
        }
        val secHeaderBg = Paint().apply {
            color = Color.rgb(240, 240, 240)
            style = Paint.Style.FILL
        }
        val secTitlePaint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = 19f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val labelPaint = Paint().apply {
            isAntiAlias = true
            color = Color.rgb(50, 50, 50)
            textSize = 19f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val valPaint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = 19f
            typeface = Typeface.DEFAULT
        }
        val valBoldPaint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        // 4. Section 1: Employee Details
        val empRect = RectF(leftX, curY, rightX, curY + empBoxHeight)
        canvas.drawRect(empRect, boxBorder)

        val empHeaderRect = RectF(leftX, curY, rightX, curY + 32f)
        canvas.drawRect(empHeaderRect, secHeaderBg)
        canvas.drawLine(leftX, curY + 32f, rightX, curY + 32f, boxBorder)
        canvas.drawText("1. EMPLOYEE DETAILS", leftX + 14f, curY + 23f, secTitlePaint)

        var empRowY = curY + 60f
        val rowH = 34f

        canvas.drawText("Name:", leftX + 14f, empRowY, labelPaint)
        canvas.drawText(state.applicantName.ifBlank { "N/A" }, leftX + 100f, empRowY, valBoldPaint)

        canvas.drawText("Designation:", centerX + 10f, empRowY, labelPaint)
        canvas.drawText(state.designation.ifBlank { "N/A" }, centerX + 150f, empRowY, valBoldPaint)

        empRowY += rowH

        if (hasMobile) {
            canvas.drawText("Mobile No:", leftX + 14f, empRowY, labelPaint)
            canvas.drawText(state.contactNumber, leftX + 130f, empRowY, valPaint)

            if (hasSalary) {
                canvas.drawText("Basic Salary:", centerX + 10f, empRowY, labelPaint)
                canvas.drawText("Tk. ${EnglishUtils.formatEnglishCurrency(state.monthlySalary)}/-", centerX + 150f, empRowY, valPaint)
            }
            empRowY += rowH
        } else if (hasSalary) {
            canvas.drawText("Basic Salary:", leftX + 14f, empRowY, labelPaint)
            canvas.drawText("Tk. ${EnglishUtils.formatEnglishCurrency(state.monthlySalary)}/-", leftX + 150f, empRowY, valPaint)
            empRowY += rowH
        }

        curY += empBoxHeight + 16f

        // 5. Section 2: Advance & Repayment Details
        val advRect = RectF(leftX, curY, rightX, curY + advBoxHeight)
        canvas.drawRect(advRect, boxBorder)

        val advHeaderRect = RectF(leftX, curY, rightX, curY + 32f)
        canvas.drawRect(advHeaderRect, secHeaderBg)
        canvas.drawLine(leftX, curY + 32f, rightX, curY + 32f, boxBorder)
        canvas.drawText("2. ADVANCE & REPAYMENT TERMS", leftX + 14f, curY + 23f, secTitlePaint)

        var advRowY = curY + 60f

        // Advance Amount
        canvas.drawText("Advance Amount:", leftX + 14f, advRowY, labelPaint)
        canvas.drawText("Tk. ${EnglishUtils.formatEnglishCurrency(state.advanceAmount)}/-", leftX + 200f, advRowY, valBoldPaint)

        advRowY += 30f

        // In Words
        canvas.drawText("In Words:", leftX + 14f, advRowY, labelPaint)
        val inWords = state.advanceAmountInWords.ifBlank { EnglishUtils.amountToEnglishWords(state.advanceAmount) }
        val wrapWords = CanvasTextUtils.wrapText(inWords, valPaint, contentWidth - 140f)
        if (wrapWords.isNotEmpty()) {
            canvas.drawText(wrapWords[0], leftX + 120f, advRowY, valPaint)
            if (wrapWords.size > 1) {
                advRowY += 24f
                canvas.drawText(wrapWords[1], leftX + 120f, advRowY, valPaint)
            }
        }

        advRowY += 30f

        // Reason
        canvas.drawText("Reason:", leftX + 14f, advRowY, labelPaint)
        val reasonText = state.reason.ifBlank { "Personal Emergency" }
        canvas.drawText(reasonText, leftX + 120f, advRowY, valPaint)

        advRowY += 30f

        // Repayment
        canvas.drawText("Repayment:", leftX + 14f, advRowY, labelPaint)
        val repayStr = if (state.repaymentType == "installments") {
            "Monthly Installments: ${state.installmentCount} Months (Tk. ${EnglishUtils.formatEnglishCurrency(state.installmentAmountPerMonth)}/- per month)"
        } else {
            "One-time full deduction from salary"
        }
        canvas.drawText(repayStr, leftX + 140f, advRowY, valBoldPaint)

        advRowY += 28f

        // Deduction Start & Due
        val dedStart = state.deductionStartMonth.ifBlank { "Next Month" }
        canvas.drawText("Deduction Starts: $dedStart", leftX + 14f, advRowY, valPaint)
        if (state.previousAdvancePending > 0) {
            canvas.drawText("Previous Due: Tk. ${EnglishUtils.formatEnglishCurrency(state.previousAdvancePending)}/-", centerX + 10f, advRowY, valPaint)
        }

        curY += advBoxHeight + 16f

        // 6. Section 3: Declaration / Undertaking
        val declRect = RectF(leftX, curY, rightX, curY + declBoxHeight)
        canvas.drawRect(declRect, boxBorder)

        val declPaint = Paint().apply {
            isAntiAlias = true
            color = Color.rgb(60, 60, 60)
            textSize = 15f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        }
        canvas.drawText("Undertaking: I hereby request the above advance against my salary and authorize deduction from my", leftX + 12f, curY + 26f, declPaint)
        canvas.drawText("monthly salary as stated above. I agree to abide by the company rules & policies.", leftX + 12f, curY + 48f, declPaint)

        curY += declBoxHeight + 45f

        // 7. Signatures Row (2 Signatures: Applicant and Authorized)
        if (state.showSignatures) {
            val sigLinePaint = Paint().apply {
                isAntiAlias = true
                color = Color.BLACK
                strokeWidth = 1.8f
            }
            val sigTextPaint = Paint().apply {
                isAntiAlias = true
                color = Color.BLACK
                textSize = 17f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }

            val sig1Center = leftX + 140f
            val sig2Center = rightX - 140f

            val sigLineY = curY + 45f

            // 1. Applicant's Signature (Left)
            canvas.drawLine(leftX + 25f, sigLineY, leftX + 255f, sigLineY, sigLinePaint)
            canvas.drawText("Applicant's Signature", sig1Center, sigLineY + 24f, sigTextPaint)

            // 2. Authorized Signature (Right)
            canvas.drawLine(rightX - 255f, sigLineY, rightX - 25f, sigLineY, sigLinePaint)
            canvas.drawText("Authorized Signature", sig2Center, sigLineY + 24f, sigTextPaint)
        }

        return bitmap
    }

    fun saveAdvanceSalaryImageToGallery(context: Context, state: AdvanceSalaryFormState): Boolean {
        return try {
            val bitmap = generateAdvanceSalaryBitmap(state)
            val empLabel = state.applicantName.replace(" ", "_").ifBlank { "Employee" }
            val dateLabel = state.dateString.replace("/", "-").ifBlank { "Advance" }
            val filename = "Advance_Salary_${empLabel}_$dateLabel.png"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/AdvanceSalary")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }

                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                    ?: return false

                resolver.openOutputStream(uri)?.use { stream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                }

                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
            } else {
                val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "AdvanceSalary")
                if (!dir.exists()) dir.mkdirs()
                val file = File(dir, filename)
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), arrayOf("image/png"), null)
            }

            Toast.makeText(context, "Image saved to gallery", Toast.LENGTH_SHORT).show()
            true
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to save image: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            false
        }
    }

    fun shareAdvanceSalaryImage(context: Context, state: AdvanceSalaryFormState) {
        try {
            val bitmap = generateAdvanceSalaryBitmap(state)
            val empLabel = state.applicantName.replace(" ", "_").ifBlank { "Employee" }
            val dateLabel = state.dateString.replace("/", "-").ifBlank { "Advance" }
            val cacheDir = File(context.cacheDir, "advance_salaries").apply { mkdirs() }
            val file = File(cacheDir, "Advance_Salary_${empLabel}_$dateLabel.png")
            if (file.exists()) file.delete()

            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            val authority = "${context.packageName}.fileprovider"
            val uri: Uri = FileProvider.getUriForFile(context, authority, file)

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Advance Salary Application - ${state.applicantName}")
                putExtra(Intent.EXTRA_TEXT, "Advance Salary Application for ${state.applicantName} (${state.dateString})")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(intent, "Share Advance Salary Image"))
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to share image: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }
}
