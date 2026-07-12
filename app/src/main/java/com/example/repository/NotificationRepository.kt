package com.example.repository

import com.example.models.Notification
import kotlinx.coroutines.flow.StateFlow

interface NotificationRepository {
    val notifications: StateFlow<List<Notification>>
    val unreadCount: StateFlow<Int>
    suspend fun fetchNotifications(): Result<List<Notification>>
    suspend fun markAsRead(notificationId: String): Result<Unit>
    suspend fun addNotification(notification: Notification): Result<Unit>
}
