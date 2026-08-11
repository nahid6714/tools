package com.example.ui.components

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MedicalRecordEntity
import com.example.data.PresetMedicalCodeEntity
import com.example.ui.theme.DarkForestGreen
import com.example.ui.theme.ForestGreenText
import com.example.ui.theme.HeadingFontFamily
import com.example.util.BengaliUtils
import com.example.util.MedicalPrintUtils
import com.example.util.ScannedMedicalItem
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun computeDefaultPatientIdPrefix(dateStr: String): String {
    return try {
        if (dateStr.contains("-")) {
            val parts = dateStr.split("-")
            if (parts.size >= 2) {
                val year2 = parts[0].takeLast(2)
                val month2 = parts[1].padStart(2, '0')
                "AB$year2$month2"
            } else {
                val y = SimpleDateFormat("yy", Locale.getDefault()).format(Date())
                val m = SimpleDateFormat("MM", Locale.getDefault()).format(Date())
                "AB$y$m"
            }
        } else {
            val y = SimpleDateFormat("yy", Locale.getDefault()).format(Date())
            val m = SimpleDateFormat("MM", Locale.getDefault()).format(Date())
            "AB$y$m"
        }
    } catch (e: Exception) {
        val y = SimpleDateFormat("yy", Locale.getDefault()).format(Date())
        val m = SimpleDateFormat("MM", Locale.getDefault()).format(Date())
        "AB$y$m"
    }
}

fun formatPatientId(rawId: String, dateStr: String, existingItems: List<ScannedMedicalItem> = emptyList()): String {
    val trimmed = rawId.trim().uppercase(Locale.ROOT)
    if (trimmed.isEmpty()) return ""

    // Check if input is purely numeric (e.g., "1", "15", "001", "123")
    if (trimmed.all { it.isDigit() }) {
        // Find existing prefix in previous items if any (e.g. "AB2608")
        val existingPrefix = existingItems.asReversed().firstNotNullOfOrNull { item ->
            val digits = item.patientId.takeLastWhile { it.isDigit() }
            if (digits.isNotEmpty()) {
                val p = item.patientId.dropLast(digits.length)
                if (p.isNotBlank()) p else null
            } else null
        }

        val prefixToUse = existingPrefix ?: computeDefaultPatientIdPrefix(dateStr)

        val paddedNum = if (trimmed.length < 3) {
            String.format(Locale.US, "%03d", trimmed.toIntOrNull() ?: 0)
        } else {
            trimmed
        }

        return "$prefixToUse$paddedNum"
    }

    return trimmed
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MedicalReportGeneratorScreen(
    reportDate: String,
    editableItems: List<ScannedMedicalItem>,
    presetCodes: List<PresetMedicalCodeEntity>,
    isScanning: Boolean,
    scanStatusText: String,
    onDateChange: (String) -> Unit,
    onAddItem: (patientId: String, code: String) -> Unit,
    onUpdateItem: (index: Int, patientId: String, code: String) -> Unit,
    onRemoveItem: (index: Int) -> Unit,
    onClearAll: () -> Unit,
    onScanImage: (Bitmap) -> Unit,
    onSaveAndConfirm: () -> Unit,
    onDuplicateItem: ((index: Int) -> Unit)? = null,
    onMoveItem: ((fromIndex: Int, toIndex: Int) -> Unit)? = null,
    onAutoSequenceIds: ((prefix: String) -> Unit)? = null,
    onBatchApplyCode: ((newCode: String) -> Unit)? = null,
    onParseRawText: ((rawText: String) -> Unit)? = null,
    onAddPresetCode: ((code: String, name: String, category: String) -> Unit)? = null,
    onDeletePresetCode: ((code: String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val activity = remember(context) { context.findActivity() }

    var newPatientId by remember { mutableStateOf("") }
    var newCode by remember { mutableStateOf("") }
    var showReportPreviewDialog by remember { mutableStateOf(false) }
    var showPasteRawTextDialog by remember { mutableStateOf(false) }
    var showBatchCodeDialog by remember { mutableStateOf(false) }
    var showAutoSequenceDialog by remember { mutableStateOf(false) }
    var showPresetCodeManagerDialog by remember { mutableStateOf(false) }
    var rawInputText by remember { mutableStateOf("") }
    var batchTargetCode by remember { mutableStateOf("") }
    var sequencePrefix by remember { mutableStateOf("") }
    var showManualCodeDropdown by remember { mutableStateOf(false) }


    // List of active codes available for selection & cycling
    val availableCodeList = remember(presetCodes, editableItems) {
        val list = presetCodes.map { it.code }.toMutableList()
        editableItems.forEach { item ->
            if (item.code.isNotBlank() && !list.contains(item.code)) {
                list.add(item.code)
            }
        }
        list.filter { it.isNotBlank() }.distinct()
    }

    // Helper to load bitmap from Uri
    fun loadBitmap(uri: Uri): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
            } else {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // ML Kit Scanner Launcher for OCR Image Scanning
    val smartScannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val scanningResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
            val uri = scanningResult?.pages?.firstOrNull()?.imageUri
            if (uri != null) {
                coroutineScope.launch(Dispatchers.IO) {
                    val bitmap = loadBitmap(uri)
                    if (bitmap != null) {
                        withContext(Dispatchers.Main) {
                            onScanImage(bitmap)
                        }
                    }
                }
            }
        }
    }

    // Direct Camera Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let { onScanImage(it) }
    }

    // Gallery Launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch(Dispatchers.IO) {
                val bitmap = loadBitmap(it)
                if (bitmap != null) {
                    withContext(Dispatchers.Main) {
                        onScanImage(bitmap)
                    }
                }
            }
        }
    }

    val launchSmartScanner = {
        val options = GmsDocumentScannerOptions.Builder()
            .setGalleryImportAllowed(true)
            .setPageLimit(5)
            .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG)
            .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
            .build()

        if (activity != null) {
            try {
                GmsDocumentScanning.getClient(options)
                    .getStartScanIntent(activity)
                    .addOnSuccessListener { intentSender ->
                        smartScannerLauncher.launch(
                            IntentSenderRequest.Builder(intentSender).build()
                        )
                    }
                    .addOnFailureListener {
                        cameraLauncher.launch(null)
                    }
            } catch (e: Exception) {
                cameraLauncher.launch(null)
            }
        } else {
            cameraLauncher.launch(null)
        }
    }

    val showDatePicker = {
        val calendar = Calendar.getInstance()
        try {
            val parts = reportDate.split("-")
            if (parts.size == 3) {
                calendar.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedCal = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }
                val formatted = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedCal.time)
                onDateChange(formatted)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(12.dp)
    ) {
        // Hero Card with OCR Scanner Launchers
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkForestGreen),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.DocumentScanner, contentDescription = null, tint = Color.White)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "মেডিকেল ডেইলি রিপোর্ট ও OCR স্ক্যানার",
                                fontFamily = HeadingFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.5.sp,
                                color = Color.White
                            )
                            Text(
                                text = "হাতে লেখা আইডি ও কোডের ছবি তুলে সরাসরি অটো-ইমপোর্ট করুন",
                                fontSize = 11.5.sp,
                                color = Color(0xFFE2D6C5)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Date Picker Chip Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        onClick = showDatePicker,
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White.copy(alpha = 0.15f),
                        modifier = Modifier
                            .testTag("btn_select_report_date")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "তারিখ: $reportDate",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Raw text paste action
                    OutlinedButton(
                        onClick = { showPasteRawTextDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White.copy(alpha = 0.15f),
                            contentColor = Color.White
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("টেক্সট পেস্ট", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // OCR Image Scanner Action Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Smart Document Scanner Button (with auto-crop)
                    Button(
                        onClick = { launchSmartScanner() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF28A745)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(44.dp)
                            .testTag("smart_ocr_scan_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("স্মার্ট ক্যামেরা (OCR)", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Gallery upload button
                    Button(
                        onClick = { galleryLauncher.launch("image/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("গ্যালারি", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        if (isScanning) {
            Spacer(modifier = Modifier.height(10.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), color = DarkForestGreen, strokeWidth = 2.5.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = scanStatusText.ifBlank { "ছবি স্ক্যান ও AI/OCR তথ্য বের করা হচ্ছে..." },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5D4037)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Quick Single Item Add & Preset Code Bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "নতুন আইডি ও কোড ম্যানুয়ালি যোগ করুন:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = ForestGreenText
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val previewFormattedId = if (newPatientId.isNotBlank() && newPatientId.all { it.isDigit() }) {
                        formatPatientId(newPatientId, reportDate, editableItems)
                    } else null

                    OutlinedTextField(
                        value = newPatientId,
                        onValueChange = { newPatientId = it.uppercase(Locale.ROOT) },
                        label = { Text("সিরিয়াল / আইডি (যেমন 15)", fontSize = 11.sp) },
                        supportingText = if (previewFormattedId != null) {
                            { Text("→ $previewFormattedId", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DarkForestGreen) }
                        } else null,
                        modifier = Modifier.weight(1.3f),
                        singleLine = true
                    )

                    Box(modifier = Modifier.weight(1.1f)) {
                        OutlinedTextField(
                            value = newCode,
                            onValueChange = {
                                newCode = it
                                showManualCodeDropdown = true
                            },
                            label = { Text("কোড সিলেক্ট করুন", fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            trailingIcon = {
                                IconButton(onClick = { showManualCodeDropdown = !showManualCodeDropdown }) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDownward,
                                        contentDescription = "কোড ড্রোপডাউন",
                                        tint = DarkForestGreen
                                    )
                                }
                            }
                        )

                        DropdownMenu(
                            expanded = showManualCodeDropdown,
                            onDismissRequest = { showManualCodeDropdown = false },
                            modifier = Modifier.fillMaxWidth(0.55f)
                        ) {
                            Text(
                                text = "ড্রোপডাউন থেকে কোড বেছে নিন:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkForestGreen,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                            Divider()
                            val filteredCodes = if (newCode.isBlank()) availableCodeList else availableCodeList.filter { it.contains(newCode, ignoreCase = true) }
                            val displayCodes = if (filteredCodes.isEmpty()) availableCodeList else filteredCodes

                            displayCodes.forEach { codeOpt ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = codeOpt,
                                            fontSize = 13.sp,
                                            fontWeight = if (newCode.equals(codeOpt, ignoreCase = true)) FontWeight.Bold else FontWeight.Normal,
                                            color = if (newCode.equals(codeOpt, ignoreCase = true)) DarkForestGreen else Color.Unspecified
                                        )
                                    },
                                    onClick = {
                                        newCode = codeOpt
                                        showManualCodeDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            val formattedId = formatPatientId(newPatientId, reportDate, editableItems)
                            if (formattedId.isNotBlank() && newCode.isNotBlank()) {
                                onAddItem(formattedId, newCode)
                                // Auto increment patient ID for quick next entry
                                val digits = formattedId.takeLastWhile { it.isDigit() }
                                if (digits.isNotEmpty()) {
                                    val prefix = formattedId.dropLast(digits.length)
                                    val nextNum = (digits.toLongOrNull() ?: 0L) + 1
                                    newPatientId = "$prefix${String.format(Locale.US, "%0${digits.length}d", nextNum)}"
                                } else {
                                    newPatientId = ""
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkForestGreen),
                        modifier = Modifier.height(54.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "যোগ করুন")
                    }
                }

                // Quick Management Actions
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { showPresetCodeManagerDialog = true },
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(13.dp), tint = DarkForestGreen)
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("কোড যোগ / এডিট", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = DarkForestGreen)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Grocery-List Style Editable Table Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Table Title & Metrics Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "মেডিকেল কাজের মেমো তালিকা (Review Table)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "মোট: ${BengaliUtils.toBengaliDigits(editableItems.size.toString())}টি এন্ট্রি | বাজার লিস্টের মতো সহজে এডিট করুন",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (editableItems.isNotEmpty()) {
                        IconButton(onClick = onClearAll) {
                            Icon(Icons.Default.Clear, contentDescription = "সব মুছুন", tint = Color.Red)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Quick Batch Toolbar (Auto-Sequence, Batch Code, Paste)
                if (editableItems.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showAutoSequenceDialog = true },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.FormatListNumbered, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("আইডি সিকোয়েন্স", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { showBatchCodeDialog = true },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("সব কোড পরিবর্তন", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Table Header Bar
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("নং", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(30.dp))
                        Text("পেশেন্ট আইডি", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.3f))
                        Text("কোড (১-ট্যাপ চেইঞ্জ)", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f))
                        Text("অ্যাকশন", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.width(88.dp))
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                if (editableItems.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.DocumentScanner, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "তালিকা খালি। ছবি তুলে স্ক্যান করুন অথবা ম্যানুয়ালি আইডি যোগ করুন।",
                                fontSize = 12.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    editableItems.forEachIndexed { index, item ->
                        GroceryStyleItemRow(
                            index = index,
                            item = item,
                            totalCount = editableItems.size,
                            availableCodes = availableCodeList,
                            onUpdate = { pId, c -> onUpdateItem(index, pId, c) },
                            onRemove = { onRemoveItem(index) },
                            onDuplicate = { onDuplicateItem?.invoke(index) },
                            onMoveUp = { onMoveItem?.invoke(index, index - 1) },
                            onMoveDown = { onMoveItem?.invoke(index, index + 1) }
                        )
                        Divider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Action Buttons: Save & Confirm, Print / Preview
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showReportPreviewDialog = true },
                        enabled = editableItems.isNotEmpty(),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("প্রিন্ট / প্রিভিউ", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            onSaveAndConfirm()
                            showReportPreviewDialog = true
                        },
                        enabled = editableItems.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkForestGreen),
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("নিশ্চিত ও সেভ করুন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Modal Dialog: Paste Raw Text for Auto OCR / Parsing
    if (showPasteRawTextDialog) {
        AlertDialog(
            onDismissRequest = { showPasteRawTextDialog = false },
            title = { Text("স্মার্ট টেক্সট ইনপুট / পেস্ট", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column {
                    Text(
                        text = "হাতে লেখা বা কপি করা মেসেজের টেক্সট পেস্ট করুন। যেমন:\nAB2608001 101\nAB2608002 102, 105",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = rawInputText,
                        onValueChange = { rawInputText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        placeholder = { Text("এখানে টেক্সট পেস্ট করুন...") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (rawInputText.isNotBlank()) {
                            onParseRawText?.invoke(rawInputText)
                            rawInputText = ""
                            showPasteRawTextDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkForestGreen)
                ) {
                    Text("ইমপোর্ট করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPasteRawTextDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // Modal Dialog: Batch Code Change
    if (showBatchCodeDialog) {
        AlertDialog(
            onDismissRequest = { showBatchCodeDialog = false },
            title = { Text("সব রেকর্ড এর কোড পরিবর্তন", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column {
                    Text("তালিকার সকল আইটেমে নিচের কোডটি সেট করা হবে:", fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = batchTargetCode,
                        onValueChange = { batchTargetCode = it },
                        label = { Text("নতুন কোড (যেমন 101, CBC, USG)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        availableCodeList.forEach { c ->
                            Surface(
                                onClick = { batchTargetCode = c },
                                shape = RoundedCornerShape(10.dp),
                                color = if (batchTargetCode == c) DarkForestGreen else MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = c,
                                    fontSize = 11.sp,
                                    color = if (batchTargetCode == c) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onBatchApplyCode?.invoke(batchTargetCode)
                        showBatchCodeDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkForestGreen)
                ) {
                    Text("প্রয়োগ করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBatchCodeDialog = false }) { Text("বাতিল") }
            }
        )
    }

    // Modal Dialog: Auto-Sequence Patient IDs
    if (showAutoSequenceDialog) {
        val currentYearMonth = remember {
            val m = SimpleDateFormat("MM", Locale.getDefault()).format(Date())
            val y = SimpleDateFormat("yy", Locale.getDefault()).format(Date())
            "AB$y$m"
        }

        AlertDialog(
            onDismissRequest = { showAutoSequenceDialog = false },
            title = { Text("অটো পেশেন্ট আইডি সিকোয়েন্স", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column {
                    Text("পেশেন্ট প্রিফিক্স দিন (যেমন AB2608), এটি ১, ২, ৩ এভাবে পর পর সিকোয়েন্স করে দেবে:", fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = sequencePrefix,
                        onValueChange = { sequencePrefix = it.uppercase(Locale.ROOT) },
                        label = { Text("প্রিফিক্স (যেমন $currentYearMonth)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onAutoSequenceIds?.invoke(sequencePrefix.ifBlank { currentYearMonth })
                        showAutoSequenceDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkForestGreen)
                ) {
                    Text("সিকোয়েন্স সেট করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAutoSequenceDialog = false }) { Text("বাতিল") }
            }
        )
    }

    // Daily Report Printable Preview Dialog
    if (showReportPreviewDialog && editableItems.isNotEmpty()) {
        val records = editableItems.map { MedicalRecordEntity(date = reportDate, patientId = it.patientId, code = it.code) }
        val formattedDate = try {
            if (reportDate.contains("-")) {
                val p = reportDate.split("-")
                if (p.size == 3) "${p[2].padStart(2, '0')}/${p[1].padStart(2, '0')}/${p[0].takeLast(2)}" else reportDate
            } else reportDate
        } catch (e: Exception) { reportDate }

        AlertDialog(
            onDismissRequest = { showReportPreviewDialog = false },
            title = {
                Text("ডেইলি রিপোর্ট প্রিভিউ", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .background(Color.White)
                        .padding(8.dp)
                ) {
                    // Date Header Top Right
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "তারিখ: $formattedDate",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.align(Alignment.CenterEnd)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Divider(color = Color.Black, thickness = 1.5.dp)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Clean Grid Table
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color.Black)
                    ) {
                        // Header Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "ক্রমিক",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.weight(0.7f)
                            )
                            Divider(modifier = Modifier.fillMaxHeight().width(1.dp), color = Color.Black)
                            Text(
                                "ID",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.weight(1.3f)
                            )
                            Divider(modifier = Modifier.fillMaxHeight().width(1.dp), color = Color.Black)
                            Text(
                                "কোড",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.weight(1.3f)
                            )
                        }

                        Divider(color = Color.Black, thickness = 1.dp)

                        // Data Rows
                        records.forEachIndexed { i, r ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(36.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "${i + 1}",
                                    fontSize = 12.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.weight(0.7f)
                                )
                                Divider(modifier = Modifier.fillMaxHeight().width(1.dp), color = Color.Black)
                                Text(
                                    r.patientId,
                                    fontSize = 12.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.weight(1.3f)
                                )
                                Divider(modifier = Modifier.fillMaxHeight().width(1.dp), color = Color.Black)
                                Text(
                                    r.code,
                                    fontSize = 12.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.weight(1.3f)
                                )
                            }
                            if (i < records.size - 1) {
                                Divider(color = Color.Black, thickness = 1.dp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { MedicalPrintUtils.shareDailyReportAsImage(context, reportDate, records) }
                    ) {
                        Icon(Icons.Default.Image, contentDescription = "Share Image", tint = DarkForestGreen)
                    }
                    IconButton(
                        onClick = { MedicalPrintUtils.shareDailyReportAsPdf(context, reportDate, records) }
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = "Share PDF", tint = DarkForestGreen)
                    }
                    IconButton(
                        onClick = { MedicalPrintUtils.printDailyReport(context, reportDate, records) }
                    ) {
                        Icon(Icons.Default.Print, contentDescription = "Print", tint = DarkForestGreen)
                    }
                    TextButton(onClick = { showReportPreviewDialog = false }) {
                        Text("বন্ধ করুন")
                    }
                }
            }
        )
    }

    // Modal Dialog: Preset Code Manager (Add/Delete Limited Set of Codes)
    if (showPresetCodeManagerDialog) {
        var newCodeInput by remember { mutableStateOf("") }
        var newNameInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showPresetCodeManagerDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Style, contentDescription = null, tint = DarkForestGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("প্রিসেট কোড ম্যানেজার", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "নতুন কোড যোগ করুন (কাজের রিপোর্টে ১-ট্যাপে সিলেক্ট করতে):",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newCodeInput,
                            onValueChange = { newCodeInput = it },
                            label = { Text("কোড (যেমন 106, USG)", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = newNameInput,
                            onValueChange = { newNameInput = it },
                            label = { Text("নাম (যেমন X-Ray)", fontSize = 11.sp) },
                            modifier = Modifier.weight(1.2f),
                            singleLine = true
                        )

                        Button(
                            onClick = {
                                if (newCodeInput.isNotBlank()) {
                                    onAddPresetCode?.invoke(newCodeInput.trim(), newNameInput.trim(), "General")
                                    newCodeInput = ""
                                    newNameInput = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkForestGreen),
                            contentPadding = PaddingValues(horizontal = 10.dp),
                            modifier = Modifier.height(54.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("যোগ", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("সংরক্ষিত প্রিসেট কোডসমূহ (${availableCodeList.size}টি):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))

                    availableCodeList.forEach { codeStr ->
                        val presetItem = presetCodes.find { it.code.equals(codeStr, ignoreCase = true) }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("কোড: $codeStr", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DarkForestGreen)
                                if (presetItem != null && presetItem.name.isNotBlank()) {
                                    Text(presetItem.name, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            IconButton(
                                onClick = { onDeletePresetCode?.invoke(codeStr) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Code", tint = Color.Red, modifier = Modifier.size(16.dp))
                            }
                        }
                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showPresetCodeManagerDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkForestGreen)
                ) {
                    Text("সম্পন্ন")
                }
            }
        )
    }


}

/**
 * Single Row Item Composable designed like a Grocery List row
 * with direct patient ID input, 1-tap code cycle/step buttons, duplicate, move, and remove.
 */
@Composable
private fun GroceryStyleItemRow(
    index: Int,
    item: ScannedMedicalItem,
    totalCount: Int,
    availableCodes: List<String>,
    onUpdate: (patientId: String, code: String) -> Unit,
    onRemove: () -> Unit,
    onDuplicate: (() -> Unit)? = null,
    onMoveUp: (() -> Unit)? = null,
    onMoveDown: (() -> Unit)? = null
) {
    var showCodeDropdown by remember { mutableStateOf(false) }

    // Cycle to previous/next code in available list
    val cycleCode = { forward: Boolean ->
        val currentIdx = availableCodes.indexOf(item.code)
        val nextIdx = if (currentIdx == -1) {
            0
        } else if (forward) {
            (currentIdx + 1) % availableCodes.size
        } else {
            (currentIdx - 1 + availableCodes.size) % availableCodes.size
        }
        onUpdate(item.patientId, availableCodes[nextIdx])
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Serial Number Badge
        Text(
            text = BengaliUtils.toBengaliDigits((index + 1).toString()),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = ForestGreenText,
            modifier = Modifier.width(28.dp)
        )

        // Patient ID Input Box
        OutlinedTextField(
            value = item.patientId,
            onValueChange = { updatedId ->
                val formatted = if (updatedId.isNotBlank() && updatedId.all { it.isDigit() } && updatedId.length >= 3) {
                    formatPatientId(updatedId, "", emptyList())
                } else {
                    updatedId.uppercase(Locale.ROOT)
                }
                onUpdate(formatted, item.code)
            },
            modifier = Modifier
                .weight(1.3f)
                .padding(end = 4.dp),
            singleLine = true
        )

        // Code Input & Dropdown Selector (Unit Selection Style)
        Row(
            modifier = Modifier
                .weight(1.3f)
                .padding(end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { cycleCode(false) },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Previous Code", tint = DarkForestGreen, modifier = Modifier.size(16.dp))
            }

            Box(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = item.code,
                    onValueChange = { updatedCode -> onUpdate(item.patientId, updatedCode) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    trailingIcon = {
                        IconButton(
                            onClick = { showCodeDropdown = true },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                Icons.Default.ArrowDownward,
                                contentDescription = "Select Preset Code",
                                tint = DarkForestGreen,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                )

                DropdownMenu(
                    expanded = showCodeDropdown,
                    onDismissRequest = { showCodeDropdown = false }
                ) {
                    Text(
                        text = "কোড বেছে নিন (যেমন কেজি/গ্রাম সিলেক্ট):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkForestGreen,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                    Divider()
                    availableCodes.forEach { codeOpt ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = codeOpt,
                                    fontSize = 13.sp,
                                    fontWeight = if (item.code.equals(codeOpt, ignoreCase = true)) FontWeight.Bold else FontWeight.Normal,
                                    color = if (item.code.equals(codeOpt, ignoreCase = true)) DarkForestGreen else Color.Unspecified
                                )
                            },
                            onClick = {
                                onUpdate(item.patientId, codeOpt)
                                showCodeDropdown = false
                            }
                        )
                    }
                }
            }

            IconButton(
                onClick = { cycleCode(true) },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Next Code", tint = DarkForestGreen, modifier = Modifier.size(16.dp))
            }
        }

        // Action Toolbar (Move Up/Down, Duplicate, Delete)
        Row(
            modifier = Modifier.width(88.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (index > 0 && onMoveUp != null) {
                IconButton(onClick = onMoveUp, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = "Up", modifier = Modifier.size(14.dp))
                }
            }

            if (index < totalCount - 1 && onMoveDown != null) {
                IconButton(onClick = onMoveDown, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.ArrowDownward, contentDescription = "Down", modifier = Modifier.size(14.dp))
                }
            }

            if (onDuplicate != null) {
                IconButton(onClick = onDuplicate, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate", tint = DarkForestGreen, modifier = Modifier.size(13.dp))
                }
            }

            IconButton(onClick = onRemove, modifier = Modifier.size(26.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.Red, modifier = Modifier.size(16.dp))
            }
        }
    }
}
