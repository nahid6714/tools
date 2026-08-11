package com.example.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "medical_records")
data class MedicalRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // YYYY-MM-DD
    val patientId: String, // e.g. AB2608001
    val code: String, // e.g. 101, CBC, USG
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = ""
)

/**
 * A CodeGroup represents an "Owner" (person) that a set of Codes is assigned to.
 * ONE CODE = ONE OWNER is enforced via the unique, case-insensitive index on
 * CodeGroupItemEntity.code below, plus application-level checks in MedicalRepository.
 */
@Entity(tableName = "code_groups")
data class CodeGroupEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val groupName: String, // Owner's name, e.g. "রাহাত"
    val description: String = ""
)

@Entity(
    tableName = "code_group_items",
    indices = [Index(value = ["code"], unique = true)]
)
data class CodeGroupItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val groupId: Long, // FK to CodeGroupEntity (the Owner)
    @ColumnInfo(collate = ColumnInfo.NOCASE) val code: String // e.g. "101" / "MD-01" (case-insensitive unique)
)

@Entity(tableName = "preset_medical_codes")
data class PresetMedicalCodeEntity(
    @PrimaryKey val code: String,
    val name: String = "",
    val category: String = "General"
)
