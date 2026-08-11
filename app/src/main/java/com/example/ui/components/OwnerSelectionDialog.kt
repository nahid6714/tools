package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CodeGroupEntity
import com.example.ui.theme.DarkForestGreen
import com.example.ui.theme.ForestGreenText

/**
 * "এই Code কার অধীনে?" dialog shown the first time a brand-new (unowned) code is
 * added, either from the main daily-entry "+" form or from the Preset Code Manager.
 * Lets the user pick an existing Owner, or create a brand-new Owner inline
 * without leaving the dialog.
 */
@Composable
fun OwnerSelectionDialog(
    patientId: String,
    code: String,
    owners: List<CodeGroupEntity>,
    onDismiss: () -> Unit,
    onConfirm: (ownerId: Long) -> Unit,
    onCreateOwner: (name: String) -> Unit
) {
    var selectedOwnerId by remember { mutableStateOf<Long?>(null) }
    var dropdownExpanded by remember { mutableStateOf(false) }
    var showAddOwnerField by remember { mutableStateOf(false) }
    var newOwnerName by remember { mutableStateOf("") }
    var pendingNewOwnerName by remember { mutableStateOf<String?>(null) }

    // When a newly-created owner shows up in the list, auto-select it.
    LaunchedEffect(owners) {
        val pending = pendingNewOwnerName
        if (pending != null) {
            val match = owners.find { it.groupName.trim().equals(pending, ignoreCase = true) }
            if (match != null) {
                selectedOwnerId = match.id
                showAddOwnerField = false
                newOwnerName = ""
                pendingNewOwnerName = null
            }
        }
    }

    val selectedOwnerName = owners.find { it.id == selectedOwnerId }?.groupName

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = null, tint = DarkForestGreen)
                Spacer(modifier = Modifier.width(8.dp))
                Text("এই কোড কার অধীনে থাকবে?", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        if (patientId.isNotBlank()) {
                            Text("আইডি: $patientId", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("কোড: $code", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = ForestGreenText)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "এটি একটি নতুন কোড। এই কোডের একজন নির্দিষ্ট অনার নির্বাচন করুন — পরে এই কোডের অনার আর কেউ পরিবর্তন না করলে বদলাবে না।",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                Text("অনার নির্বাচন করুন", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))

                Box {
                    OutlinedButton(
                        onClick = { dropdownExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(selectedOwnerName ?: "-- অনার নির্বাচন করুন --")
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }
                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        owners.forEach { owner ->
                            DropdownMenuItem(
                                text = { Text(owner.groupName) },
                                onClick = {
                                    selectedOwnerId = owner.id
                                    dropdownExpanded = false
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.height(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("নতুন অনার যোগ করুন", color = DarkForestGreen, fontWeight = FontWeight.Bold)
                                }
                            },
                            onClick = {
                                dropdownExpanded = false
                                showAddOwnerField = true
                            }
                        )
                    }
                }

                if (showAddOwnerField) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("নতুন অনারের নাম", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = newOwnerName,
                            onValueChange = { newOwnerName = it },
                            placeholder = { Text("যেমন: রাহাত", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (newOwnerName.isNotBlank()) {
                                    pendingNewOwnerName = newOwnerName.trim()
                                    onCreateOwner(newOwnerName.trim())
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkForestGreen)
                        ) {
                            Text("যোগ করুন", fontSize = 12.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { selectedOwnerId?.let(onConfirm) },
                enabled = selectedOwnerId != null,
                colors = ButtonDefaults.buttonColors(containerColor = DarkForestGreen)
            ) {
                Text("সেভ করুন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল")
            }
        }
    )
}
