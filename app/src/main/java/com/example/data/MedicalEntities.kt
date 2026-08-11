package com.example.data

import androidx.room.Entity
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

@Entity(tableName = "code_groups")
data class CodeGroupEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val groupName: String, // e.g. "Nahid"
    val description: String = ""
)

@Entity(tableName = "code_group_items")
data class CodeGroupItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val groupId: Long, // FK to CodeGroupEntity
    val code: String // e.g. "101"
)

@Entity(tableName = "preset_medical_codes")
data class PresetMedicalCodeEntity(
    @PrimaryKey val code: String,
    val name: String = "",
    val category: String = "General"
)
