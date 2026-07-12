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

sealed class EditProfileState {
    object Idle : EditProfileState()
    object Loading : EditProfileState()
    data class Success(val user: User) : EditProfileState()
    data class Error(val message: String) : EditProfileState()
}

sealed class ChangePasswordState {
    object Idle : ChangePasswordState()
    object Loading : ChangePasswordState()
    object Success : ChangePasswordState()
    data class Error(val message: String) : ChangePasswordState()
}

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    val currentUser: StateFlow<User?> = authRepository.currentUser

    private val _editState = MutableStateFlow<EditProfileState>(EditProfileState.Idle)
    val editState: StateFlow<EditProfileState> = _editState.asStateFlow()

    private val _passwordState = MutableStateFlow<ChangePasswordState>(ChangePasswordState.Idle)
    val passwordState: StateFlow<ChangePasswordState> = _passwordState.asStateFlow()

    fun updateProfile(
        mobile: String,
        department: String,
        designation: String,
        plant: String,
        profilePhotoUrl: String
    ) {
        if (mobile.isBlank() || department.isBlank() || designation.isBlank() || plant.isBlank()) {
            _editState.value = EditProfileState.Error("All profile fields must be filled.")
            return
        }

        _editState.value = EditProfileState.Loading
        viewModelScope.launch {
            authRepository.updateProfile(mobile, department, designation, plant, profilePhotoUrl)
                .onSuccess { updatedUser ->
                    _editState.value = EditProfileState.Success(updatedUser)
                }
                .onFailure { exception ->
                    _editState.value = EditProfileState.Error(exception.message ?: "Failed to update profile.")
                }
        }
    }

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

    fun changePassword(oldPass: String, newPass: String, confirmPass: String) {
        if (oldPass.isBlank() || newPass.isBlank() || confirmPass.isBlank()) {
            _passwordState.value = ChangePasswordState.Error("All password fields are required.")
            return
        }

        val errors = validatePasswordStrength(newPass)
        if (errors.isNotEmpty()) {
            _passwordState.value = ChangePasswordState.Error("New password must include: ${errors.joinToString(", ")}")
            return
        }

        if (newPass != confirmPass) {
            _passwordState.value = ChangePasswordState.Error("New passwords do not match.")
            return
        }

        _passwordState.value = ChangePasswordState.Loading
        viewModelScope.launch {
            authRepository.changePassword(oldPass, newPass)
                .onSuccess {
                    _passwordState.value = ChangePasswordState.Success
                }
                .onFailure { exception ->
                    _passwordState.value = ChangePasswordState.Error(exception.message ?: "Password change failed. Please verify your old password.")
                }
        }
    }

    fun resetStates() {
        _editState.value = EditProfileState.Idle
        _passwordState.value = ChangePasswordState.Idle
    }
}
