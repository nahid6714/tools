package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.CodeGroupEntity
import com.example.data.CodeGroupItemEntity
import com.example.data.MedicalRecordEntity
import com.example.data.MedicalRepository
import com.example.data.PresetMedicalCodeEntity
import com.example.util.MedicalImageOcrScanner
import com.example.util.ScannedMedicalItem
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class DateFilterType(val labelBn: String, val labelEn: String) {
    TODAY("আজ", "Today"),
    YESTERDAY("গতকাল", "Yesterday"),
    SPECIFIC_DATE("নির্দিষ্ট তারিখ", "Specific Date"),
    THIS_MONTH("এই মাস", "This Month"),
    LAST_MONTH("গত মাস", "Last Month"),
    CUSTOM_RANGE("তারিখের সীমা", "Custom Range")
}

data class AnalysisSummary(
    val totalCount: Int = 0,
    val filterDescriptionBn: String = "",
    val dateBreakdown: List<DateCountItem> = emptyList(),
    val codeBreakdown: List<CodeCountItem> = emptyList(),
    val records: List<MedicalRecordEntity> = emptyList()
)

data class DateCountItem(
    val date: String, // YYYY-MM-DD
    val formattedDateBn: String,
    val count: Int
)

data class CodeCountItem(
    val code: String,
    val codeName: String = "",
    val count: Int
)

private data class DateFilterState(
    val dateFilter: DateFilterType,
    val specDate: String,
    val startD: String,
    val endD: String
)

private data class CategoryFilterState(
    val codeFilter: String?,
    val groupFilter: Long?
)

class MedicalWorkViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MedicalRepository

    private val _uiEvent = MutableSharedFlow<String>()
    val uiEvent: SharedFlow<String> = _uiEvent.asSharedFlow()

    // Database state flows
    val allRecords: StateFlow<List<MedicalRecordEntity>>
    val allCodeGroups: StateFlow<List<CodeGroupEntity>>
    val allGroupItems: StateFlow<List<CodeGroupItemEntity>>
    val allPresetCodes: StateFlow<List<PresetMedicalCodeEntity>>

    // Generator Screen State
    private val _reportDate = MutableStateFlow(getTodayDateStr())
    val reportDate: StateFlow<String> = _reportDate.asStateFlow()

    private val _editableItems = MutableStateFlow<List<ScannedMedicalItem>>(emptyList())
    val editableItems: StateFlow<List<ScannedMedicalItem>> = _editableItems.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _scanStatusText = MutableStateFlow("")
    val scanStatusText: StateFlow<String> = _scanStatusText.asStateFlow()

    // Filter & Analysis State
    private val _dateFilterType = MutableStateFlow(DateFilterType.TODAY)
    val dateFilterType: StateFlow<DateFilterType> = _dateFilterType.asStateFlow()

    private val _selectedSpecificDate = MutableStateFlow(getTodayDateStr())
    val selectedSpecificDate: StateFlow<String> = _selectedSpecificDate.asStateFlow()

    private val _customStartDate = MutableStateFlow(getTodayDateStr())
    val customStartDate: StateFlow<String> = _customStartDate.asStateFlow()

    private val _customEndDate = MutableStateFlow(getTodayDateStr())
    val customEndDate: StateFlow<String> = _customEndDate.asStateFlow()

    private val _selectedCodeFilter = MutableStateFlow<String?>(null) // null = all
    val selectedCodeFilter: StateFlow<String?> = _selectedCodeFilter.asStateFlow()

    private val _selectedGroupFilter = MutableStateFlow<Long?>(null) // null = all, groupId for Nahid, etc.
    val selectedGroupFilter: StateFlow<Long?> = _selectedGroupFilter.asStateFlow()

    private val _analysisSummary = MutableStateFlow(AnalysisSummary())
    val analysisSummary: StateFlow<AnalysisSummary> = _analysisSummary.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = MedicalRepository(database.medicalDao())

        allRecords = repository.allRecords.toStateFlow(emptyList())
        allCodeGroups = repository.allCodeGroups.toStateFlow(emptyList())
        allGroupItems = repository.allGroupItems.toStateFlow(emptyList())
        allPresetCodes = repository.allPresetCodes.toStateFlow(emptyList())

        viewModelScope.launch {
            repository.seedDefaultsIfEmpty()
        }

        // Generator list starts empty for user input
        _editableItems.value = emptyList()

        // Recalculate analysis summary whenever filters or records change
        val dateFiltersFlow = combine(
            _dateFilterType,
            _selectedSpecificDate,
            _customStartDate,
            _customEndDate
        ) { dateFilter, specDate, startD, endD ->
            DateFilterState(dateFilter, specDate, startD, endD)
        }

        val categoryFiltersFlow = combine(
            _selectedCodeFilter,
            _selectedGroupFilter
        ) { codeFilter, groupFilter ->
            CategoryFilterState(codeFilter, groupFilter)
        }

        combine(
            allRecords,
            allCodeGroups,
            allGroupItems,
            dateFiltersFlow,
            categoryFiltersFlow
        ) { records, groups, groupItems, dateFilters, catFilters ->
            computeAnalysisSummary(
                records, groups, groupItems,
                dateFilters.dateFilter, dateFilters.specDate, dateFilters.startD, dateFilters.endD,
                catFilters.codeFilter, catFilters.groupFilter
            )
        }.onEach { summary ->
            _analysisSummary.value = summary
        }.launchIn(viewModelScope)
    }

    private fun <T> kotlinx.coroutines.flow.Flow<T>.toStateFlow(initialValue: T): StateFlow<T> {
        val state = MutableStateFlow(initialValue)
        viewModelScope.launch {
            collect { state.value = it }
        }
        return state
    }

    private fun getTodayDateStr(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    private fun loadDefaultSampleGeneratorItems() {
        val monthStr = SimpleDateFormat("MM", Locale.getDefault()).format(Date())
        val yearStr = SimpleDateFormat("yy", Locale.getDefault()).format(Date())
        val prefix = "AB$yearStr$monthStr"

        _editableItems.value = listOf(
            ScannedMedicalItem("${prefix}001", "101"),
            ScannedMedicalItem("${prefix}002", "102"),
            ScannedMedicalItem("${prefix}003", "101"),
            ScannedMedicalItem("${prefix}004", "105"),
            ScannedMedicalItem("${prefix}005", "104")
        )
    }

    // Generator Screen Actions
    fun setReportDate(dateStr: String) {
        _reportDate.value = dateStr
    }

    fun addItemToGenerator(patientId: String, code: String) {
        val current = _editableItems.value.toMutableList()
        current.add(ScannedMedicalItem(patientId = patientId.trim(), code = code.trim()))
        _editableItems.value = current
    }

    fun updateGeneratorItem(index: Int, patientId: String, code: String) {
        val current = _editableItems.value.toMutableList()
        if (index in current.indices) {
            current[index] = ScannedMedicalItem(patientId = patientId.trim(), code = code.trim())
            _editableItems.value = current
        }
    }

    fun removeGeneratorItem(index: Int) {
        val current = _editableItems.value.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _editableItems.value = current
        }
    }

    fun moveGeneratorItem(fromIndex: Int, toIndex: Int) {
        val current = _editableItems.value.toMutableList()
        if (fromIndex in current.indices && toIndex in current.indices) {
            val item = current.removeAt(fromIndex)
            current.add(toIndex, item)
            _editableItems.value = current
        }
    }

    fun duplicateGeneratorItem(index: Int) {
        val current = _editableItems.value.toMutableList()
        if (index in current.indices) {
            val original = current[index]
            val nextId = incrementPatientId(original.patientId)
            current.add(index + 1, ScannedMedicalItem(patientId = nextId, code = original.code))
            _editableItems.value = current
        }
    }

    fun autoSequencePatientIds(prefix: String) {
        val current = _editableItems.value.toMutableList()
        if (current.isEmpty()) return
        val cleanPrefix = prefix.ifBlank {
            val monthStr = SimpleDateFormat("MM", Locale.getDefault()).format(Date())
            val yearStr = SimpleDateFormat("yy", Locale.getDefault()).format(Date())
            "AB$yearStr$monthStr"
        }
        val resequenced = current.mapIndexed { idx, item ->
            val numStr = String.format(Locale.US, "%03d", idx + 1)
            ScannedMedicalItem(patientId = "$cleanPrefix$numStr", code = item.code)
        }
        _editableItems.value = resequenced
    }

    fun batchApplyCode(newCode: String) {
        if (newCode.isBlank()) return
        val current = _editableItems.value
        _editableItems.value = current.map { it.copy(code = newCode.trim()) }
    }

    private fun incrementPatientId(id: String): String {
        val digits = id.takeLastWhile { it.isDigit() }
        if (digits.isEmpty()) return "$id-1"
        val prefix = id.dropLast(digits.length)
        val num = digits.toLongOrNull() ?: return "$id-1"
        val nextNum = num + 1
        val formattedNum = String.format(Locale.US, "%0${digits.length}d", nextNum)
        return "$prefix$formattedNum"
    }

    /**
     * Suggests the next patient ID by continuing the serial from the most recently saved
     * record across ALL dates (allRecords is already sorted date DESC, id DESC), so serial
     * numbers keep running forward day-to-day instead of resetting back to 001 each day.
     * Falls back to a fresh date-based prefix (e.g. AB2608001) only when there is no
     * previous record at all to continue from.
     */
    fun getNextSuggestedPatientId(): String {
        val lastRecord = allRecords.value.firstOrNull()
        if (lastRecord != null && lastRecord.patientId.isNotBlank()) {
            return incrementPatientId(lastRecord.patientId)
        }
        val monthStr = SimpleDateFormat("MM", Locale.getDefault()).format(Date())
        val yearStr = SimpleDateFormat("yy", Locale.getDefault()).format(Date())
        return "AB$yearStr${monthStr}001"
    }

    fun clearGeneratorItems() {
        _editableItems.value = emptyList()
    }

    fun scanImageForMedicalReport(bitmap: Bitmap) {
        viewModelScope.launch {
            _isScanning.value = true
            _scanStatusText.value = "ছবি স্ক্যান ও AI/OCR শনাক্তকরণ চলছে..."
            try {
                val result = MedicalImageOcrScanner.scanImage(bitmap)
                if (result.date.isNotBlank()) {
                    _reportDate.value = result.date
                }
                if (result.items.isNotEmpty()) {
                    _editableItems.value = result.items
                    _uiEvent.emit("স্ক্যান সফল! ${result.items.size}টি রেকর্ড শনাক্ত করা হয়েছে।")
                } else {
                    _uiEvent.emit("ছবি থেকে রেকর্ড শনাক্ত করা যায়নি। ম্যানুয়ালি যোগ করুন।")
                }
            } catch (e: Exception) {
                _uiEvent.emit("স্ক্যান ত্রুটি: ${e.message}")
            } finally {
                _isScanning.value = false
                _scanStatusText.value = ""
            }
        }
    }

    fun parseRawTextForMedicalReport(rawText: String) {
        val result = MedicalImageOcrScanner.parseRawText(rawText, _reportDate.value)
        _reportDate.value = result.date
        _editableItems.value = result.items
        viewModelScope.launch {
            _uiEvent.emit("${result.items.size}টি আইডি এবং কোড ইম্পোর্ট করা হয়েছে।")
        }
    }

    fun saveAndConfirmDailyReport() {
        val items = _editableItems.value
        if (items.isEmpty()) {
            viewModelScope.launch { _uiEvent.emit("রিপোর্ট তালিকায় কোনো আইডি বা কোড নেই!") }
            return
        }

        viewModelScope.launch {
            val dateStr = _reportDate.value
            // First delete any previous entries for this exact date to allow overwrite/update cleanly
            repository.deleteRecordsByDate(dateStr)

            val entities = items.map { item ->
                MedicalRecordEntity(
                    date = dateStr,
                    patientId = item.patientId,
                    code = item.code
                )
            }
            repository.saveRecords(entities)
            _uiEvent.emit("তারিখ $dateStr এর ${entities.size}টি মেডিকেল কাজের রেকর্ড ডাটাবেজে সংরক্ষিত হয়েছে!")
        }
    }

    fun deleteRecordsForDate(dateStr: String) {
        viewModelScope.launch {
            repository.deleteRecordsByDate(dateStr)
            _uiEvent.emit("তারিখ $dateStr এর রেকর্ড মুছে ফেলা হয়েছে।")
        }
    }

    // Filter & Analysis Actions
    fun setDateFilterType(type: DateFilterType) {
        _dateFilterType.value = type
    }

    fun setSelectedSpecificDate(dateStr: String) {
        _selectedSpecificDate.value = dateStr
    }

    fun setCustomDateRange(startDate: String, endDate: String) {
        _customStartDate.value = startDate
        _customEndDate.value = endDate
    }

    fun setSelectedCodeFilter(code: String?) {
        _selectedCodeFilter.value = code
        if (code != null) {
            _selectedGroupFilter.value = null // reset group if code selected
        }
    }

    fun setSelectedGroupFilter(groupId: Long?) {
        _selectedGroupFilter.value = groupId
        if (groupId != null) {
            _selectedCodeFilter.value = null // reset individual code if group selected
        }
    }

    // Group Management Actions
    fun createCodeGroup(groupName: String, description: String, codes: List<String>) {
        if (groupName.isBlank()) return
        viewModelScope.launch {
            repository.createCodeGroup(groupName.trim(), description.trim(), codes)
            _uiEvent.emit("কোড গ্রুপ '$groupName' সফলভাবে তৈরি হয়েছে!")
        }
    }

    fun updateCodeGroup(groupId: Long, groupName: String, description: String, codes: List<String>) {
        if (groupName.isBlank()) return
        viewModelScope.launch {
            repository.updateCodeGroup(groupId, groupName.trim(), description.trim(), codes)
            _uiEvent.emit("কোড গ্রুপ '$groupName' আপডেট করা হয়েছে!")
        }
    }

    fun deleteCodeGroup(groupId: Long, groupName: String) {
        viewModelScope.launch {
            repository.deleteCodeGroup(groupId)
            if (_selectedGroupFilter.value == groupId) {
                _selectedGroupFilter.value = null
            }
            _uiEvent.emit("গ্রুপ '$groupName' মুছে ফেলা হয়েছে।")
        }
    }

    fun addPresetCode(code: String, name: String, category: String) {
        if (code.isBlank()) return
        viewModelScope.launch {
            repository.savePresetCode(code.trim(), name.trim(), category.trim())
            _uiEvent.emit("নতুন কোড '$code' যোগ করা হয়েছে!")
        }
    }

    fun deletePresetCode(code: String) {
        viewModelScope.launch {
            repository.deletePresetCode(code)
            _uiEvent.emit("কোড '$code' মুছে ফেলা হয়েছে।")
        }
    }

    // Filter computation logic
    private fun computeAnalysisSummary(
        records: List<MedicalRecordEntity>,
        groups: List<CodeGroupEntity>,
        groupItems: List<CodeGroupItemEntity>,
        dateFilter: DateFilterType,
        specDate: String,
        startD: String,
        endD: String,
        codeFilter: String?,
        groupFilter: Long?
    ): AnalysisSummary {
        // 1. Date Filtering
        val (startDate, endDate) = calculateDateBounds(dateFilter, specDate, startD, endD)
        val dateFilteredRecords = records.filter { record ->
            record.date in startDate..endDate
        }

        // 2. Code / Group Filtering
        val targetCodes = mutableSetOf<String>()
        var filterDesc = ""

        if (groupFilter != null) {
            val selectedGroup = groups.find { it.id == groupFilter }
            val groupCodes = groupItems.filter { it.groupId == groupFilter }.map { it.code }
            targetCodes.addAll(groupCodes)
            val groupName = selectedGroup?.groupName ?: "গ্রুপ"
            filterDesc = "গ্রুপ: $groupName (কোড: ${groupCodes.joinToString(", ")})"
        } else if (!codeFilter.isNullOrBlank()) {
            targetCodes.add(codeFilter)
            filterDesc = "কোড: $codeFilter"
        } else {
            filterDesc = "সব কোড"
        }

        val finalFilteredRecords = if (targetCodes.isNotEmpty()) {
            dateFilteredRecords.filter { record -> record.code in targetCodes }
        } else {
            dateFilteredRecords
        }

        // 3. Compute Date Breakdown
        val dateMap = finalFilteredRecords.groupBy { it.date }
        val dateBreakdown = dateMap.map { (date, list) ->
            DateCountItem(
                date = date,
                formattedDateBn = formatDateBn(date),
                count = list.size
            )
        }.sortedByDescending { it.date }

        // 4. Compute Code Breakdown
        val codeMap = finalFilteredRecords.groupBy { it.code }
        val codeBreakdown = codeMap.map { (code, list) ->
            CodeCountItem(
                code = code,
                codeName = getCodeNameFor(code),
                count = list.size
            )
        }.sortedByDescending { it.count }

        val fullFilterDesc = "${dateFilter.labelBn} | $filterDesc"

        return AnalysisSummary(
            totalCount = finalFilteredRecords.size,
            filterDescriptionBn = fullFilterDesc,
            dateBreakdown = dateBreakdown,
            codeBreakdown = codeBreakdown,
            records = finalFilteredRecords
        )
    }

    private fun calculateDateBounds(
        filter: DateFilterType,
        specDate: String,
        startD: String,
        endD: String
    ): Pair<String, String> {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()

        return when (filter) {
            DateFilterType.TODAY -> {
                val today = sdf.format(cal.time)
                Pair(today, today)
            }
            DateFilterType.YESTERDAY -> {
                cal.add(Calendar.DAY_OF_YEAR, -1)
                val yesterday = sdf.format(cal.time)
                Pair(yesterday, yesterday)
            }
            DateFilterType.SPECIFIC_DATE -> {
                Pair(specDate, specDate)
            }
            DateFilterType.THIS_MONTH -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                val firstDay = sdf.format(cal.time)
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                val lastDay = sdf.format(cal.time)
                Pair(firstDay, lastDay)
            }
            DateFilterType.LAST_MONTH -> {
                cal.add(Calendar.MONTH, -1)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                val firstDay = sdf.format(cal.time)
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                val lastDay = sdf.format(cal.time)
                Pair(firstDay, lastDay)
            }
            DateFilterType.CUSTOM_RANGE -> {
                val s = if (startD <= endD) startD else endD
                val e = if (startD <= endD) endD else startD
                Pair(s, e)
            }
        }
    }

    private fun formatDateBn(dateStr: String): String {
        return try {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr)
            if (date != null) {
                SimpleDateFormat("dd MMMM, yyyy", Locale("bn", "BD")).format(date)
            } else dateStr
        } catch (e: Exception) {
            dateStr
        }
    }

    private fun getCodeNameFor(code: String): String {
        val preset = allPresetCodes.value.find { it.code.equals(code, ignoreCase = true) }
        return preset?.name ?: "কোড $code"
    }
}
