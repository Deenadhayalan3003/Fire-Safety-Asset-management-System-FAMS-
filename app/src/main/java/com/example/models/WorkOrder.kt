package com.example.models

data class TimelineEvent(
    val status: String = "",
    val timestamp: String = "",
    val note: String = "",
    val user: String = ""
)

data class WorkOrder(
    val workOrderNumber: String = "",
    val assetId: String = "",
    val assetName: String = "",
    val priority: String = "Medium", // Low, Medium, High, Critical
    val description: String = "",
    val problem: String = "",
    val assignedTo: String = "",
    val vendor: String = "",
    val department: String = "",
    val startDate: String = "",
    val dueDate: String = "",
    val completionDate: String = "",
    val status: String = "Open", // Open, Assigned, In Progress, Waiting Spare, Completed, Closed, Cancelled
    val maintenanceType: String = "Preventive", // Preventive, Corrective, Breakdown, Emergency, Calibration, AMC
    val beforePhotoUrl: String = "",
    val afterPhotoUrl: String = "",
    val invoiceUrl: String = "",
    val vendorReportUrl: String = "",
    val completionCertificateUrl: String = "",
    val cost: Double = 0.0,
    val downtimeMinutes: Long = 0,
    val timeline: List<TimelineEvent> = emptyList()
)

data class Vendor(
    val id: String = "",
    val name: String = "",
    val contactPerson: String = "",
    val phone: String = "",
    val email: String = "",
    val services: List<String> = emptyList(),
    val rating: Double = 5.0
)
