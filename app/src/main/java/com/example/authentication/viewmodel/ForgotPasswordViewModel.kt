package com.example.authentication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ForgotPasswordState {
    object Idle : ForgotPasswordState()
    object Loading : ForgotPasswordState()
    object Success : ForgotPasswordState()
    data class Error(val message: String) : ForgotPasswordState()
}

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ForgotPasswordState>(ForgotPasswordState.Idle)
    val uiState: StateFlow<ForgotPasswordState> = _uiState.asStateFlow()

    fun resetPassword(email: String) {
        if (email.isBlank()) {
            _uiState.value = ForgotPasswordState.Error("Email address cannot be empty.")
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = ForgotPasswordState.Error("Please enter a valid email address.")
            return
        }

        _uiState.value = ForgotPasswordState.Loading
        viewModelScope.launch {
            authRepository.resetPassword(email)
                .onSuccess {
                    _uiState.value = ForgotPasswordState.Success
                }
                .onFailure { exception ->
                    _uiState.value = ForgotPasswordState.Error(exception.message ?: "Failed to send reset link.")
                }
        }
    }

    fun resetState() {
        _uiState.value = ForgotPasswordState.Idle
    }
}
