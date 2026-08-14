package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.DecimalFormat

object FoodBillImageExporter {

    /**
     * Renders a clean, compact, vertical "normal list type" cash voucher / memo image.
     * Dimensions are dynamically calculated to fit the content cleanly without wasteful A4 margins.
     */
    fun generateMemoListBitmap(memo: PrintMemoData): Bitmap {
        val validItems = memo.items.filter { it.name.isNotBlank() || it.amount > 0 }
        val numRows = maxOf(6, validItems.size) // minimum 6 rows for great aesthetic balance

        val imageWidth = 840
        val paddingX = 30f
        val paddingY = 30f
        val contentWidth = imageWidth - (paddingX * 2)

        // Heights
        val headerBoxHeight = if (memo.subtitle.isNotBlank()) 95f else 75f
        val metaHeight = 42f
        val tableHeaderHeight = 44f
        val rowHeight = 42f
        val totalRowHeight = 46f
        val tableHeight = tableHeaderHeight + (numRows * rowHeight) + totalRowHeight
        val footerHeight = 90f

        val totalHeight = (paddingY * 2 + headerBoxHeight + 12f + metaHeight + tableHeight + 20f + footerHeight).toInt()

        val bitmap = Bitmap.createBitmap(imageWidth, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background (Pure White)
        val bgPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, imageWidth.toFloat(), totalHeight.toFloat(), bgPaint)

        // Outer decorative border
        val outerBorderPaint = Paint().apply {
            isAntiAlias = true
            color = Color.rgb(20, 20, 20)
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
        }
        val innerBorderPaint = Paint().apply {
            isAntiAlias = true
            color = Color.rgb(80, 80, 80)
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        canvas.drawRect(12f, 12f, imageWidth - 12f, totalHeight - 12f, outerBorderPaint)
        canvas.drawRect(16f, 16f, imageWidth - 16f, totalHeight - 16f, innerBorderPaint)

        var currentY = paddingY + 6f
        val leftX = paddingX
        val rightX = imageWidth - paddingX
        val centerX = imageWidth / 2f

        // 1. Header Box
        val headerRect = RectF(leftX, currentY, rightX, currentY + headerBoxHeight)
        val headerBorderPaint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        canvas.drawRect(headerRect, headerBorderPaint)

        // Header Title
        val titleText = memo.centerName.ifBlank { "খাবার বিল ক্যাশ মেমো" }
        val titlePaint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = 32f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(titleText, centerX, currentY + 44f, titlePaint)

        // Subtitle
        if (memo.subtitle.isNotBlank()) {
            val subPaint = Paint().apply {
                isAntiAlias = true
                color = Color.rgb(50, 50, 50)
                textSize = 18f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(memo.subtitle, centerX, currentY + 76f, subPaint)
        }

        currentY += headerBoxHeight + 8f

        // Separator line
        val linePaint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            strokeWidth = 2f
        }
        canvas.drawLine(leftX, currentY, rightX, currentY, linePaint)
        currentY += 8f

        // 2. Metadata (Memo ID, Purchaser, Date)
        val metaPaint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val memoIdText = if (memo.memoId > 0) "মেমো নং: #${BengaliUtils.toBengaliDigits(memo.memoId.toString())}" else "চলতি বিল মেমো"
        metaPaint.textAlign = Paint.Align.LEFT
        canvas.drawText(memoIdText, leftX + 4f, currentY + 22f, metaPaint)

        if (memo.purchaserName.isNotBlank()) {
            val purchaserText = "ক্রেতা: ${memo.purchaserName}"
            val normalMetaPaint = Paint().apply {
                isAntiAlias = true
                color = Color.rgb(40, 40, 40)
                textSize = 19f
            }
            normalMetaPaint.textAlign = Paint.Align.LEFT
            canvas.drawText(purchaserText, leftX + 220f, currentY + 22f, normalMetaPaint)
        }

        val bnDate = BengaliUtils.toBengaliDigits(memo.dateString)
        metaPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("তারিখ: $bnDate", rightX - 4f, currentY + 22f, metaPaint)

        currentY += metaHeight

        // 3. Table Column Config
        // Total width: contentWidth (approx 780)
        // Sl: 80, Name: 340, Qty: 120, Rate: 100, Amount: 140 -> Sum = 780
        val colWidths = floatArrayOf(
            contentWidth * 0.10f, // Sl (10%)
            contentWidth * 0.44f, // Description (44%)
            contentWidth * 0.15f, // Quantity (15%)
            contentWidth * 0.13f, // Rate (13%)
            contentWidth * 0.18f  // Amount (18%)
        )

        val tableTop = currentY
        val tableBottom = tableTop + tableHeight
        val tableBorderPaint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }

        // Table Header
        val headerBgPaint = Paint().apply {
            color = Color.rgb(245, 245, 245)
            style = Paint.Style.FILL
        }
        val thRect = RectF(leftX, tableTop, rightX, tableTop + tableHeaderHeight)
        canvas.drawRect(thRect, headerBgPaint)
        canvas.drawRect(thRect, tableBorderPaint)

        val thTextPaint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = 19f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val colTitles = arrayOf("ক্র. নং", "খাবারের নাম / বিবরণ", "পরিমাণ", "দর", "টাকা")
        var curColX = leftX
        for (i in 0 until 5) {
            val colCenterX = curColX + colWidths[i] / 2f
            canvas.drawText(colTitles[i], colCenterX, tableTop + 29f, thTextPaint)
            curColX += colWidths[i]
        }

        // Table Rows
        val rowDashPaint = Paint().apply {
            isAntiAlias = true
            color = Color.rgb(120, 120, 120)
            strokeWidth = 1f
            pathEffect = DashPathEffect(floatArrayOf(5f, 4f), 0f)
        }
        val itemTextPaint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = 20f
        }
        val itemBoldPaint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        var rowY = tableTop + tableHeaderHeight
        for (r in 0 until numRows) {
            val slNo = BengaliUtils.toBengaliDigits(String.format("%02d", r + 1))
            val textY = rowY + 28f

            // Sl No
            itemBoldPaint.textAlign = Paint.Align.CENTER
            canvas.drawText(slNo, leftX + colWidths[0] / 2f, textY, itemBoldPaint)

            if (r < validItems.size) {
                val item = validItems[r]

                // Name (auto fit size if long)
                itemBoldPaint.textAlign = Paint.Align.LEFT
                val origSize = itemBoldPaint.textSize
                if (item.name.length > 30) {
                    itemBoldPaint.textSize = origSize * 0.80f
                } else if (item.name.length > 18) {
                    itemBoldPaint.textSize = origSize * 0.90f
                }
                canvas.drawText(item.name, leftX + colWidths[0] + 10f, textY, itemBoldPaint)
                itemBoldPaint.textSize = origSize

                // Quantity
                itemTextPaint.textAlign = Paint.Align.CENTER
                val bnQty = BengaliUtils.toBengaliDigits(item.quantity)
                canvas.drawText(bnQty, leftX + colWidths[0] + colWidths[1] + colWidths[2] / 2f, textY, itemTextPaint)

                // Rate
                val bnRate = if (item.rate == "0" || item.rate.isBlank()) "" else BengaliUtils.toBengaliDigits(item.rate)
                canvas.drawText(bnRate, leftX + colWidths[0] + colWidths[1] + colWidths[2] + colWidths[3] / 2f, textY, itemTextPaint)

                // Amount
                itemBoldPaint.textAlign = Paint.Align.RIGHT
                val bnAmount = if (item.amount <= 0) "—" else "${BengaliUtils.toBengaliDigits(DecimalFormat("#,##0").format(item.amount))}/-"
                canvas.drawText(bnAmount, rightX - 12f, textY, itemBoldPaint)
            }

            // Row bottom line
            canvas.drawLine(leftX, rowY + rowHeight, rightX, rowY + rowHeight, rowDashPaint)
            rowY += rowHeight
        }

        // Total Row
        val totalRowY = rowY
        val totalBgPaint = Paint().apply {
            color = Color.rgb(250, 250, 250)
            style = Paint.Style.FILL
        }
        val totalRect = RectF(leftX, totalRowY, rightX, totalRowY + totalRowHeight)
        canvas.drawRect(totalRect, totalBgPaint)
        canvas.drawLine(leftX, totalRowY, rightX, totalRowY, tableBorderPaint)

        val totalLabelPaint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = 21f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }
        val totalLabelX = leftX + colWidths[0] + colWidths[1] + colWidths[2] + colWidths[3] - 12f
        canvas.drawText("মোট —", totalLabelX, totalRowY + 31f, totalLabelPaint)

        val totalValPaint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }
        val bnTotal = if (memo.totalAmount <= 0) "0/-" else "${BengaliUtils.formatBengaliCurrency(memo.totalAmount)}/-"
        canvas.drawText(bnTotal, rightX - 12f, totalRowY + 31f, totalValPaint)

        // Draw Outer table box and vertical dividers
        canvas.drawRect(leftX, tableTop, rightX, tableBottom, tableBorderPaint)

        var divX = leftX
        for (i in 0 until 4) {
            divX += colWidths[i]
            canvas.drawLine(divX, tableTop, divX, tableBottom, tableBorderPaint)
        }

        currentY = tableBottom + 35f

        // 4. Footer Signatures & Tag
        val sigLineWidth = 220f
        val sigLinePaint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            strokeWidth = 1.5f
        }
        val sigTextPaint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        // Left Signature (Purchaser)
        canvas.drawLine(leftX + 10f, currentY + 25f, leftX + 10f + sigLineWidth, currentY + 25f, sigLinePaint)
        val sigLabel = memo.purchaserLabel.ifBlank { "ক্রয়কারীর স্বাক্ষর" }
        canvas.drawText(sigLabel, leftX + 10f + (sigLineWidth / 2f), currentY + 48f, sigTextPaint)

        // Right Tag
        val appTagPaint = Paint().apply {
            isAntiAlias = true
            color = Color.rgb(100, 100, 100)
            textSize = 15f
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("স্মার্ট ক্যাশ মেমো", rightX - 10f, currentY + 48f, appTagPaint)

        return bitmap
    }

    /**
     * Saves the generated food bill memo image directly to the device Gallery / Pictures folder.
     */
    fun saveMemoImageToGallery(context: Context, memo: PrintMemoData): Boolean {
        return try {
            val bitmap = generateMemoListBitmap(memo)
            val dateLabel = memo.dateString.replace("/", "-").ifBlank { "Bill" }
            val fileName = "Food_Bill_Memo_${dateLabel}_${System.currentTimeMillis()}.png"

            val success = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/FoodBills")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
                val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    }
                    contentValues.clear()
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    context.contentResolver.update(uri, contentValues, null, null)
                    true
                } else {
                    false
                }
            } else {
                @Suppress("DEPRECATION")
                val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val foodBillsDir = File(picturesDir, "FoodBills").apply { mkdirs() }
                val imageFile = File(foodBillsDir, fileName)
                FileOutputStream(imageFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                MediaScannerConnection.scanFile(context, arrayOf(imageFile.absolutePath), arrayOf("image/png"), null)
                true
            }

            if (success) {
                Toast.makeText(context, "মেমোর ছবি গ্যালারিতে (Pictures/FoodBills) সেভ হয়েছে!", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "ছবি সেভ করতে ব্যর্থ হয়েছে।", Toast.LENGTH_SHORT).show()
            }
            success
        } catch (e: Exception) {
            Toast.makeText(context, "ত্রুটি: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            false
        }
    }

    /**
     * Shares the food bill memo as a PNG image via Android share sheet.
     */
    fun shareMemoImage(context: Context, memo: PrintMemoData) {
        try {
            val bitmap = generateMemoListBitmap(memo)
            val dateLabel = memo.dateString.replace("/", "-").ifBlank { "Bill" }
            val cacheDir = File(context.cacheDir, "food_bill_images").apply { mkdirs() }
            val imageFile = File(cacheDir, "Food_Bill_Memo_$dateLabel.png")
            if (imageFile.exists()) imageFile.delete()

            FileOutputStream(imageFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            val authority = "${context.packageName}.fileprovider"
            val contentUri: Uri = FileProvider.getUriForFile(context, authority, imageFile)
            val displayName = memo.centerName.ifBlank { "খাবার বিল মেমো" }

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, "খাবার বিল মেমো - ${memo.dateString}")
                putExtra(Intent.EXTRA_TEXT, "$displayName - ${memo.dateString} তারিখের খাবার বিল মেমোর ছবি।")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "মেমোর ছবি শেয়ার করুন (WhatsApp / মেসেঞ্জার)")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "ছবি শেয়ার করতে ব্যর্থ: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }
}
