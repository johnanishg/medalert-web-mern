package com.medalert.patient.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.medalert.patient.data.model.Medication
import com.medalert.patient.data.model.NotificationTime

@Composable
fun SetTimingDialog(
    medication: Medication,
    onDismiss: () -> Unit,
    onSave: (List<NotificationTime>) -> Unit
) {
    var notificationTimes by remember { 
        mutableStateOf(
            if (medication.timing.isNotEmpty()) {
                medication.timing.map { time ->
                    NotificationTime(time = time, label = "Custom", isActive = true)
                }
            } else {
                listOf(NotificationTime(time = "08:00", label = "Morning", isActive = true))
            }
        )
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Set Medication Timings",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "${medication.name} - ${medication.dosage}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Scheduled Times:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyColumn(
                    modifier = Modifier.height(200.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(notificationTimes) { index, notificationTime ->
                        TimingRow(
                            notificationTime = notificationTime,
                            onTimeChange = { newTime ->
                                notificationTimes = notificationTimes.toMutableList().apply {
                                    this[index] = this[index].copy(time = newTime)
                                }
                            },
                            onRemove = if (notificationTimes.size > 1) {
                                {
                                    notificationTimes = notificationTimes.toMutableList().apply {
                                        removeAt(index)
                                    }
                                }
                            } else null
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                TextButton(
                    onClick = {
                        notificationTimes = notificationTimes + NotificationTime(
                            time = "08:00",
                            label = "Custom",
                            isActive = true
                        )
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Another Time")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = "Note: Reminders will be sent at the scheduled times. Make sure to enable notifications for this app.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(notificationTimes) }
            ) {
                Text("Save Timings")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimingRow(
    notificationTime: NotificationTime,
    onTimeChange: (String) -> Unit,
    onRemove: (() -> Unit)?
) {
    var showTimePicker by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = notificationTime.time,
            onValueChange = onTimeChange,
            label = { Text("Time") },
            leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = "Time") },
            modifier = Modifier.weight(1f),
            singleLine = true,
            placeholder = { Text("HH:MM") }
        )
        
        onRemove?.let { removeAction ->
            IconButton(onClick = removeAction) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}