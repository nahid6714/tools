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

    /**
     * Creates a new Owner (CodeGroup) and assigns the given codes to it.
     * Codes that are already owned by someone else are SKIPPED (one code = one owner
     * invariant) and returned so the caller can inform the user.
     */
    suspend fun createCodeGroup(groupName: String, description: String, codes: List<String>): Pair<Long, List<String>> {
        val group = CodeGroupEntity(groupName = groupName, description = description)
        val groupId = medicalDao.insertCodeGroup(group)
        val skipped = mutableListOf<String>()
        val toInsert = mutableListOf<CodeGroupItemEntity>()
        for (raw in codes.map { it.trim() }.filter { it.isNotBlank() }.distinct()) {
            val existing = medicalDao.findGroupItemByCode(raw)
            if (existing != null) skipped.add(raw) else toInsert.add(CodeGroupItemEntity(groupId = groupId, code = raw))
        }
        if (toInsert.isNotEmpty()) medicalDao.insertGroupItems(toInsert)
        return Pair(groupId, skipped)
    }

    /**
     * Updates an Owner's name/description and their full set of owned codes.
     * Codes already owned by a DIFFERENT owner are SKIPPED and returned.
     */
    suspend fun updateCodeGroup(groupId: Long, groupName: String, description: String, codes: List<String>): List<String> {
        val group = CodeGroupEntity(id = groupId, groupName = groupName, description = description)
        medicalDao.insertCodeGroup(group)
        medicalDao.deleteGroupItemsByGroupId(groupId)
        val skipped = mutableListOf<String>()
        val toInsert = mutableListOf<CodeGroupItemEntity>()
        for (raw in codes.map { it.trim() }.filter { it.isNotBlank() }.distinct()) {
            val existing = medicalDao.findGroupItemByCode(raw)
            if (existing != null && existing.groupId != groupId) {
                skipped.add(raw)
            } else {
                toInsert.add(CodeGroupItemEntity(groupId = groupId, code = raw))
            }
        }
        if (toInsert.isNotEmpty()) medicalDao.insertGroupItems(toInsert)
        return skipped
    }

    suspend fun deleteCodeGroup(groupId: Long) {
        medicalDao.deleteGroupItemsByGroupId(groupId)
        medicalDao.deleteCodeGroup(groupId)
    }

    // ---- Ownership (ONE CODE = ONE OWNER) ----

    /** Returns the Owner currently assigned to [code], or null if unassigned. */
    suspend fun findOwnerForCode(code: String): CodeGroupEntity? {
        val trimmed = code.trim()
        if (trimmed.isBlank()) return null
        val item = medicalDao.findGroupItemByCode(trimmed) ?: return null
        return allCodeGroups.first().find { it.id == item.groupId }
    }

    /**
     * Assigns [code] to [ownerId]. Returns true if the code is now (or already was)
     * owned by [ownerId]; returns false if the code is already owned by someone else
     * (assignment refused to preserve the one-owner invariant).
     */
    suspend fun assignCodeToOwner(ownerId: Long, code: String): Boolean {
        val trimmed = code.trim()
        if (trimmed.isBlank()) return false
        val existing = medicalDao.findGroupItemByCode(trimmed)
        if (existing != null) return existing.groupId == ownerId
        medicalDao.insertGroupItems(listOf(CodeGroupItemEntity(groupId = ownerId, code = trimmed)))
        return true
    }

    /** Moves [code]'s ownership to [newOwnerId], replacing any previous owner. */
    suspend fun reassignCodeOwner(code: String, newOwnerId: Long) {
        val trimmed = code.trim()
        medicalDao.deleteGroupItemByCode(trimmed)
        medicalDao.insertGroupItems(listOf(CodeGroupItemEntity(groupId = newOwnerId, code = trimmed)))
    }

    /** Makes [code] "Owner Not Assigned" again. */
    suspend fun removeCodeOwnership(code: String) {
        medicalDao.deleteGroupItemByCode(code.trim())
    }

    /** Creates a new Owner. Fails if the (trimmed, case-insensitive) name already exists. */
    suspend fun createOwner(name: String): Result<CodeGroupEntity> {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return Result.failure(IllegalArgumentException("অনারের নাম খালি রাখা যাবে না।"))
        val exists = allCodeGroups.first().any { it.groupName.trim().equals(trimmed, ignoreCase = true) }
        if (exists) return Result.failure(IllegalStateException("এই নামে ইতিমধ্যে একজন অনার আছে।"))
        val id = medicalDao.insertCodeGroup(CodeGroupEntity(groupName = trimmed))
        return Result.success(CodeGroupEntity(id = id, groupName = trimmed))
    }

    /** Renames an existing Owner. Fails if another owner already has that name. */
    suspend fun renameOwner(ownerId: Long, newName: String): Result<Unit> {
        val trimmed = newName.trim()
        if (trimmed.isBlank()) return Result.failure(IllegalArgumentException("অনারের নাম খালি রাখা যাবে না।"))
        val current = allCodeGroups.first()
        val duplicate = current.any { it.id != ownerId && it.groupName.trim().equals(trimmed, ignoreCase = true) }
        if (duplicate) return Result.failure(IllegalStateException("এই নামে ইতিমধ্যে একজন অনার আছে।"))
        val existing = current.find { it.id == ownerId } ?: return Result.failure(IllegalStateException("অনার খুঁজে পাওয়া যায়নি।"))
        medicalDao.insertCodeGroup(existing.copy(groupName = trimmed))
        return Result.success(Unit)
    }

    /** Deletes an Owner only if they own zero codes (ownership must be transferred first). */
    suspend fun deleteOwnerSafely(ownerId: Long): Result<Unit> {
        val hasCodes = allGroupItems.first().any { it.groupId == ownerId }
        if (hasCodes) {
            return Result.failure(IllegalStateException("এই অনারের অধীনে কোড আছে। আগে কোডগুলোর অনার পরিবর্তন করুন।"))
        }
        medicalDao.deleteCodeGroup(ownerId)
        return Result.success(Unit)
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
                codes = listOf("CBC")
            )
        }
    }
}
