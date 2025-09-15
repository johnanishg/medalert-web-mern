package com.medalert.patient.ui.screens

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
import androidx.hilt.navigation.compose.hiltViewModel
import com.medalert.patient.data.model.Medication
import com.medalert.patient.data.model.NotificationTime
import com.medalert.patient.ui.components.MedicationCard
import com.medalert.patient.ui.components.SetTimingDialog
import com.medalert.patient.viewmodel.PatientViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationsScreen(
    onNavigateBack: () -> Unit,
    patientViewModel: PatientViewModel = hiltViewModel()
) {
    val medications by patientViewModel.medications.collectAsState()
    val uiState by patientViewModel.uiState.collectAsState()
    
    var showTimingDialog by remember { mutableStateOf(false) }
    var selectedMedication by remember { mutableStateOf<Medication?>(null) }
    var selectedMedicationIndex by remember { mutableStateOf(-1) }
    
    // Load data when screen opens
    LaunchedEffect(Unit) {
        patientViewModel.loadPatientData()
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = { Text("My Medications") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(onClick = { patientViewModel.loadPatientData() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                }
            }
        )
        
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (medications.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MedicalServices,
                                    contentDescription = "No medications",
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No medications yet",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Your prescribed medications will appear here",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    itemsIndexed(medications) { index, medication ->
                        MedicationCard(
                            medication = medication,
                            onRecordAdherence = { taken ->
                                patientViewModel.recordAdherence(index, taken)
                            },
                            onEditTiming = {
                                selectedMedication = medication
                                selectedMedicationIndex = index
                                showTimingDialog = true
                            },
                            onDelete = {
                                patientViewModel.deleteMedicine(index)
                            },
                            showActions = true
                        )
                    }
                }
            }
        }
    }
    
    // Set Timing Dialog
    if (showTimingDialog && selectedMedication != null) {
        SetTimingDialog(
            medication = selectedMedication!!,
            onDismiss = { 
                showTimingDialog = false
                selectedMedication = null
                selectedMedicationIndex = -1
            },
            onSave = { notificationTimes ->
                patientViewModel.setMedicineTimings(
                    medicineName = selectedMedication!!.name,
                    dosage = selectedMedication!!.dosage,
                    notificationTimes = notificationTimes,
                    instructions = selectedMedication!!.instructions,
                    foodTiming = selectedMedication!!.foodTiming,
                    frequency = selectedMedication!!.frequency,
                    duration = selectedMedication!!.duration
                )
                showTimingDialog = false
                selectedMedication = null
                selectedMedicationIndex = -1
            }
        )
    }
    
    // Error handling
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // Show error snackbar
            patientViewModel.clearError()
        }
    }
}