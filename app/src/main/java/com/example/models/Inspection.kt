package com.example.models

data class Inspection(
    val id: String = "",
    val assetId: String = "",
    val inspectorName: String = "",
    val inspectorId: String = "",
    val date: String = "",
    val status: String = "Passed", // Passed, Failed, Advisory, Attention Required
    val comments: String = "",
    val pressureLevel: String = "Normal", // Normal, Low, High
    val batteryStatus: String = "Normal", // Normal, Low, Dead
    val structuralIntegrity: String = "Good", // Good, Fair, Poor
    val photoUrl: String = "",

    // PHASE 5 FIELDS
    val inspectionType: String = "Routine", // Daily, Weekly, Monthly, Quarterly, Half-Yearly, Annual, Special Inspection
    val checklist: Map<String, String> = emptyMap(), // Key: Checklist Item Name, Value: Pass, Fail, NA
    val inspectionScore: Double = 100.0,
    val compliancePercentage: Double = 100.0,
    val riskLevel: String = "Low", // Low, Medium, High
    val photoUrls: List<String> = emptyList(), // Multiple Photos
    val voiceNoteUrl: String = "",
    val digitalSignatureUrl: String = "",
    val gpsCoordinates: String = "",
    val isDraft: Boolean = false
)

