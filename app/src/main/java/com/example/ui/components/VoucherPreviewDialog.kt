package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.BillItem
import com.example.data.FoodBillUiModel
import com.example.ui.theme.CreamPaperBg
import com.example.ui.theme.DarkForestGreen
import com.example.ui.theme.ForestGreenText
import com.example.util.BengaliUtils
import com.example.util.PrintMemoData
import com.example.util.PrintPosition

@Composable
fun VoucherPreviewDialog(
    initialTopMemo: PrintMemoData,
    initialBottomMemo: PrintMemoData? = null,
    historyBills: List<FoodBillUiModel> = emptyList(),
    defaultCenterName: String = "",
    defaultSubtitle: String = "",
    defaultPurchaserLabel: String = "",
    onDismiss: () -> Unit,
    onPrint: (PrintMemoData, PrintMemoData?, PrintPosition) -> Unit,
    onSharePdf: (PrintMemoData, PrintMemoData?, PrintPosition) -> Unit
) {
    var selectedPosition by remember {
        mutableStateOf(if (initialBottomMemo != null) PrintPosition.BOTH else PrintPosition.TOP)
    }

    var topMemoState by remember { mutableStateOf(initialTopMemo) }
    var bottomMemoState by remember { mutableStateOf(initialBottomMemo) }

    var showTopPicker by remember { mutableStateOf(false) }
    var showBottomPicker by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.96f)
                .testTag("voucher_preview_dialog"),
            shape = RoundedCornerShape(16.dp),
            color = CreamPaperBg
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                // Dialog Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "বিল প্রিভিউ & A4 পজিশন",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreenText
                        )
                        Text(
                            text = "A4 পেপারে প্রিন্ট/পিডিএফ পজিশন ও মেমো নির্বাচন করুন:",
                            fontSize = 12.sp,
                            color = Color.DarkGray
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_preview_dialog")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "বন্ধ করুন", tint = ForestGreenText)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Position Selection Bar (Segmented Controls)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE8E0D5), RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    PrintPosition.values().forEach { pos ->
                        val isSelected = selectedPosition == pos
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .background(
                                    if (isSelected) DarkForestGreen else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    selectedPosition = pos
                                    if (pos == PrintPosition.BOTH && bottomMemoState == null) {
                                        // Auto-suggest a second memo if available or duplicate
                                        val otherBill = historyBills.firstOrNull { it.id != topMemoState.memoId }
                                        bottomMemoState = if (otherBill != null) {
                                            PrintMemoData(
                                                memoId = otherBill.id,
                                                centerName = defaultCenterName.ifBlank { topMemoState.centerName },
                                                subtitle = defaultSubtitle.ifBlank { topMemoState.subtitle },
                                                dateString = otherBill.dateString,
                                                purchaserName = otherBill.purchaserName,
                                                purchaserLabel = defaultPurchaserLabel.ifBlank { topMemoState.purchaserLabel },
                                                items = otherBill.items,
                                                totalAmount = otherBill.totalAmount
                                            )
                                        } else {
                                            topMemoState
                                        }
                                    }
                                }
                                .padding(horizontal = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = pos.label,
                                fontSize = 11.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else ForestGreenText,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Dual Memo Selectors (When BOTH position is active or to allow changing top/bottom memo)
                if (selectedPosition == PrintPosition.BOTH) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(10.dp))
                            .border(1.dp, Color(0xFFD0C8B8), RoundedCornerShape(10.dp))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "A4 পেজে মেমো সেটআপ (উপরে ও নিচে আলাদা মেমো)",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkForestGreen
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Top Memo Selector Button
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { showTopPicker = true },
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFF4EFE6),
                                border = androidx.compose.foundation.BorderStroke(1.dp, DarkForestGreen)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = "১. উপরে (Top Slot):", fontSize = 10.sp, color = Color.Gray)
                                        val labelText = if (topMemoState.memoId > 0) "মেমো #${BengaliUtils.toBengaliDigits(topMemoState.memoId.toString())} (${topMemoState.dateString})" else "চলতি মেমো (${topMemoState.dateString})"
                                        Text(
                                            text = labelText,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = DarkForestGreen)
                                }
                            }

                            // Bottom Memo Selector Button
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { showBottomPicker = true },
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFF4EFE6),
                                border = androidx.compose.foundation.BorderStroke(1.dp, DarkForestGreen)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = "২. নিচে (Bottom Slot):", fontSize = 10.sp, color = Color.Gray)
                                        val bMemo = bottomMemoState ?: topMemoState
                                        val labelText = if (bMemo.memoId > 0) "মেমো #${BengaliUtils.toBengaliDigits(bMemo.memoId.toString())} (${bMemo.dateString})" else if (bottomMemoState == null || bMemo == topMemoState) "একই মেমো (কপি)" else "চলতি মেমো (${bMemo.dateString})"
                                        Text(
                                            text = labelText,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = DarkForestGreen)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Interactive A4 Paper Representation Scroll Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color(0xFFEDE7DC), shape = RoundedCornerShape(8.dp))
                        .padding(10.dp)
                        .verticalScroll(rememberScrollState()),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(6.dp))
                            .padding(8.dp)
                    ) {
                        val activeBottom = bottomMemoState ?: topMemoState

                        when (selectedPosition) {
                            PrintPosition.TOP -> {
                                SingleMemoVoucherCard(
                                    memo = topMemoState,
                                    slotLabel = "উপরে (Top Position)"
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                EmptyPaperHalfPlaceholder()
                            }
                            PrintPosition.BOTTOM -> {
                                EmptyPaperHalfPlaceholder()
                                Spacer(modifier = Modifier.height(10.dp))
                                SingleMemoVoucherCard(
                                    memo = activeBottom,
                                    slotLabel = "নিচে (Bottom Position)"
                                )
                            }
                            PrintPosition.BOTH -> {
                                SingleMemoVoucherCard(
                                    memo = topMemoState,
                                    slotLabel = "১. উপরের মেমো (Top Slot)"
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                CutLineSeparator()
                                Spacer(modifier = Modifier.height(8.dp))
                                SingleMemoVoucherCard(
                                    memo = activeBottom,
                                    slotLabel = "২. নিচের মেমো (Bottom Slot)"
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Direct System Print Button
                    Button(
                        onClick = {
                            onPrint(topMemoState, bottomMemoState ?: topMemoState, selectedPosition)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("preview_print_button"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkForestGreen, contentColor = Color.White)
                    ) {
                        Icon(imageVector = Icons.Default.Print, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "প্রিন্ট করুন", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    // Share PDF / WhatsApp Button
                    Button(
                        onClick = {
                            onSharePdf(topMemoState, bottomMemoState ?: topMemoState, selectedPosition)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("preview_share_pdf_button"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32), contentColor = Color.White)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "পিডিএফ শেয়ার", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }

    // Top Memo Picker Modal Dialog
    if (showTopPicker) {
        MemoSelectionDialog(
            title = "উপরের মেমো নির্বাচন করুন (Top Slot)",
            historyBills = historyBills,
            initialMemo = initialTopMemo,
            defaultCenterName = defaultCenterName,
            defaultSubtitle = defaultSubtitle,
            defaultPurchaserLabel = defaultPurchaserLabel,
            onDismiss = { showTopPicker = false },
            onSelect = { selectedMemo ->
                topMemoState = selectedMemo
                showTopPicker = false
            }
        )
    }

    // Bottom Memo Picker Modal Dialog
    if (showBottomPicker) {
        MemoSelectionDialog(
            title = "নিচের মেমো নির্বাচন করুন (Bottom Slot)",
            historyBills = historyBills,
            initialMemo = initialTopMemo,
            defaultCenterName = defaultCenterName,
            defaultSubtitle = defaultSubtitle,
            defaultPurchaserLabel = defaultPurchaserLabel,
            allowSameCopy = true,
            onDismiss = { showBottomPicker = false },
            onSelect = { selectedMemo ->
                bottomMemoState = selectedMemo
                showBottomPicker = false
            }
        )
    }
}

@Composable
private fun SingleMemoVoucherCard(
    memo: PrintMemoData,
    slotLabel: String,
    modifier: Modifier = Modifier
) {
    val validItems = memo.items.filter { it.name.isNotBlank() || it.amount > 0 }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(containerColor = CreamPaperBg)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Slot Label Indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE8E0D5))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = slotLabel,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkForestGreen
                )
                val memoTag = if (memo.memoId > 0) "মেমো #${BengaliUtils.toBengaliDigits(memo.memoId.toString())}" else "চলতি মেমো"
                Text(
                    text = memoTag,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )
            }

            // Header Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, Color.Black, RoundedCornerShape(4.dp))
                    .background(Color.White)
                    .padding(vertical = 8.dp, horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (memo.centerName.isNotBlank()) {
                        Text(
                            text = memo.centerName,
                            style = TextStyle(
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                    if (memo.subtitle.isNotBlank()) {
                        Text(
                            text = memo.subtitle,
                            style = TextStyle(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color.Black
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(3.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.5.dp)
                    .background(Color.Black)
            )
            Spacer(modifier = Modifier.height(3.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                // Metadata Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "তারিখ: ${BengaliUtils.toBengaliDigits(memo.dateString)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Table Grid
                val totalRows = maxOf(14, validItems.size.coerceAtMost(18))
                val rowPaddingY = if (totalRows > 14) 3.5.dp else 6.5.dp
                val dividerBoxHeight = if (totalRows > 14) 14.dp else 18.dp
                val itemTextSize = if (totalRows > 14) 9.sp else 10.sp
                val memoThemeBorder = Color.Black

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, memoThemeBorder, RoundedCornerShape(4.dp))
                ) {
                    // Table Header Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(vertical = 4.dp, horizontal = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ক্র. নং",
                            modifier = Modifier.weight(1.0f),
                            style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black, textAlign = TextAlign.Center),
                            maxLines = 1,
                            softWrap = false
                        )
                        Box(modifier = Modifier.width(1.2.dp).height(12.dp).background(Color.Black))
                        Text(
                            text = "খাবারের নাম / বিবরণ",
                            modifier = Modifier.weight(2.5f).padding(start = 4.dp),
                            style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black, textAlign = TextAlign.Start),
                            maxLines = 1,
                            softWrap = false
                        )
                        Box(modifier = Modifier.width(1.2.dp).height(12.dp).background(Color.Black))
                        Text(
                            text = "পরিমাণ",
                            modifier = Modifier.weight(1.2f),
                            style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black, textAlign = TextAlign.Center)
                        )
                        Box(modifier = Modifier.width(1.2.dp).height(12.dp).background(Color.Black))
                        Text(
                            text = "দর",
                            modifier = Modifier.weight(0.9f),
                            style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black, textAlign = TextAlign.Center)
                        )
                        Box(modifier = Modifier.width(1.2.dp).height(12.dp).background(Color.Black))
                        Text(
                            text = "টাকা",
                            modifier = Modifier.weight(1.2f).padding(end = 4.dp),
                            style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black, textAlign = TextAlign.End)
                        )
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(1.5.dp).background(Color.Black))

                    // Table Body Rows
                    for (r in 0 until totalRows) {
                        val slNo = BengaliUtils.toBengaliDigits(String.format("%02d", r + 1))
                        val item = if (r < validItems.size) validItems[r] else null

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = rowPaddingY, horizontal = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = slNo,
                                modifier = Modifier.weight(1.0f),
                                style = TextStyle(fontSize = itemTextSize, fontWeight = FontWeight.Bold, color = Color.Black, textAlign = TextAlign.Center)
                            )
                            Box(modifier = Modifier.width(1.2.dp).height(dividerBoxHeight).background(memoThemeBorder))

                            val rawName = item?.name ?: ""
                            val dynamicFontSize = when {
                                rawName.length > 35 -> (itemTextSize.value - 2.5f).sp
                                rawName.length > 20 -> (itemTextSize.value - 1.5f).sp
                                else -> itemTextSize
                            }
                            Text(
                                text = rawName,
                                modifier = Modifier.weight(2.5f).padding(horizontal = 4.dp),
                                style = TextStyle(
                                    fontSize = dynamicFontSize,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    lineHeight = (dynamicFontSize.value * 1.2f).sp
                                ),
                                softWrap = true
                            )
                            Box(modifier = Modifier.width(1.2.dp).height(dividerBoxHeight).background(memoThemeBorder))

                            Text(
                                text = if (item != null) BengaliUtils.toBengaliDigits(item.quantity) else "",
                                modifier = Modifier.weight(1.2f),
                                style = TextStyle(fontSize = itemTextSize, color = Color.Black, textAlign = TextAlign.Center)
                            )
                            Box(modifier = Modifier.width(1.2.dp).height(dividerBoxHeight).background(memoThemeBorder))

                            Text(
                                text = if (item != null && item.rate != "0" && item.rate.isNotBlank()) BengaliUtils.toBengaliDigits(item.rate) else "",
                                modifier = Modifier.weight(0.9f),
                                style = TextStyle(fontSize = itemTextSize, color = Color.Black, textAlign = TextAlign.Center)
                            )
                            Box(modifier = Modifier.width(1.2.dp).height(dividerBoxHeight).background(memoThemeBorder))

                            val bnAmount = if (item != null) {
                                if (item.amount <= 0) "—" else "${BengaliUtils.toBengaliDigits(item.amount.toInt().toString())}/-"
                            } else ""
                            Text(
                                text = bnAmount,
                                modifier = Modifier.weight(1.2f).padding(end = 4.dp),
                                style = TextStyle(fontSize = itemTextSize, fontWeight = FontWeight.Bold, color = Color.Black, textAlign = TextAlign.End)
                            )
                        }

                        Canvas(modifier = Modifier.fillMaxWidth().height(0.8.dp)) {
                            drawLine(
                                color = Color.Black,
                                start = Offset(0f, 0f),
                                end = Offset(size.width, 0f),
                                strokeWidth = 0.8.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 3f), 0f)
                            )
                        }
                    }

                    // Table Total Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(vertical = 5.dp, horizontal = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "মোট —",
                            modifier = Modifier.weight(5.6f).padding(end = 6.dp),
                            style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black, textAlign = TextAlign.End)
                        )
                        Box(modifier = Modifier.width(1.2.dp).height(16.dp).background(memoThemeBorder))
                        Text(
                            text = "${BengaliUtils.formatBengaliCurrency(memo.totalAmount)}/-",
                            modifier = Modifier.weight(1.2f).padding(end = 4.dp),
                            style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black, textAlign = TextAlign.End)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(130.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color.Black)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = memo.purchaserLabel.ifBlank { "ক্রয়কারীর স্বাক্ষর" },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CutLineSeparator() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(modifier = Modifier.weight(1f).height(1.dp)) {
            drawLine(
                color = Color.Gray,
                start = Offset(0f, 0f),
                end = Offset(size.width, 0f),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f), 0f)
            )
        }
        Text(
            text = " ✂ কাটিং লাইন (A4 হাফ পেজ) ",
            fontSize = 10.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Bold
        )
        Canvas(modifier = Modifier.weight(1f).height(1.dp)) {
            drawLine(
                color = Color.Gray,
                start = Offset(0f, 0f),
                end = Offset(size.width, 0f),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f), 0f)
            )
        }
    }
}

@Composable
private fun EmptyPaperHalfPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .background(Color(0xFFFBF9F4), RoundedCornerShape(4.dp))
            .border(1.dp, Color(0xFFE5DFD3), RoundedCornerShape(4.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "খালি পজিশন (প্রিন্ট পেজে ফাঁকা থাকবে)",
            fontSize = 11.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun MemoSelectionDialog(
    title: String,
    historyBills: List<FoodBillUiModel>,
    initialMemo: PrintMemoData,
    defaultCenterName: String,
    defaultSubtitle: String,
    defaultPurchaserLabel: String,
    allowSameCopy: Boolean = false,
    onDismiss: () -> Unit,
    onSelect: (PrintMemoData) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkForestGreen) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
            ) {
                if (historyBills.isEmpty() && !allowSameCopy) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("সংরক্ষিত কোনো মেমো নেই।", fontSize = 13.sp, color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Current Draft Memo option
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSelect(initialMemo)
                                    },
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(imageVector = Icons.Default.Receipt, contentDescription = null, tint = DarkForestGreen)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "চলতি মেমো (সম্পাদিত বিল)",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DarkForestGreen
                                        )
                                        Text(
                                            text = "তারিখ: ${initialMemo.dateString} | মোট: ${BengaliUtils.formatBengaliCurrency(initialMemo.totalAmount)}",
                                            fontSize = 11.sp,
                                            color = Color.DarkGray
                                        )
                                    }
                                }
                            }
                        }

                        // Saved History Bills
                        items(historyBills, key = { it.id }) { bill ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val printMemo = PrintMemoData(
                                            memoId = bill.id,
                                            centerName = bill.centerName,
                                            subtitle = bill.subtitle,
                                            dateString = bill.dateString,
                                            purchaserName = bill.purchaserName,
                                            purchaserLabel = defaultPurchaserLabel.ifBlank { initialMemo.purchaserLabel },
                                            items = bill.items,
                                            totalAmount = bill.totalAmount
                                        )
                                        onSelect(printMemo)
                                    },
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "মেমো #${BengaliUtils.toBengaliDigits(bill.id.toString())} — ${BengaliUtils.toBengaliDigits(bill.dateString)}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black
                                        )
                                        if (bill.purchaserName.isNotBlank()) {
                                            Text(
                                                text = "ক্রেতা: ${bill.purchaserName}",
                                                fontSize = 11.sp,
                                                color = Color.Gray
                                            )
                                        }
                                        Text(
                                            text = "${bill.items.size}টি আইটেম",
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                    }

                                    Text(
                                        text = BengaliUtils.formatBengaliCurrency(bill.totalAmount),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DarkForestGreen
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল", color = Color.Gray)
            }
        }
    )
}
