package com.example.models

data class User(
    val uid: String = "",
    val fullName: String = "",
    val email: String = "",
    val role: String = "Safety Officer", // Administrator, Safety Manager, Safety Officer, Fire Technician, Auditor, Viewer
    val companyCode: String = "",
    val employeeId: String = "",
    val mobile: String = "",
    val department: String = "",
    val designation: String = "",
    val plant: String = "",
    val status: String = "Active", // Active, Inactive
    val profilePhoto: String = "",
    val createdDate: Long = System.currentTimeMillis(),
    val lastLogin: Long = System.currentTimeMillis(),
    val deviceModel: String = "Android Device",
    val androidVersion: String = "Android 13",
    // Backwards compatibility fields
    val name: String = "",
    val company: String = ""
) {
    // Helper to get printable name
    fun getDisplayName(): String {
        return if (fullName.isNotEmpty()) fullName else name
    }
    
    // Helper to get company name/code
    fun getDisplayCompany(): String {
        return if (companyCode.isNotEmpty()) companyCode else company
    }
}

