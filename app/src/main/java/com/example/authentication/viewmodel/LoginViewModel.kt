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

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val user: User) : LoginState()
    data class Error(val message: String) : LoginState()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginState>(LoginState.Idle)
    val uiState: StateFlow<LoginState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = LoginState.Error("Email and Password cannot be empty.")
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = LoginState.Error("Please enter a valid email address.")
            return
        }
        if (password.length < 8) {
            _uiState.value = LoginState.Error("Password must be at least 8 characters.")
            return
        }

        _uiState.value = LoginState.Loading
        viewModelScope.launch {
            authRepository.login(email, password)
                .onSuccess { user ->
                    _uiState.value = LoginState.Success(user)
                }
                .onFailure { exception ->
                    _uiState.value = LoginState.Error(exception.message ?: "Authentication failed.")
                }
        }
    }

    fun resetState() {
        _uiState.value = LoginState.Idle
    }
}
