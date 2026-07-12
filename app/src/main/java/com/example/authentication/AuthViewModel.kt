package com.example.authentication

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

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    val currentUser = authRepository.currentUser
    val isFirebaseEnabled = authRepository.isFirebaseEnabled

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _loginSuccess = MutableStateFlow(false)
    val loginSuccess: StateFlow<Boolean> = _loginSuccess.asStateFlow()

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _error.value = "Email and Password cannot be empty."
            return
        }
        _loading.value = true
        _error.value = null
        viewModelScope.launch {
            authRepository.login(email, password)
                .onSuccess {
                    _loginSuccess.value = true
                    _loading.value = false
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Authentication failed."
                    _loading.value = false
                }
        }
    }

    fun register(name: String, email: String, password: String, role: String) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            _error.value = "All fields are required."
            return
        }
        _loading.value = true
        _error.value = null
        viewModelScope.launch {
            authRepository.register(name, email, password, role)
                .onSuccess {
                    _loginSuccess.value = true
                    _loading.value = false
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Registration failed."
                    _loading.value = false
                }
        }
    }

    fun resetPassword(email: String, onSuccess: () -> Unit) {
        if (email.isBlank()) {
            _error.value = "Enter email to reset password."
            return
        }
        _loading.value = true
        _error.value = null
        viewModelScope.launch {
            authRepository.resetPassword(email)
                .onSuccess {
                    _loading.value = false
                    onSuccess()
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Password reset failed."
                    _loading.value = false
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _loginSuccess.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }
}
