package com.medalert.patient.data.repository

import com.medalert.patient.data.api.ApiService
import com.medalert.patient.data.model.*
import com.medalert.patient.data.local.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PatientRepository @Inject constructor(
    private val apiService: ApiService,
    private val userPreferences: UserPreferences
) {
    
    // Authentication
    suspend fun login(email: String, password: String): Result<AuthResponse> {
        return try {
            val response = apiService.login(LoginRequest(email, password, "patient"))
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                // Save token and user data
                userPreferences.saveAuthToken(authResponse.token)
                userPreferences.saveUserData(authResponse.user)
                Result.success(authResponse)
            } else {
                Result.failure(Exception(response.message() ?: "Login failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun register(request: RegisterRequest): Result<AuthResponse> {
        return try {
            val response = apiService.register(request)
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                // Save token and user data
                userPreferences.saveAuthToken(authResponse.token)
                userPreferences.saveUserData(authResponse.user)
                Result.success(authResponse)
            } else {
                Result.failure(Exception(response.message() ?: "Registration failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun logout() {
        userPreferences.clearAuthData()
    }
    
    // Patient Profile
    suspend fun getPatientProfile(): Result<Patient> {
        return try {
            val user = userPreferences.getUserData().first()
            if (user != null) {
                val response = apiService.getPatientProfile(user._id)
                if (response.isSuccessful && response.body()?.data != null) {
                    Result.success(response.body()!!.data!!)
                } else {
                    Result.failure(Exception(response.message() ?: "Failed to get profile"))
                }
            } else {
                Result.failure(Exception("User not logged in"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updatePatientProfile(patient: Patient): Result<Patient> {
        return try {
            val response = apiService.updatePatientProfile(patient._id, patient)
            if (response.isSuccessful && response.body()?.data != null) {
                // Update local user data
                userPreferences.saveUserData(response.body()!!.data!!)
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.message() ?: "Failed to update profile"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Medicine Management
    suspend fun updateMedicine(medicineIndex: Int, updateData: Map<String, Any>): Result<Medication> {
        return try {
            val response = apiService.updateMedicine(medicineIndex, updateData)
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.message() ?: "Failed to update medicine"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteMedicine(medicineIndex: Int): Result<Boolean> {
        return try {
            val response = apiService.deleteMedicine(medicineIndex)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception(response.message() ?: "Failed to delete medicine"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Medicine Notifications
    suspend fun setMedicineTimings(request: SetTimingsRequest): Result<MedicineNotification> {
        return try {
            val response = apiService.setMedicineTimings(request)
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.message() ?: "Failed to set timings"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getMedicineNotifications(): Result<List<MedicineNotification>> {
        return try {
            val user = userPreferences.getUserData().first()
            if (user != null) {
                val response = apiService.getMedicineNotifications(user._id)
                if (response.isSuccessful && response.body()?.data != null) {
                    Result.success(response.body()!!.data!!)
                } else {
                    Result.failure(Exception(response.message() ?: "Failed to get notifications"))
                }
            } else {
                Result.failure(Exception("User not logged in"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Adherence Tracking
    suspend fun recordAdherence(
        medicineIndex: Int,
        taken: Boolean,
        notes: String = ""
    ): Result<Boolean> {
        return try {
            val user = userPreferences.getUserData().first()
            if (user != null) {
                val adherenceData = mapOf(
                    "taken" to taken,
                    "timestamp" to System.currentTimeMillis(),
                    "notes" to notes
                )
                val response = apiService.recordAdherence(user._id, medicineIndex, adherenceData)
                if (response.isSuccessful) {
                    Result.success(true)
                } else {
                    Result.failure(Exception(response.message() ?: "Failed to record adherence"))
                }
            } else {
                Result.failure(Exception("User not logged in"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Caretaker Management
    suspend fun getAvailableCaretakers(search: String? = null): Result<List<Caretaker>> {
        return try {
            val response = apiService.getAvailableCaretakers(search)
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.message() ?: "Failed to get caretakers"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun assignCaretaker(caretakerUserId: String): Result<Boolean> {
        return try {
            val request = mapOf("caretakerUserId" to caretakerUserId)
            val response = apiService.assignCaretaker(request)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception(response.message() ?: "Failed to assign caretaker"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // User Data
    fun getUserData(): Flow<Patient?> = userPreferences.getUserData()
    fun getAuthToken(): Flow<String?> = userPreferences.getAuthToken()
    
    suspend fun isLoggedIn(): Boolean {
        return userPreferences.getAuthToken().first() != null
    }
}