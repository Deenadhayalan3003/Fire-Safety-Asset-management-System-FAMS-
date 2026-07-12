package com.example.repository

import com.example.models.User
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val currentUser: StateFlow<User?>
    val isFirebaseEnabled: Boolean
    
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(name: String, email: String, password: String, role: String): Result<User>
    suspend fun registerEnterpriseUser(user: User, password: String): Result<User>
    suspend fun updateProfile(mobile: String, department: String, designation: String, plant: String, profilePhotoUrl: String): Result<User>
    suspend fun changePassword(oldPassword: String, newPassword: String): Result<Unit>
    suspend fun isEmployeeIdTaken(employeeId: String): Boolean
    suspend fun isMobileNumberTaken(mobile: String): Boolean
    suspend fun logout(): Result<Unit>
    suspend fun resetPassword(email: String): Result<Unit>
    suspend fun checkAutoLogin(): Result<User?>
}

