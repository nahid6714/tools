package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull

class MedicalRepository(private val medicalDao: MedicalDao) {

    val allRecords: Flow<List<MedicalRecordEntity>> = medicalDao.getAllRecords()
    val allCodeGroups: Flow<List<CodeGroupEntity>> = medicalDao.getAllCodeGroups()
    val allGroupItems: Flow<List<CodeGroupItemEntity>> = medicalDao.getAllGroupItems()
    val allPresetCodes: Flow<List<PresetMedicalCodeEntity>> = medicalDao.getAllPresetCodes()

    suspend fun saveRecords(records: List<MedicalRecordEntity>) {
        medicalDao.insertRecords(records)
    }

    suspend fun saveRecord(record: MedicalRecordEntity) {
        medicalDao.insertRecord(record)
    }

    suspend fun deleteRecord(id: Long) {
        medicalDao.deleteRecordById(id)
    }

    suspend fun deleteRecordsByDate(date: String) {
        medicalDao.deleteRecordsByDate(date)
    }

    suspend fun createCodeGroup(groupName: String, description: String, codes: List<String>): Long {
        val group = CodeGroupEntity(groupName = groupName, description = description)
        val groupId = medicalDao.insertCodeGroup(group)
        val items = codes.map { CodeGroupItemEntity(groupId = groupId, code = it) }
        medicalDao.insertGroupItems(items)
        return groupId
    }

    suspend fun updateCodeGroup(groupId: Long, groupName: String, description: String, codes: List<String>) {
        val group = CodeGroupEntity(id = groupId, groupName = groupName, description = description)
        medicalDao.insertCodeGroup(group)
        medicalDao.deleteGroupItemsByGroupId(groupId)
        val items = codes.map { CodeGroupItemEntity(groupId = groupId, code = it) }
        medicalDao.insertGroupItems(items)
    }

    suspend fun deleteCodeGroup(groupId: Long) {
        medicalDao.deleteGroupItemsByGroupId(groupId)
        medicalDao.deleteCodeGroup(groupId)
    }

    suspend fun savePresetCode(code: String, name: String = "", category: String = "General") {
        medicalDao.insertPresetCode(PresetMedicalCodeEntity(code = code.trim(), name = name, category = category))
    }

    suspend fun deletePresetCode(code: String) {
        medicalDao.deletePresetCode(code)
    }

    suspend fun seedDefaultsIfEmpty() {
        val existingPresets = allPresetCodes.firstOrNull() ?: emptyList()
        if (existingPresets.isEmpty()) {
            val defaultCodes = listOf(
                PresetMedicalCodeEntity("101", "CBC / Blood Test", "Pathology"),
                PresetMedicalCodeEntity("102", "Urine R/E", "Pathology"),
                PresetMedicalCodeEntity("103", "Stool R/E", "Pathology"),
                PresetMedicalCodeEntity("104", "Blood Sugar / RBS", "Biochemistry"),
                PresetMedicalCodeEntity("105", "Lipid Profile", "Biochemistry"),
                PresetMedicalCodeEntity("106", "USG Whole Abdomen", "Radiology"),
                PresetMedicalCodeEntity("107", "X-Ray Chest PB", "Radiology"),
                PresetMedicalCodeEntity("108", "ECG", "Cardiology"),
                PresetMedicalCodeEntity("109", "ECHO", "Cardiology"),
                PresetMedicalCodeEntity("110", "Serum Creatinine", "Biochemistry"),
                PresetMedicalCodeEntity("CBC", "Complete Blood Count", "Pathology"),
                PresetMedicalCodeEntity("USG", "Ultrasonogram", "Radiology"),
                PresetMedicalCodeEntity("XRAY", "X-Ray", "Radiology")
            )
            medicalDao.insertPresetCodes(defaultCodes)
        }

        val existingGroups = allCodeGroups.firstOrNull() ?: emptyList()
        if (existingGroups.isEmpty()) {
            // Seed sample "Nahid" group requested in prompt!
            createCodeGroup(
                groupName = "Nahid",
                description = "নাহিদের বরাদ্দকৃত কোডসমূহ",
                codes = listOf("101", "102", "103", "104", "105")
            )
            createCodeGroup(
                groupName = "Pathology Group",
                description = "প্যাথোলজি টেস্টসমূহ",
                codes = listOf("101", "102", "103", "CBC")
            )
        }
    }
}
