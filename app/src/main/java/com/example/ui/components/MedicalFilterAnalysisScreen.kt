package com.example.ui.components

import android.app.DatePickerDialog
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CodeGroupEntity
import com.example.data.CodeGroupItemEntity
import com.example.data.PresetMedicalCodeEntity
import com.example.ui.AnalysisSummary
import com.example.ui.DateFilterType
import com.example.ui.theme.DarkForestGreen
import com.example.ui.theme.ForestGreenText
import com.example.ui.theme.HeadingFontFamily
import com.example.util.BengaliUtils
import com.example.util.MedicalPrintUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MedicalFilterAnalysisScreen(
    summary: AnalysisSummary,
    codeGroups: List<CodeGroupEntity> = emptyList(),
    groupItems: List<CodeGroupItemEntity> = emptyList(),
    presetCodes: List<PresetMedicalCodeEntity>,
    activeDateFilter: DateFilterType,
    selectedSpecificDate: String,
    customStartDate: String,
    customEndDate: String,
    selectedCodeFilter: String?,
    selectedGroupFilter: Long? = null,
    selectedOwnerFilter: String? = null,
    onSelectDateFilter: (DateFilterType) -> Unit,
    onSelectSpecificDate: (String) -> Unit,
    onSelectCustomDateRange: (String, String) -> Unit,
    onSelectCodeFilter: (String?) -> Unit,
    onSelectGroupFilter: (Long?) -> Unit = {},
    onSelectOwnerFilter: (String?) -> Unit = {},
    onResetAllFilters: () -> Unit = {},
    onOpenGroupManager: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val showSpecificDatePicker = {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedCal = Calendar.getInstance().apply { set(year, month, dayOfMonth) }
                val formatted = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedCal.time)
                onSelectSpecificDate(formatted)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    val showStartDatePicker = {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedCal = Calendar.getInstance().apply { set(year, month, dayOfMonth) }
                val formatted = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedCal.time)
                onSelectCustomDateRange(formatted, customEndDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    val showEndDatePicker = {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedCal = Calendar.getInstance().apply { set(year, month, dayOfMonth) }
                val formatted = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedCal.time)
                onSelectCustomDateRange(customStartDate, formatted)
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
            .padding(14.dp)
    ) {
        // Hero Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkForestGreen)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0x33FFFFFF), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Analytics, contentDescription = null, tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ফিল্টার ও এনালাইসিস রিপোর্ট",
                                fontFamily = HeadingFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color.White
                            )
                            Text(
                                text = "তারিখ ও কোড অনুযায়ী কাজের পরিসংখ্যান",
                                fontSize = 12.sp,
                                color = Color(0xFFE0E0E0)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Summary Stats Big Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0x2BFFFFFF),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Text("মোট কাজের সংখ্যা (Total Count)", fontSize = 11.sp, color = Color(0xFFB0BEC5))
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${BengaliUtils.toBengaliDigits(summary.totalCount.toString())} টি",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = summary.filterDescriptionBn,
                            fontSize = 12.sp,
                            color = Color(0xFFE0E0E0)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Consolidated Dropdown Filters Section Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = null,
                            tint = DarkForestGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ফিল্টার সিস্টেম (Dropdown Filters)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = ForestGreenText
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (activeDateFilter != DateFilterType.TODAY || selectedCodeFilter != null || !selectedOwnerFilter.isNullOrBlank()) {
                            TextButton(
                                onClick = onResetAllFilters,
                                modifier = Modifier.testTag("reset_filters_btn")
                            ) {
                                Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("রিসেট", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 1. Date Filter Dropdown
                var dateDropdownExpanded by remember { mutableStateOf(false) }
                Column {
                    Text(
                        text = "১. তারিখ অনুযায়ী ফিল্টার (Date Filter):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Surface(
                            onClick = { dateDropdownExpanded = true },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFFCFD8DC)),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("date_filter_dropdown")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = DarkForestGreen, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${activeDateFilter.labelBn} (${activeDateFilter.labelEn})",
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 13.sp
                                    )
                                }
                                Icon(
                                    imageVector = if (dateDropdownExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = DarkForestGreen
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = dateDropdownExpanded,
                            onDismissRequest = { dateDropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.85f)
                        ) {
                            DateFilterType.values().forEach { filter ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("${filter.labelBn} (${filter.labelEn})", fontSize = 13.sp)
                                            if (activeDateFilter == filter) {
                                                Icon(Icons.Default.Check, contentDescription = null, tint = DarkForestGreen, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    },
                                    onClick = {
                                        onSelectDateFilter(filter)
                                        dateDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    if (activeDateFilter == DateFilterType.SPECIFIC_DATE) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            onClick = showSpecificDatePicker,
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("নির্দিষ্ট তারিখ পছন্দ করুন:", fontSize = 12.sp)
                                Text(selectedSpecificDate, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DarkForestGreen)
                            }
                        }
                    }

                    if (activeDateFilter == DateFilterType.CUSTOM_RANGE) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                onClick = showStartDatePicker,
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("শুরু:", fontSize = 10.sp)
                                    Text(customStartDate, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = DarkForestGreen)
                                }
                            }
                            Surface(
                                onClick = showEndDatePicker,
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("শেষ:", fontSize = 10.sp)
                                    Text(customEndDate, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = DarkForestGreen)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 2. Code Owner / Name Filter Dropdown
                var ownerDropdownExpanded by remember { mutableStateOf(false) }
                val availableOwners = remember(presetCodes) {
                    presetCodes.map { it.name.trim() }.filter { it.isNotBlank() }.distinct().sorted()
                }

                Column {
                    Text(
                        text = "২. কোড অনার / নাম অনুযায়ী ফিল্টার (Code Owner Filter):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Surface(
                            onClick = { ownerDropdownExpanded = true },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFFCFD8DC)),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("owner_filter_dropdown")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = DarkForestGreen, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = selectedOwnerFilter ?: "সব অনার / নাম (All Owners)",
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 13.sp,
                                        color = if (selectedOwnerFilter != null) DarkForestGreen else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (selectedOwnerFilter != null) {
                                        IconButton(
                                            onClick = { onSelectOwnerFilter(null) },
                                            modifier = Modifier.size(20.dp)
                                        ) {
                                            Icon(Icons.Default.Clear, contentDescription = "Clear Owner", tint = Color.Gray, modifier = Modifier.size(16.dp))
                                        }
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Icon(
                                        imageVector = if (ownerDropdownExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        tint = DarkForestGreen
                                    )
                                }
                            }
                        }

                        DropdownMenu(
                            expanded = ownerDropdownExpanded,
                            onDismissRequest = { ownerDropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.85f)
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("সব অনার / নাম (All Owners)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        if (selectedOwnerFilter == null) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = DarkForestGreen, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                },
                                onClick = {
                                    onSelectOwnerFilter(null)
                                    ownerDropdownExpanded = false
                                }
                            )

                            availableOwners.forEach { ownerName ->
                                val count = presetCodes.count { it.name.equals(ownerName, ignoreCase = true) }
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "$ownerName (${BengaliUtils.toBengaliDigits(count.toString())} টি কোড)",
                                                fontSize = 13.sp
                                            )
                                            if (selectedOwnerFilter == ownerName) {
                                                Icon(Icons.Default.Check, contentDescription = null, tint = DarkForestGreen, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    },
                                    onClick = {
                                        onSelectOwnerFilter(ownerName)
                                        ownerDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 3. Code Filter Dropdown
                var codeDropdownExpanded by remember { mutableStateOf(false) }

                val filteredPresetCodes = remember(presetCodes, selectedOwnerFilter) {
                    if (!selectedOwnerFilter.isNullOrBlank()) {
                        presetCodes.filter { it.name.equals(selectedOwnerFilter, ignoreCase = true) }
                    } else {
                        presetCodes
                    }
                }

                val availableCodesList = remember(filteredPresetCodes, summary.records, selectedOwnerFilter) {
                    val codeMap = mutableMapOf<String, String>()
                    filteredPresetCodes.forEach { codeMap[it.code] = it.name }
                    if (selectedOwnerFilter.isNullOrBlank()) {
                        summary.records.forEach { record ->
                            if (!codeMap.containsKey(record.code)) {
                                codeMap[record.code] = ""
                            }
                        }
                    }
                    codeMap.entries.toList().sortedBy { it.key }
                }

                Column {
                    Text(
                        text = "৩. কোড অনুযায়ী ফিল্টার (Code Filter):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Surface(
                            onClick = { codeDropdownExpanded = true },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFFCFD8DC)),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("code_filter_dropdown")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Code, contentDescription = null, tint = DarkForestGreen, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    val codeLabel = if (selectedCodeFilter != null) {
                                        val codeOwner = presetCodes.find { it.code.equals(selectedCodeFilter, ignoreCase = true) }?.name
                                        if (!codeOwner.isNullOrBlank()) "$selectedCodeFilter ($codeOwner)" else selectedCodeFilter!!
                                    } else {
                                        "সব কোড (All Codes)"
                                    }
                                    Text(
                                        text = codeLabel,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 13.sp,
                                        color = if (selectedCodeFilter != null) DarkForestGreen else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (selectedCodeFilter != null) {
                                        IconButton(
                                            onClick = { onSelectCodeFilter(null) },
                                            modifier = Modifier.size(20.dp)
                                        ) {
                                            Icon(Icons.Default.Clear, contentDescription = "Clear Code", tint = Color.Gray, modifier = Modifier.size(16.dp))
                                        }
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Icon(
                                        imageVector = if (codeDropdownExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        tint = DarkForestGreen
                                    )
                                }
                            }
                        }

                        DropdownMenu(
                            expanded = codeDropdownExpanded,
                            onDismissRequest = { codeDropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.85f)
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("সব কোড (All Codes)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        if (selectedCodeFilter == null) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = DarkForestGreen, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                },
                                onClick = {
                                    onSelectCodeFilter(null)
                                    codeDropdownExpanded = false
                                }
                            )

                            availableCodesList.forEach { entry ->
                                val codeStr = entry.key
                                val ownerStr = entry.value
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = if (ownerStr.isNotBlank()) "$codeStr — $ownerStr" else codeStr,
                                                fontSize = 13.sp
                                            )
                                            if (selectedCodeFilter == codeStr) {
                                                Icon(Icons.Default.Check, contentDescription = null, tint = DarkForestGreen, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    },
                                    onClick = {
                                        onSelectCodeFilter(codeStr)
                                        codeDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Clean Direct Record Table Card (Home screen style list)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "কোড ও আইডির তালিকা (Filtered Records)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = ForestGreenText
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = DarkForestGreen.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "${BengaliUtils.toBengaliDigits(summary.records.size.toString())} টি আইডি",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkForestGreen,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (summary.records.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "এই ফিল্টারে কোনো রেকর্ড পাওয়া যায়নি।",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    // Clean Grid Table (Home screen preview style)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFCFD8DC), RoundedCornerShape(8.dp))
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        // Header Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFE8EAF6))
                                .height(38.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "ক্রমিক",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(0.7f)
                            )
                            Divider(modifier = Modifier.fillMaxHeight().width(1.dp), color = Color(0xFFCFD8DC))
                            Text(
                                "পেশেন্ট আইডি",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1.5f)
                            )
                            Divider(modifier = Modifier.fillMaxHeight().width(1.dp), color = Color(0xFFCFD8DC))
                            Text(
                                "কোড",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1.2f)
                            )
                        }

                        Divider(color = Color(0xFFCFD8DC), thickness = 1.dp)

                        // Data Rows
                        summary.records.forEachIndexed { i, r ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(38.dp)
                                    .background(if (i % 2 == 1) Color(0xFFFAFAFA) else Color.White),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "${i + 1}",
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.weight(0.7f)
                                )
                                Divider(modifier = Modifier.fillMaxHeight().width(1.dp), color = Color(0xFFCFD8DC))
                                Text(
                                    r.patientId,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1.5f)
                                )
                                Divider(modifier = Modifier.fillMaxHeight().width(1.dp), color = Color(0xFFCFD8DC))
                                Text(
                                    r.code,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = DarkForestGreen,
                                    modifier = Modifier.weight(1.2f)
                                )
                            }
                            if (i < summary.records.size - 1) {
                                Divider(color = Color(0xFFCFD8DC), thickness = 0.8.dp)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bottom Report Share & Print Actions Card (Matching Home screen bottom options)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "রিপোর্ট শেয়ার ও প্রিন্ট (Export Options):",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = ForestGreenText
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 1. Share Image
                    OutlinedButton(
                        onClick = {
                            MedicalPrintUtils.shareDailyReportAsImage(
                                context,
                                summary.filterDescriptionBn,
                                summary.records
                            )
                        },
                        enabled = summary.records.isNotEmpty(),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("share_image_btn"),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, DarkForestGreen),
                        contentPadding = PaddingValues(vertical = 10.dp, horizontal = 4.dp)
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, tint = DarkForestGreen, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ছবি শেয়ার", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarkForestGreen)
                    }

                    // 2. Share PDF
                    Button(
                        onClick = {
                            MedicalPrintUtils.shareDailyReportAsPdf(
                                context,
                                summary.filterDescriptionBn,
                                summary.records
                            )
                        },
                        enabled = summary.records.isNotEmpty(),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("share_pdf_btn"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkForestGreen),
                        contentPadding = PaddingValues(vertical = 10.dp, horizontal = 4.dp)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PDF শেয়ার", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    // 3. Print
                    OutlinedButton(
                        onClick = {
                            MedicalPrintUtils.printDailyReport(
                                context,
                                summary.filterDescriptionBn,
                                summary.records
                            )
                        },
                        enabled = summary.records.isNotEmpty(),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("print_report_btn"),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFF1565C0)),
                        contentPadding = PaddingValues(vertical = 10.dp, horizontal = 4.dp)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, tint = Color(0xFF1565C0), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("প্রিন্ট", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
                    }
                }
            }
        }
    }
}
