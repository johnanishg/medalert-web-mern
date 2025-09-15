package com.medalert.patient.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medalert.patient.data.model.*
import com.medalert.patient.data.repository.PatientRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PatientViewModel @Inject constructor(
    private val repository: PatientRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(PatientUiState())
    val uiState: StateFlow<PatientUiState> = _uiState.asStateFlow()
    
    private val _patient = MutableStateFlow<Patient?>(null)
    val patient: StateFlow<Patient?> = _patient.asStateFlow()
    
    private val _medications = MutableStateFlow<List<Medication>>(emptyList())
    val medications: StateFlow<List<Medication>> = _medications.asStateFlow()
    
    private val _notifications = MutableStateFlow<List<MedicineNotification>>(emptyList())
    val notifications: StateFlow<List<MedicineNotification>> = _notifications.asStateFlow()
    
    private val _caretakers = MutableStateFlow<List<Caretaker>>(emptyList())
    val caretakers: StateFlow<List<Caretaker>> = _caretakers.asStateFlow()
    
    init {
        loadPatientData()
    }
    
    fun loadPatientData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            repository.getPatientProfile()
                .onSuccess { patient ->
                    _patient.value = patient
                    _medications.value = patient.currentMedications
                    _uiState.value = _uiState.value.copy(isLoading = false, error = null)
                    
                    // Load notifications
                    loadMedicineNotifications()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
        }
    }
    
    fun loadMedicineNotifications() {
        viewModelScope.launch {
            repository.getMedicineNotifications()
                .onSuccess { notifications ->
                    _notifications.value = notifications
                }
                .onFailure { error ->
                    // Don't update UI state for notification errors
                    println("Failed to load notifications: ${error.message}")
                }
        }
    }
    
    fun updateMedicine(medicineIndex: Int, updateData: Map<String, Any>) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            repository.updateMedicine(medicineIndex, updateData)
                .onSuccess {
                    loadPatientData() // Refresh data
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
        }
    }
    
    fun deleteMedicine(medicineIndex: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            repository.deleteMedicine(medicineIndex)
                .onSuccess {
                    loadPatientData() // Refresh data
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
        }
    }
    
    fun setMedicineTimings(
        medicineName: String,
        dosage: String,
        notificationTimes: List<NotificationTime>,
        instructions: String = "",
        foodTiming: String = "",
        frequency: String = "",
        duration: String = ""
    ) {
        viewModelScope.launch {
            val request = SetTimingsRequest(
                medicineName = medicineName,
                dosage = dosage,
                notificationTimes = notificationTimes,
                instructions = instructions,
                foodTiming = foodTiming,
                frequency = frequency,
                duration = duration
            )
            
            repository.setMedicineTimings(request)
                .onSuccess {
                    loadMedicineNotifications()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(error = error.message)
                }
        }
    }
    
    fun recordAdherence(medicineIndex: Int, taken: Boolean, notes: String = "") {
        viewModelScope.launch {
            repository.recordAdherence(medicineIndex, taken, notes)
                .onSuccess {
                    loadPatientData() // Refresh to get updated adherence data
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(error = error.message)
                }
        }
    }
    
    fun loadAvailableCaretakers(search: String? = null) {
        viewModelScope.launch {
            repository.getAvailableCaretakers(search)
                .onSuccess { caretakers ->
                    _caretakers.value = caretakers
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(error = error.message)
                }
        }
    }
    
    fun assignCaretaker(caretakerUserId: String) {
        viewModelScope.launch {
            repository.assignCaretaker(caretakerUserId)
                .onSuccess {
                    loadPatientData() // Refresh to get updated caretaker info
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(error = error.message)
                }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class PatientUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)