package com.medalert.patient.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Patient(
    val _id: String = "",
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val dateOfBirth: String = "",
    val age: Int = 0,
    val gender: String = "",
    val phoneNumber: String = "",
    val emergencyContact: EmergencyContact? = null,
    val medicalHistory: List<MedicalCondition> = emptyList(),
    val allergies: List<String> = emptyList(),
    val currentMedications: List<Medication> = emptyList(),
    val visits: List<Visit> = emptyList(),
    val selectedCaretaker: SelectedCaretaker? = null,
    val caretakerApprovals: List<CaretakerApproval> = emptyList(),
    val isActive: Boolean = true,
    val createdAt: String = "",
    val updatedAt: String = ""
) : Parcelable

@Parcelize
data class EmergencyContact(
    val name: String = "",
    val phone: String = "",
    val relationship: String = ""
) : Parcelable

@Parcelize
data class MedicalCondition(
    val condition: String = "",
    val diagnosisDate: String = "",
    val status: String = ""
) : Parcelable

@Parcelize
data class Medication(
    val _id: String = "",
    val name: String = "",
    val dosage: String = "",
    val frequency: String = "",
    val duration: String = "",
    val instructions: String = "",
    val timing: List<String> = emptyList(),
    val foodTiming: String = "",
    val prescribedBy: String = "",
    val prescribedDate: String = "",
    val prescriptionId: String = "",
    val adherence: List<AdherenceRecord> = emptyList(),
    val lastTaken: String = "",
    val updatedAt: String = "",
    val updatedBy: String = ""
) : Parcelable

@Parcelize
data class AdherenceRecord(
    val timestamp: String = "",
    val taken: Boolean = false,
    val notes: String = "",
    val recordedBy: String = ""
) : Parcelable

@Parcelize
data class Visit(
    val visitDate: String = "",
    val visitType: String = "",
    val doctorId: String = "",
    val doctorName: String = "",
    val diagnosis: String = "",
    val notes: String = "",
    val medicines: List<PrescribedMedicine> = emptyList(),
    val followUpDate: String = "",
    val followUpRequired: Boolean = false,
    val createdAt: String = ""
) : Parcelable

@Parcelize
data class PrescribedMedicine(
    val name: String = "",
    val dosage: String = "",
    val frequency: String = "",
    val duration: String = "",
    val instructions: String = ""
) : Parcelable

@Parcelize
data class SelectedCaretaker(
    val caretakerId: String = "",
    val caretakerUserId: String = "",
    val caretakerName: String = "",
    val caretakerEmail: String = "",
    val assignedAt: String = ""
) : Parcelable

@Parcelize
data class CaretakerApproval(
    val caretakerId: String = "",
    val status: String = "", // pending, approved, rejected
    val requestedAt: String = "",
    val approvedAt: String = "",
    val rejectedAt: String = ""
) : Parcelable