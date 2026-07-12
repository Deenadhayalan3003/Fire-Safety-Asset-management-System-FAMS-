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

sealed class ProfileState {
    object Idle : ProfileState()
    object Loading : ProfileState()
    object LogoutSuccess : ProfileState()
    data class Error(val message: String) : ProfileState()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    val currentUser: StateFlow<User?> = authRepository.currentUser

    private val _uiState = MutableStateFlow<ProfileState>(ProfileState.Idle)
    val uiState: StateFlow<ProfileState> = _uiState.asStateFlow()

    fun logout() {
        _uiState.value = ProfileState.Loading
        viewModelScope.launch {
            authRepository.logout()
                .onSuccess {
                    _uiState.value = ProfileState.LogoutSuccess
                }
                .onFailure { exception ->
                    _uiState.value = ProfileState.Error(exception.message ?: "Failed to log out.")
                }
        }
    }

    fun resetState() {
        _uiState.value = ProfileState.Idle
    }
}
