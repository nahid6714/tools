package com.example.ui

import android.app.DatePickerDialog
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AppSplashScreen
import com.example.ui.components.BillHistoryList
import com.example.ui.components.GlobalSettingsScreen
import com.example.ui.components.MemoVoucherCard
import com.example.ui.components.OnboardingDialog
import com.example.ui.components.QuickPresetChips
import com.example.ui.components.SettingsScreen
import com.example.ui.components.VoucherPreviewDialog
import com.example.ui.theme.DarkForestGreen
import com.example.ui.theme.HeadingFontFamily
import com.example.ui.theme.LightForestGreen
import com.example.update.AppUpdateManager
import com.example.update.UpdateDialog
import com.example.update.UpdateInfo
import com.example.util.PrintMemoData
import com.example.util.PrintUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: FoodBillViewModel
) {
    val context = LocalContext.current
    val currentBillState by viewModel.currentBillState.collectAsStateWithLifecycle()
    val historyBills by viewModel.historyBills.collectAsStateWithLifecycle()
    val quickPresets by viewModel.quickPresets.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()
    val fontScale by viewModel.fontScale.collectAsStateWithLifecycle()
    val showOnboarding by viewModel.showOnboarding.collectAsStateWithLifecycle()

    val isEn = appLanguage == "en"

    var selectedTool by remember { mutableStateOf<String?>("food_bill") }
    var selectedTab by remember { mutableIntStateOf(0) }
    var showGlobalSettings by remember { mutableStateOf(false) }
    var showMemoSettings by remember { mutableStateOf(false) }
    var showPreviewDialog by remember { mutableStateOf(false) }
    var previewTopMemo by remember { mutableStateOf<PrintMemoData?>(null) }
    var previewBottomMemo by remember { mutableStateOf<PrintMemoData?>(null) }
    var isSplashLoading by remember { mutableStateOf(true) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val updateManager = remember { AppUpdateManager() }
    var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }
    var isCheckingUpdate by remember { mutableStateOf(false) }
    var updateDialogInfoToShow by remember { mutableStateOf<UpdateInfo?>(null) }

    LaunchedEffect(Unit) {
        try {
            isCheckingUpdate = true
            val info = updateManager.checkForUpdate(context, forceCheck = false)
            updateInfo = info
            if (info.hasUpdate) {
                updateDialogInfoToShow = info
            }
        } catch (e: Exception) {
            // Keep app safe from network/parsing exceptions
        } finally {
            isCheckingUpdate = false
        }
    }

    BackHandler(enabled = showGlobalSettings || showMemoSettings) {
        if (showMemoSettings) {
            showMemoSettings = false
        } else {
            showGlobalSettings = false
        }
    }

    LaunchedEffect(Unit) {
        delay(1200)
        isSplashLoading = false
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    // Android System Date Picker Dialog
    val showDatePicker = {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val formattedDay = String.format("%02d", dayOfMonth)
                val formattedMonth = String.format("%02d", month + 1)
                viewModel.updateDate("$formattedDay/$formattedMonth/$year")
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Crossfade(
        targetState = isSplashLoading,
        animationSpec = tween(600),
        label = "splash_fade"
    ) { loading ->
        if (loading) {
            AppSplashScreen(
                appName = "Digital Tool",
                subtitle = "স্মার্ট বিলিং সহকারী"
            )
        } else {
            val baseDensity = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(
                    density = baseDensity.density,
                    fontScale = baseDensity.fontScale * fontScale
                )
            ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = when {
                                    showGlobalSettings -> if (isEn) "Main App Settings" else "মেইন অ্যাপ সেটিংস"
                                    showMemoSettings -> if (isEn) "Memo Header Settings" else "মেমো হেডার সেটিংস"
                                    selectedTool == "food_bill" -> when (selectedTab) {
                                        1 -> if (isEn) "Saved Records" else "সংরক্ষিত হিসাব"
                                        else -> if (currentBillState.centerName.isNotBlank())
                                            (if (isEn) "Food Bill - " else "খাবার বিল - ") + currentBillState.centerName
                                        else
                                            (if (isEn) "Food Bill Memo" else "খাবার বিল মেমো")
                                    }
                                    else -> if (isEn) "Digital Tools Hub" else "ডিজিটাল টুলস হাব"
                                },
                                fontFamily = HeadingFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 19.sp
                            )
                        },
                        navigationIcon = {
                            if (showGlobalSettings || showMemoSettings) {
                                IconButton(onClick = {
                                    if (showMemoSettings) {
                                        showMemoSettings = false
                                    } else {
                                        showGlobalSettings = false
                                    }
                                }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "ফিরে যান",
                                        tint = Color.White
                                    )
                                }
                            }
                        },
                        actions = {
                            if (!showGlobalSettings && !showMemoSettings) {
                                IconButton(
                                    onClick = { showGlobalSettings = true },
                                    modifier = Modifier.testTag("global_settings_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "অ্যাপ সেটিংস",
                                        tint = Color.White
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            titleContentColor = Color.White
                        )
                    )
                },
                bottomBar = {
                    if (!showGlobalSettings && !showMemoSettings && selectedTool == "food_bill") {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ) {
                            val darkUnselectedColor = MaterialTheme.colorScheme.onSurfaceVariant

                            NavigationBarItem(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                icon = { Icon(Icons.Default.Edit, contentDescription = if (isEn) "Daily Bill" else "দৈনিক বিল") },
                                label = { Text(if (isEn) "Daily Bill" else "দৈনিক বিল", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.White,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primary,
                                    unselectedIconColor = darkUnselectedColor,
                                    unselectedTextColor = darkUnselectedColor
                                ),
                                modifier = Modifier.testTag("tab_daily_bill")
                            )

                            NavigationBarItem(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                icon = { Icon(Icons.Default.History, contentDescription = if (isEn) "Saved Records" else "সংরক্ষিত হিসাব") },
                                label = { Text(if (isEn) "Saved Records" else "সংরক্ষিত হিসাব", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.White,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primary,
                                    unselectedIconColor = darkUnselectedColor,
                                    unselectedTextColor = darkUnselectedColor
                                ),
                                modifier = Modifier.testTag("tab_saved_history")
                            )
                        }
                    }
                },
                snackbarHost = { SnackbarHost(snackbarHostState) }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    if (showGlobalSettings) {
                        GlobalSettingsScreen(
                            themeMode = themeMode,
                            appLanguage = appLanguage,
                            fontScale = fontScale,
                            onThemeModeChange = { mode -> viewModel.setThemeMode(mode) },
                            onLanguageChange = { lang -> viewModel.setAppLanguage(lang) },
                            onFontScaleChange = { scale -> viewModel.setFontScale(scale) },
                            onOpenMemoSettings = {
                                showGlobalSettings = false
                                showMemoSettings = true
                            }
                        )
                    } else if (showMemoSettings) {
                        SettingsScreen(
                            state = currentBillState,
                            quickPresets = quickPresets,
                            onAddCustomPreset = { name, qty, rate, amount ->
                                viewModel.addCustomQuickPreset(name, qty, rate, amount)
                            },
                            onRemovePreset = { preset ->
                                viewModel.removeQuickPreset(preset)
                            },
                            onResetPresetsDefault = {
                                viewModel.resetQuickPresetsToDefault()
                            },
                            onCenterNameChange = { viewModel.updateCenterName(it) },
                            onSubtitleChange = { viewModel.updateSubtitle(it) },
                            onPurchaserLabelChange = { viewModel.updatePurchaserLabel(it) },
                            onSaveSettings = { centerName, subtitle, purchaserLabel ->
                                viewModel.saveSettings(centerName, subtitle, purchaserLabel)
                            },
                            onResetTemplate = { viewModel.resetToInitialTemplate() },
                            onResetAllData = { viewModel.resetAllUserData() }
                        )
                    } else if (selectedTool == "food_bill") {
                        when (selectedTab) {
                            0 -> {
                                // Daily Bill Voucher Editor
                                val mainScrollState = rememberScrollState()
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(mainScrollState)
                                        .padding(12.dp)
                                ) {
                                    // Memo Paper Cash Voucher
                                    MemoVoucherCard(
                                        state = currentBillState,
                                        quickPresets = quickPresets,
                                        scrollState = mainScrollState,
                                        onCenterNameChange = { viewModel.updateCenterName(it) },
                                        onSubtitleChange = { viewModel.updateSubtitle(it) },
                                        onPresetClick = { name, qty, rate, amount ->
                                            viewModel.addQuickPresetItem(name, qty, rate, amount)
                                        },
                                        onAddCustomPreset = { name, qty, rate, amount ->
                                            viewModel.addCustomQuickPreset(name, qty, rate, amount)
                                        },
                                        onAddCustomPresetWithParent = { name, qty, rate, amount, parentId ->
                                            viewModel.addCustomQuickPreset(name, qty, rate, amount, parentId)
                                        },
                                        onAddFolder = { folderName, parentId ->
                                            viewModel.addQuickPresetFolder(folderName, parentId)
                                        },
                                        onUpdatePresetNode = { nodeId, name, qty, rate, amount, isFolder, parentId, children ->
                                            viewModel.updateQuickPresetNode(nodeId, name, qty, rate, amount, isFolder, parentId, children)
                                        },
                                        onRemovePreset = { preset ->
                                            viewModel.removeQuickPreset(preset)
                                        },
                                        onResetDefaults = {
                                            viewModel.resetQuickPresetsToDefault()
                                        },
                                        onReorderPreset = { from, to ->
                                            viewModel.reorderQuickPresets(from, to)
                                        },
                                        onReorderPresetWithParent = { parentId, from, to ->
                                            viewModel.reorderQuickPresets(parentId, from, to)
                                        },
                                        onUpdateDateClick = { showDatePicker() },
                                        onUpdateItemName = { id, name -> viewModel.updateItemName(id, name) },
                                        onUpdateItemQty = { id, qty -> viewModel.updateItemQuantity(id, qty) },
                                        onUpdateItemRate = { id, rate -> viewModel.updateItemRate(id, rate) },
                                        onUpdateItemAmount = { id, amount -> viewModel.updateItemAmount(id, amount) },
                                        onRemoveItem = { id -> viewModel.removeItemRow(id) },
                                        onMoveItem = { from, to -> viewModel.moveItem(from, to) },
                                        onAddItemRow = { viewModel.addItemRow() },
                                        onPurchaserLabelChange = { viewModel.updatePurchaserLabel(it) }
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Action Buttons Bar
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 20.dp),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Print / Preview Button (Forest Green Gradient)
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(48.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    brush = Brush.horizontalGradient(
                                                        colors = listOf(Color(0xFF0D47A1), Color(0xFF1565C0))
                                                    )
                                                )
                                                .clickable {
                                                    previewTopMemo = PrintMemoData(
                                                        memoId = currentBillState.editingBillId,
                                                        centerName = currentBillState.centerName,
                                                        subtitle = currentBillState.subtitle,
                                                        dateString = currentBillState.dateString,
                                                        purchaserName = currentBillState.purchaserName,
                                                        purchaserLabel = currentBillState.purchaserLabel,
                                                        items = currentBillState.items.filter { it.name.isNotBlank() || it.amount > 0 },
                                                        totalAmount = currentBillState.totalAmount
                                                    )
                                                    previewBottomMemo = null
                                                    showPreviewDialog = true
                                                }
                                                .testTag("print_bill_button")
                                                .padding(horizontal = 4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "শেয়ার / প্রিভিউ",
                                                    fontSize = 13.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    maxLines = 1,
                                                    softWrap = false
                                                )
                                            }
                                        }

                                        // Save Button (Deep Emerald/Forest Green Gradient)
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(48.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    brush = Brush.horizontalGradient(
                                                        colors = listOf(Color(0xFF1565C0), Color(0xFF03A9F4))
                                                    )
                                                )
                                                .clickable { viewModel.saveCurrentBill() }
                                                .testTag("save_bill_button")
                                                .padding(horizontal = 4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Icon(Icons.Default.Save, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "সংরক্ষণ",
                                                    fontSize = 13.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    maxLines = 1,
                                                    softWrap = false
                                                )
                                            }
                                        }

                                        // Clear / New Button
                                        OutlinedButton(
                                            onClick = { viewModel.resetToInitialTemplate() },
                                            modifier = Modifier
                                                .height(48.dp)
                                                .testTag("clear_bill_button"),
                                            contentPadding = PaddingValues(horizontal = 10.dp),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(Icons.Default.Clear, contentDescription = "নতুন মেমো", tint = Color.DarkGray, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                }
                            }

                            1 -> {
                                // History View
                                BillHistoryList(
                                    bills = historyBills,
                                    searchQuery = searchQuery,
                                    onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                                    onEditBill = { bill ->
                                        viewModel.loadBillForEditing(bill)
                                        selectedTab = 0
                                    },
                                    onPrintBill = { bill ->
                                        previewTopMemo = PrintMemoData(
                                            memoId = bill.id,
                                            centerName = bill.centerName,
                                            subtitle = bill.subtitle,
                                            dateString = bill.dateString,
                                            purchaserName = bill.purchaserName,
                                            purchaserLabel = currentBillState.purchaserLabel,
                                            items = bill.items.filter { it.name.isNotBlank() || it.amount > 0 },
                                            totalAmount = bill.totalAmount
                                        )
                                        previewBottomMemo = null
                                        showPreviewDialog = true
                                    },
                                    onPrintDualBills = { bill1, bill2 ->
                                        previewTopMemo = PrintMemoData(
                                            memoId = bill1.id,
                                            centerName = bill1.centerName,
                                            subtitle = bill1.subtitle,
                                            dateString = bill1.dateString,
                                            purchaserName = bill1.purchaserName,
                                            purchaserLabel = currentBillState.purchaserLabel,
                                            items = bill1.items.filter { it.name.isNotBlank() || it.amount > 0 },
                                            totalAmount = bill1.totalAmount
                                        )
                                        previewBottomMemo = PrintMemoData(
                                            memoId = bill2.id,
                                            centerName = bill2.centerName,
                                            subtitle = bill2.subtitle,
                                            dateString = bill2.dateString,
                                            purchaserName = bill2.purchaserName,
                                            purchaserLabel = currentBillState.purchaserLabel,
                                            items = bill2.items.filter { it.name.isNotBlank() || it.amount > 0 },
                                            totalAmount = bill2.totalAmount
                                        )
                                        showPreviewDialog = true
                                    },
                                    onSharePdfBill = { bill ->
                                        previewTopMemo = PrintMemoData(
                                            memoId = bill.id,
                                            centerName = bill.centerName,
                                            subtitle = bill.subtitle,
                                            dateString = bill.dateString,
                                            purchaserName = bill.purchaserName,
                                            purchaserLabel = currentBillState.purchaserLabel,
                                            items = bill.items.filter { it.name.isNotBlank() || it.amount > 0 },
                                            totalAmount = bill.totalAmount
                                        )
                                        previewBottomMemo = null
                                        showPreviewDialog = true
                                    },
                                    onDeleteBill = { id -> viewModel.deleteBill(id) }
                                )
                            }

                            2 -> {
                                // Settings View
                                SettingsScreen(
                                    state = currentBillState,
                                    quickPresets = quickPresets,
                                    onAddCustomPreset = { name, qty, rate, amount ->
                                        viewModel.addCustomQuickPreset(name, qty, rate, amount)
                                    },
                                    onRemovePreset = { preset ->
                                        viewModel.removeQuickPreset(preset)
                                    },
                                    onResetPresetsDefault = {
                                        viewModel.resetQuickPresetsToDefault()
                                    },
                                    onCenterNameChange = { viewModel.updateCenterName(it) },
                                    onSubtitleChange = { viewModel.updateSubtitle(it) },
                                    onPurchaserLabelChange = { viewModel.updatePurchaserLabel(it) },
                                    onSaveSettings = { centerName, subtitle, purchaserLabel ->
                                        viewModel.saveSettings(centerName, subtitle, purchaserLabel)
                                    },
                                    onResetTemplate = { viewModel.resetToInitialTemplate() },
                                    onResetAllData = { viewModel.resetAllUserData() }
                                )
                            }
                        }
                    }
                }
            }
            }
        }
    }

    // Voucher Preview Modal Dialog
    if (showPreviewDialog) {
        val defaultTop = previewTopMemo ?: PrintMemoData(
            memoId = currentBillState.editingBillId,
            centerName = currentBillState.centerName,
            subtitle = currentBillState.subtitle,
            dateString = currentBillState.dateString,
            purchaserName = currentBillState.purchaserName,
            purchaserLabel = currentBillState.purchaserLabel,
            items = currentBillState.items.filter { it.name.isNotBlank() || it.amount > 0 },
            totalAmount = currentBillState.totalAmount
        )

        VoucherPreviewDialog(
            initialTopMemo = defaultTop,
            initialBottomMemo = previewBottomMemo,
            historyBills = historyBills,
            defaultCenterName = currentBillState.centerName,
            defaultSubtitle = currentBillState.subtitle,
            defaultPurchaserLabel = currentBillState.purchaserLabel,
            onDismiss = { showPreviewDialog = false },
            onPrint = { topMemo, bottomMemo, pos ->
                PrintUtils.printFoodBillDual(
                    context = context,
                    topMemo = topMemo,
                    bottomMemo = bottomMemo,
                    position = pos
                )
            },
            onSharePdf = { topMemo, bottomMemo, pos ->
                PrintUtils.shareFoodBillPdfDual(
                    context = context,
                    topMemo = topMemo,
                    bottomMemo = bottomMemo,
                    position = pos
                )
            },
            onSaveImage = { memo ->
                PrintUtils.saveFoodBillImage(
                    context = context,
                    memo = memo
                )
            },
            onShareImage = { memo ->
                PrintUtils.shareFoodBillImage(
                    context = context,
                    memo = memo
                )
            }
        )
    }

    // In-App Update Dialog
    updateDialogInfoToShow?.let { info ->
        UpdateDialog(
            updateInfo = info,
            updateManager = updateManager,
            onDismiss = { updateDialogInfoToShow = null }
        )
    }

    // First-launch walkthrough (shown once)
    if (showOnboarding && !isSplashLoading) {
        OnboardingDialog(onDismiss = { viewModel.dismissOnboarding() })
    }
}
