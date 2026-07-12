package com.example.repository

import com.example.models.Asset
import com.example.models.AssetSummary
import com.example.models.DashboardSummary
import com.example.models.InspectionSummary
import com.example.models.MaintenanceSummary
import kotlinx.coroutines.flow.StateFlow

interface DashboardRepository {
    val dashboardSummary: StateFlow<DashboardSummary>
    val upcomingInspections: StateFlow<List<Asset>>
    val maintenanceSummary: StateFlow<MaintenanceSummary>
    val assetDistribution: StateFlow<List<AssetSummary>>
    val inspectionSummary: StateFlow<InspectionSummary>
    
    suspend fun fetchDashboardData(): Result<Unit>
}
