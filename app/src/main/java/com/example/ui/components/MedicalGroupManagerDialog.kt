package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CodeGroupEntity
import com.example.data.CodeGroupItemEntity
import com.example.data.PresetMedicalCodeEntity
import com.example.ui.theme.DarkForestGreen
import com.example.ui.theme.ForestGreenText
import com.example.util.BengaliUtils

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MedicalGroupManagerDialog(
    groups: List<CodeGroupEntity>,
    groupItems: List<CodeGroupItemEntity>,
    presetCodes: List<PresetMedicalCodeEntity>,
    onDismiss: () -> Unit,
    onCreateGroup: (name: String, description: String, codes: List<String>) -> Unit,
    onUpdateGroup: (id: Long, name: String, description: String, codes: List<String>) -> Unit,
    onDeleteGroup: (id: Long, name: String) -> Unit,
    onAddPresetCode: (code: String, name: String, category: String) -> Unit,
    onDeletePresetCode: (code: String) -> Unit = {}
) {
    var isAddingNewGroup by remember { mutableStateOf(false) }
    var editingGroupId by remember { mutableStateOf<Long?>(null) }

    var groupNameInput by remember { mutableStateOf("") }
    var groupDescInput by remember { mutableStateOf("") }
    var selectedCodes by remember { mutableStateOf<Set<String>>(emptySet()) }
    var customCodeInput by remember { mutableStateOf("") }

    // Preset Code Addition State
    var showAddCodeForm by remember { mutableStateOf(false) }
    var newCodeInput by remember { mutableStateOf("") }
    var newCodeNameInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = DarkForestGreen,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "কোড গ্রুপ ও ফোল্ডার ম্যানেজার",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "একাধিক কোডকে একটি গ্রুপে রেখে একসাথে কাজ হিসাব করুন",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                if (!isAddingNewGroup && editingGroupId == null) {
                    // List Existing Groups
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "বিদ্যমান গ্রুপসমূহ (${BengaliUtils.toBengaliDigits(groups.size.toString())} টি)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = ForestGreenText
                        )
                        Button(
                            onClick = {
                                groupNameInput = ""
                                groupDescInput = ""
                                selectedCodes = emptySet()
                                isAddingNewGroup = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkForestGreen),
                            modifier = Modifier.testTag("btn_create_new_group")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("নতুন গ্রুপ তৈরি", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (groups.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "কোনো কোড গ্রুপ তৈরি করা নেই। নতুন গ্রুপ তৈরি করতে উপরে চাপুন।",
                                fontSize = 13.sp,
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        groups.forEach { group ->
                            val codesForGroup = groupItems.filter { it.groupId == group.id }.map { it.code }
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .background(DarkForestGreen, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = group.groupName.take(1).uppercase(),
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = group.groupName,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                if (group.description.isNotBlank()) {
                                                    Text(
                                                        text = group.description,
                                                        fontSize = 12.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }

                                        Row {
                                            IconButton(
                                                onClick = {
                                                    editingGroupId = group.id
                                                    groupNameInput = group.groupName
                                                    groupDescInput = group.description
                                                    selectedCodes = codesForGroup.toSet()
                                                }
                                            ) {
                                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = DarkForestGreen)
                                            }
                                            IconButton(
                                                onClick = { onDeleteGroup(group.id, group.groupName) }
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        codesForGroup.forEach { c ->
                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = DarkForestGreen.copy(alpha = 0.12f),
                                                modifier = Modifier.padding(2.dp)
                                            ) {
                                                Text(
                                                    text = c,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = DarkForestGreen,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(12.dp))

                    // Preset Codes Section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "প্রিসেট মেডিকেল কোডসমূহ (${presetCodes.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = ForestGreenText
                        )
                        IconButton(onClick = { showAddCodeForm = !showAddCodeForm }) {
                            Icon(if (showAddCodeForm) Icons.Default.Close else Icons.Default.Add, contentDescription = null, tint = DarkForestGreen)
                        }
                    }

                    if (showAddCodeForm) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.padding(vertical = 6.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("নতুন টেস্ট কোড যোগ করুন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = newCodeInput,
                                        onValueChange = { newCodeInput = it },
                                        label = { Text("কোড (যেমন 101)", fontSize = 11.sp) },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = newCodeNameInput,
                                        onValueChange = { newCodeNameInput = it },
                                        label = { Text("নাম (যেমন CBC)", fontSize = 11.sp) },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                }
                                Button(
                                    onClick = {
                                        if (newCodeInput.isNotBlank()) {
                                            onAddPresetCode(newCodeInput, newCodeNameInput, "Custom")
                                            newCodeInput = ""
                                            newCodeNameInput = ""
                                            showAddCodeForm = false
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = DarkForestGreen),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp)
                                ) {
                                    Text("সেভ করুন", fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 6.dp)
                    ) {
                        presetCodes.forEach { p ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.padding(2.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(start = 8.dp, end = 2.dp, top = 2.dp, bottom = 2.dp)
                                ) {
                                    Text(
                                        text = "${p.code}${if (p.name.isNotBlank()) " (${p.name})" else ""}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    IconButton(
                                        onClick = { onDeletePresetCode(p.code) },
                                        modifier = Modifier
                                            .size(20.dp)
                                            .padding(start = 2.dp)
                                            .testTag("btn_delete_preset_${p.code}")
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "কোড মুছুন",
                                            tint = Color(0xFFD32F2F),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                } else {
                    // Create or Edit Group Form
                    Text(
                        text = if (isAddingNewGroup) "নতুন কোড গ্রুপ তৈরি করুন" else "কোড গ্রুপ আপডেট করুন",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = DarkForestGreen
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = groupNameInput,
                        onValueChange = { groupNameInput = it },
                        label = { Text("গ্রুপের নাম (যেমন: Nahid / Pathology)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = groupDescInput,
                        onValueChange = { groupDescInput = it },
                        label = { Text("বিবরণ (ঐচ্ছিক)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "এই গ্রুপে যেসব কোড অন্তর্ভুক্ত থাকবে (Select Codes):",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Preset Code Selection Chips
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        presetCodes.forEach { p ->
                            val isSelected = selectedCodes.contains(p.code)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedCodes = if (isSelected) {
                                        selectedCodes - p.code
                                    } else {
                                        selectedCodes + p.code
                                    }
                                },
                                label = { Text(p.code, fontSize = 12.sp) },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = DarkForestGreen,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Add Custom Code to Group
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = customCodeInput,
                            onValueChange = { customCodeInput = it },
                            label = { Text("অন্যান্য কোড লিখুন", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Button(
                            onClick = {
                                if (customCodeInput.isNotBlank()) {
                                    selectedCodes = selectedCodes + customCodeInput.trim()
                                    customCodeInput = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkForestGreen)
                        ) {
                            Text("যোগ", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "নির্বাচিত কোডসমূহ (${selectedCodes.size}টি): ${selectedCodes.joinToString(", ")}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreenText
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = {
                                isAddingNewGroup = false
                                editingGroupId = null
                            }
                        ) {
                            Text("বাতিল")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (groupNameInput.isNotBlank()) {
                                    if (isAddingNewGroup) {
                                        onCreateGroup(groupNameInput, groupDescInput, selectedCodes.toList())
                                    } else if (editingGroupId != null) {
                                        onUpdateGroup(editingGroupId!!, groupNameInput, groupDescInput, selectedCodes.toList())
                                    }
                                    isAddingNewGroup = false
                                    editingGroupId = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkForestGreen)
                        ) {
                            Text("সংরক্ষণ করুন")
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (!isAddingNewGroup && editingGroupId == null) {
                TextButton(onClick = onDismiss) {
                    Text("বন্ধ করুন", fontWeight = FontWeight.Bold)
                }
            }
        }
    )
}
