package com.medalert.patient.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.medalert.patient.data.model.Medication
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MedicationCard(
    medication: Medication,
    onRecordAdherence: (Boolean) -> Unit,
    onEditTiming: () -> Unit,
    onDelete: (() -> Unit)? = null,
    showActions: Boolean = true
) {
    var showAdherenceDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = medication.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = medication.dosage,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                if (showActions) {
                    Row {
                        IconButton(onClick = onEditTiming) {
                            Icon(Icons.Default.Schedule, contentDescription = "Edit Timing")
                        }
                        
                        onDelete?.let { deleteAction ->
                            IconButton(onClick = { showDeleteDialog = true }) {
                                Icon(
                                    Icons.Default.Delete, 
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Medication Details
            MedicationDetailRow("Frequency", medication.frequency)
            MedicationDetailRow("Duration", medication.duration)
            
            if (medication.instructions.isNotEmpty()) {
                MedicationDetailRow("Instructions", medication.instructions)
            }
            
            if (medication.foodTiming.isNotEmpty()) {
                MedicationDetailRow("Food Timing", medication.foodTiming)
            }
            
            if (medication.prescribedBy.isNotEmpty()) {
                MedicationDetailRow("Prescribed by", medication.prescribedBy)
            }
            
            // Timing Information
            if (medication.timing.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Scheduled Times:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    medication.timing.forEach { time ->
                        AssistChip(
                            onClick = { },
                            label = { Text(time) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Schedule,
                                    contentDescription = "Time",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }
            }
            
            // Adherence Information
            if (medication.adherence.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                val adherenceRate = (medication.adherence.count { it.taken }.toFloat() / medication.adherence.size * 100).toInt()
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Adherence Rate",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = when {
                                adherenceRate >= 80 -> Icons.Default.CheckCircle
                                adherenceRate >= 60 -> Icons.Default.Warning
                                else -> Icons.Default.Error
                            },
                            contentDescription = "Adherence Status",
                            tint = when {
                                adherenceRate >= 80 -> MaterialTheme.colorScheme.primary
                                adherenceRate >= 60 -> MaterialTheme.colorScheme.tertiary
                                else -> MaterialTheme.colorScheme.error
                            },
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$adherenceRate%",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                adherenceRate >= 80 -> MaterialTheme.colorScheme.primary
                                adherenceRate >= 60 -> MaterialTheme.colorScheme.tertiary
                                else -> MaterialTheme.colorScheme.error
                            }
                        )
                    }
                }
            }
            
            // Action Buttons
            if (showActions) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showAdherenceDialog = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Record")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Record")
                    }
                    
                    OutlinedButton(
                        onClick = onEditTiming,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = "Timing")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Timing")
                    }
                }
            }
        }
    }
    
    // Adherence Recording Dialog
    if (showAdherenceDialog) {
        AlertDialog(
            onDismissRequest = { showAdherenceDialog = false },
            title = { Text("Record Medicine") },
            text = {
                Column {
                    Text("Did you take ${medication.name}?")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Dosage: ${medication.dosage}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            onRecordAdherence(true)
                            showAdherenceDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Taken")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Taken")
                    }
                    
                    OutlinedButton(
                        onClick = {
                            onRecordAdherence(false)
                            showAdherenceDialog = false
                        }
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Missed")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Missed")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showAdherenceDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Delete Confirmation Dialog
    if (showDeleteDialog && onDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Medication") },
            text = { Text("Are you sure you want to delete ${medication.name}? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun MedicationDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}