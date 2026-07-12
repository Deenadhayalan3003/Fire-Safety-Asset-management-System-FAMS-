package com.example.authentication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.models.User
import com.example.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class RegisterState {
    object Idle : RegisterState()
    object Loading : RegisterState()
    data class Success(val user: User) : RegisterState()
    data class Error(val message: String) : RegisterState()
}

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val uiState: StateFlow<RegisterState> = _uiState.asStateFlow()

    fun validatePasswordStrength(password: String): List<String> {
        val errors = mutableListOf<String>()
        if (password.length < 8) {
            errors.add("Minimum 8 characters")
        }
        if (!password.any { it.isUpperCase() }) {
            errors.add("Uppercase letter")
        }
        if (!password.any { it.isLowerCase() }) {
            errors.add("Lowercase letter")
        }
        if (!password.any { it.isDigit() }) {
            errors.add("Numeric digit")
        }
        if (!password.any { !it.isLetterOrDigit() }) {
            errors.add("Special character")
        }
        return errors
    }

    fun register(
        companyCode: String,
        fullName: String,
        employeeId: String,
        department: String,
        designation: String,
        plant: String,
        mobile: String,
        email: String,
        password: String,
        confirmPassword: String,
        role: String = "Safety Officer"
    ) {
        if (companyCode.isBlank() || fullName.isBlank() || employeeId.isBlank() ||
            department.isBlank() || designation.isBlank() || plant.isBlank() ||
            mobile.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()
        ) {
            _uiState.value = RegisterState.Error("All fields are required.")
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = RegisterState.Error("Please enter a valid email address.")
            return
        }

        val passwordErrors = validatePasswordStrength(password)
        if (passwordErrors.isNotEmpty()) {
            _uiState.value = RegisterState.Error("Password must include: ${passwordErrors.joinToString(", ")}")
            return
        }

        if (password != confirmPassword) {
            _uiState.value = RegisterState.Error("Passwords do not match.")
            return
        }

        _uiState.value = RegisterState.Loading
        viewModelScope.launch {
            try {
                if (authRepository.isEmployeeIdTaken(employeeId)) {
                    _uiState.value = RegisterState.Error("Employee ID is already registered.")
                    return@launch
                }
                if (authRepository.isMobileNumberTaken(mobile)) {
                    _uiState.value = RegisterState.Error("Mobile Number is already registered.")
                    return@launch
                }

                val newUser = User(
                    uid = "",
                    fullName = fullName,
                    email = email,
                    role = role,
                    companyCode = companyCode,
                    employeeId = employeeId,
                    mobile = mobile,
                    department = department,
                    designation = designation,
                    plant = plant,
                    status = "Active",
                    profilePhoto = "",
                    createdDate = System.currentTimeMillis(),
                    lastLogin = System.currentTimeMillis(),
                    name = fullName,
                    company = companyCode
                )

                authRepository.registerEnterpriseUser(newUser, password)
                    .onSuccess { user ->
                        _uiState.value = RegisterState.Success(user)
                    }
                    .onFailure { exception ->
                        _uiState.value = RegisterState.Error(exception.message ?: "Registration failed.")
                    }
            } catch (e: Exception) {
                _uiState.value = RegisterState.Error(e.message ?: "An unexpected error occurred.")
            }
        }
    }

    fun resetState() {
        _uiState.value = RegisterState.Idle
    }
}
