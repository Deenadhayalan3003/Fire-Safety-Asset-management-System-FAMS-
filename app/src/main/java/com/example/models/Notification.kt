package com.example.models

data class Notification(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "Critical Alert", // e.g. "Inspection Due", "Maintenance Due", "Critical Alert"
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
