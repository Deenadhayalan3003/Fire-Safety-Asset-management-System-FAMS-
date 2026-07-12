package com.example.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.models.Notification
import com.example.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    val notifications = notificationRepository.notifications
    val unreadCount = notificationRepository.unreadCount

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    init {
        loadNotifications()
    }

    fun loadNotifications() {
        _loading.value = true
        viewModelScope.launch {
            notificationRepository.fetchNotifications()
            _loading.value = false
        }
    }

    fun markAsRead(id: String) {
        viewModelScope.launch {
            notificationRepository.markAsRead(id)
        }
    }

    fun addNotification(notification: Notification) {
        viewModelScope.launch {
            notificationRepository.addNotification(notification)
        }
    }
}
