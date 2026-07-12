package com.example.models

data class Activity(
    val id: String = "",
    val type: String = "", // e.g. "Asset Added", "Inspection Completed", "Maintenance Closed", "QR Scanned", "Asset Updated", "Report Generated"
    val date: String = "", // e.g. "2026-07-09"
    val time: String = "", // e.g. "10:15 AM"
    val user: String = "",
    val description: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
