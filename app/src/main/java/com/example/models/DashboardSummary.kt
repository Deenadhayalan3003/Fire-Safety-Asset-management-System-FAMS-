package com.example.models

data class DashboardSummary(
    val totalAssets: Int = 0,
    val healthyAssets: Int = 0,
    val inspectionDue: Int = 0,
    val overdueInspections: Int = 0,
    val maintenanceDue: Int = 0,
    val criticalAssets: Int = 0
)

data class AssetSummary(
    val type: String = "",
    val count: Int = 0
)

data class InspectionSummary(
    val completed: Int = 0,
    val pending: Int = 0,
    val overdue: Int = 0
)

data class MaintenanceSummary(
    val refillDue: Int = 0,
    val hydroTestDue: Int = 0,
    val preventiveMaintenanceDue: Int = 0,
    val breakdownPending: Int = 0,
    val firePumpWeeklyTestDue: Int = 0,
    val batteryReplacementDue: Int = 0,
    val detectorCleaningDue: Int = 0
)
