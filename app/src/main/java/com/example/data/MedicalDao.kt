package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicalDao {
    @Query("SELECT * FROM medical_records ORDER BY date DESC, id DESC")
    fun getAllRecords(): Flow<List<MedicalRecordEntity>>

    @Query("SELECT * FROM medical_records WHERE date = :date ORDER BY id ASC")
    fun getRecordsByDate(date: String): Flow<List<MedicalRecordEntity>>

    @Query("SELECT * FROM medical_records WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC, id ASC")
    fun getRecordsBetweenDates(startDate: String, endDate: String): Flow<List<MedicalRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: MedicalRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecords(records: List<MedicalRecordEntity>)

    @Query("DELETE FROM medical_records WHERE id = :id")
    suspend fun deleteRecordById(id: Long)

    @Query("DELETE FROM medical_records WHERE date = :date")
    suspend fun deleteRecordsByDate(date: String)

    @Query("DELETE FROM medical_records WHERE id IN (:ids)")
    suspend fun deleteRecordsByIds(ids: List<Long>)

    // Code Groups
    @Query("SELECT * FROM code_groups ORDER BY groupName ASC")
    fun getAllCodeGroups(): Flow<List<CodeGroupEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCodeGroup(group: CodeGroupEntity): Long

    @Query("DELETE FROM code_groups WHERE id = :groupId")
    suspend fun deleteCodeGroup(groupId: Long)

    @Query("SELECT * FROM code_group_items WHERE groupId = :groupId")
    fun getGroupItems(groupId: Long): Flow<List<CodeGroupItemEntity>>

    @Query("SELECT * FROM code_group_items")
    fun getAllGroupItems(): Flow<List<CodeGroupItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroupItems(items: List<CodeGroupItemEntity>)

    @Query("DELETE FROM code_group_items WHERE groupId = :groupId")
    suspend fun deleteGroupItemsByGroupId(groupId: Long)

    // Ownership helpers (ONE CODE = ONE OWNER, case-insensitive)
    @Query("SELECT * FROM code_group_items WHERE code = :code COLLATE NOCASE LIMIT 1")
    suspend fun findGroupItemByCode(code: String): CodeGroupItemEntity?

    @Query("DELETE FROM code_group_items WHERE code = :code COLLATE NOCASE")
    suspend fun deleteGroupItemByCode(code: String)

    // Preset Codes
    @Query("SELECT * FROM preset_medical_codes ORDER BY code ASC")
    fun getAllPresetCodes(): Flow<List<PresetMedicalCodeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPresetCodes(codes: List<PresetMedicalCodeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPresetCode(code: PresetMedicalCodeEntity)

    @Query("DELETE FROM preset_medical_codes WHERE code = :code")
    suspend fun deletePresetCode(code: String)
}
