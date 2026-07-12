package com.example.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.models.*
import com.example.repository.AssetRepository
import com.example.repository.DashboardRepository
import com.example.repository.MaintenanceRepository
import com.example.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val assetRepository: AssetRepository,
    private val dashboardRepository: DashboardRepository,
    private val maintenanceRepository: MaintenanceRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    val assets = assetRepository.assets
    val workOrders = maintenanceRepository.workOrders
    val dashboardSummary = dashboardRepository.dashboardSummary
    val upcomingInspections = dashboardRepository.upcomingInspections
    val maintenanceSummary = dashboardRepository.maintenanceSummary
    val assetDistribution = dashboardRepository.assetDistribution
    val inspectionSummary = dashboardRepository.inspectionSummary

    private val _vendors = MutableStateFlow<List<Vendor>>(emptyList())
    val vendors: StateFlow<List<Vendor>> = _vendors.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadDashboardData()
        fetchWorkOrders()
        fetchVendors()
    }

    fun loadDashboardData() {
        _loading.value = true
        _error.value = null
        viewModelScope.launch {
            val assetResult = assetRepository.fetchAssets()
            val dashResult = dashboardRepository.fetchDashboardData()
            
            _loading.value = false
            if (assetResult.isFailure) {
                _error.value = assetResult.exceptionOrNull()?.message ?: "Failed to load safety assets."
            } else if (dashResult.isFailure) {
                _error.value = dashResult.exceptionOrNull()?.message ?: "Failed to compute dashboard metrics."
            }
        }
    }

    fun fetchWorkOrders() {
        viewModelScope.launch {
            maintenanceRepository.fetchWorkOrders()
        }
    }

    fun fetchVendors() {
        viewModelScope.launch {
            maintenanceRepository.getVendors().onSuccess {
                _vendors.value = it
            }
        }
    }

    fun addAsset(asset: Asset) {
        viewModelScope.launch {
            assetRepository.addAsset(asset)
            dashboardRepository.fetchDashboardData()
        }
    }

    fun updateAssetStatus(assetId: String, status: String) {
        viewModelScope.launch {
            assetRepository.updateAssetStatus(assetId, status)
            dashboardRepository.fetchDashboardData()
        }
    }

    fun deleteAsset(assetId: String) {
        viewModelScope.launch {
            assetRepository.deleteAsset(assetId)
            dashboardRepository.fetchDashboardData()
        }
    }

    fun updateAsset(asset: Asset) {
        viewModelScope.launch {
            assetRepository.updateAsset(asset)
            dashboardRepository.fetchDashboardData()
        }
    }

    fun submitInspection(inspection: Inspection) {
        viewModelScope.launch {
            // 1. Submit to asset repository
            assetRepository.submitInspection(inspection)

            // 2. Automated triggers based on inspection result
            val isFailed = inspection.status == "Failed" || inspection.status == "Attention Required"
            
            if (isFailed) {
                // Update asset status to "Damaged" or "Under Inspection"
                assetRepository.updateAssetStatus(inspection.assetId, "Damaged")

                // Find the asset to get its name
                val targetAsset = assets.value.find { it.id == inspection.assetId }
                val assetName = targetAsset?.name ?: "Safety Asset"

                // Create a notification
                val notificationId = UUID.randomUUID().toString()
                notificationRepository.addNotification(
                    Notification(
                        id = notificationId,
                        title = "Critical Failure: ${inspection.assetId}",
                        message = "Asset $assetName failed inspection on ${inspection.date}. Comments: ${inspection.comments}",
                        type = "Critical Alert",
                        timestamp = System.currentTimeMillis()
                    )
                )

                // Generate automatic corrective work order
                val woNum = "WO-${1000 + (workOrders.value.size + 1)}"
                val newWO = WorkOrder(
                    workOrderNumber = woNum,
                    assetId = inspection.assetId,
                    assetName = assetName,
                    priority = "High",
                    description = "Corrective maintenance required after inspection failure. Inspector remarks: ${inspection.comments}",
                    problem = "Inspection Failed: ${inspection.comments}",
                    startDate = inspection.date,
                    dueDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(System.currentTimeMillis() + 7 * 24 * 3600 * 1000L)),
                    status = "Open",
                    maintenanceType = "Corrective",
                    timeline = listOf(
                        TimelineEvent(
                            status = "Open",
                            timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date()),
                            note = "Auto-generated after inspection failure (Inspection ID: ${inspection.id})",
                            user = inspection.inspectorName
                        )
                    )
                )
                maintenanceRepository.createWorkOrder(newWO)
            } else {
                // Passed or Advisory
                val finalStatus = if (inspection.status == "Advisory") "Under Inspection" else "Operational"
                assetRepository.updateAssetStatus(inspection.assetId, finalStatus)

                val targetAsset = assets.value.find { it.id == inspection.assetId }
                val assetName = targetAsset?.name ?: "Safety Asset"

                // Create standard notification
                notificationRepository.addNotification(
                    Notification(
                        id = UUID.randomUUID().toString(),
                        title = "Inspection Completed: ${inspection.assetId}",
                        message = "Asset $assetName has passed inspection successfully.",
                        type = "Inspection Due", // Standard non-critical category
                        timestamp = System.currentTimeMillis()
                    )
                )
            }

            // Refresh dashboard
            dashboardRepository.fetchDashboardData()
            // Refresh work orders
            maintenanceRepository.fetchWorkOrders()
        }
    }

    fun createWorkOrder(workOrder: WorkOrder) {
        viewModelScope.launch {
            maintenanceRepository.createWorkOrder(workOrder)
            notificationRepository.addNotification(
                Notification(
                    id = UUID.randomUUID().toString(),
                    title = "Work Order Created: ${workOrder.workOrderNumber}",
                    message = "New ${workOrder.maintenanceType} work order created for asset ${workOrder.assetId}.",
                    type = "Maintenance Due",
                    timestamp = System.currentTimeMillis()
                )
            )
            // If the work order puts asset into corrective/breakdown, update its status
            if (workOrder.maintenanceType == "Breakdown" || workOrder.priority == "Critical") {
                assetRepository.updateAssetStatus(workOrder.assetId, "Damaged")
            } else {
                assetRepository.updateAssetStatus(workOrder.assetId, "Under Inspection")
            }
            dashboardRepository.fetchDashboardData()
        }
    }

    fun updateWorkOrder(workOrder: WorkOrder) {
        viewModelScope.launch {
            maintenanceRepository.updateWorkOrder(workOrder)
            
            // Handle specific status updates
            if (workOrder.status == "Completed" || workOrder.status == "Closed") {
                // Mark asset back as Operational
                assetRepository.updateAssetStatus(workOrder.assetId, "Operational")
                
                notificationRepository.addNotification(
                    Notification(
                        id = UUID.randomUUID().toString(),
                        title = "Work Order Completed: ${workOrder.workOrderNumber}",
                        message = "Work order for asset ${workOrder.assetId} has been resolved.",
                        type = "Completed",
                        timestamp = System.currentTimeMillis()
                    )
                )
            } else if (workOrder.status == "Cancelled") {
                notificationRepository.addNotification(
                    Notification(
                        id = UUID.randomUUID().toString(),
                        title = "Work Order Cancelled: ${workOrder.workOrderNumber}",
                        message = "Work order for asset ${workOrder.assetId} was cancelled.",
                        type = "Rejected",
                        timestamp = System.currentTimeMillis()
                    )
                )
            }

            dashboardRepository.fetchDashboardData()
        }
    }

    fun deleteWorkOrder(woId: String) {
        viewModelScope.launch {
            maintenanceRepository.deleteWorkOrder(woId)
            dashboardRepository.fetchDashboardData()
        }
    }

    fun addVendor(vendor: Vendor) {
        viewModelScope.launch {
            maintenanceRepository.addVendor(vendor)
            fetchVendors()
        }
    }
}
