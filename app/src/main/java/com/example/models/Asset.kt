package com.example.models

data class Asset(
    val id: String = "",
    val assetCode: String = "",
    val name: String = "",
    val category: String = "", // Active Protection, Passive Protection, Life Safety, etc.
    val type: String = "Fire Extinguisher", // Master Asset Types
    val qrCodeUrl: String = "",
    val plant: String = "",
    val building: String = "",
    val floor: String = "",
    val department: String = "",
    val zone: String = "",
    val location: String = "",
    val gps: String = "", // Optional GPS
    val manufacturer: String = "",
    val model: String = "",
    val serialNumber: String = "",
    val manufacturingDate: String = "",
    val installationDate: String = "",
    val warrantyExpiry: String = "",
    val status: String = "Operational", // Operational, Damaged, Out of Service, Under Inspection, Archived
    val condition: String = "New", // New, Good, Fair, Poor, Damaged
    val photos: List<String> = emptyList(), // Multiple Photos
    val remarks: String = "",
    val createdBy: String = "",
    val createdDate: String = "",
    val modifiedBy: String = "",
    val modifiedDate: String = "",

    // FIRE EXTINGUISHER FIELDS
    val extinguisherType: String = "", // ABC, CO2, Water, Foam, Clean Agent
    val capacity: String = "",
    val pressureGauge: String = "", // Normal, Low, High
    val refillDate: String = "",
    val nextRefill: String = "",
    val hydroTestDate: String = "",
    val nextHydroTest: String = "",
    val cylinderWeight: String = "",
    val sealStatus: String = "", // Intact, Broken, Missing
    val safetyPin: String = "", // Present, Missing
    val bracketCondition: String = "", // Good, Damaged, Loose

    // HYDRANT FIELDS
    val hydrantNumber: String = "",
    val pressure: String = "", // PSI/Bar
    val valveCondition: String = "", // Good, Leaking, Jammed
    val capCondition: String = "", // Good, Damaged, Missing
    val accessibility: String = "", // Clear, Blocked
    val flowTestDate: String = "",

    // FIRE PUMP FIELDS
    val pumpType: String = "", // Electric, Diesel, Jockey
    val pumpPressure: String = "",
    val rpm: String = "",
    val batteryVoltage: String = "",
    val fuelLevel: String = "", // %
    val controllerStatus: String = "", // Auto, Manual, Off
    val weeklyTestDate: String = "",

    // Legacy fields used by existing screens
    val lastInspectionDate: String = "",
    val nextInspectionDue: String = ""
)
