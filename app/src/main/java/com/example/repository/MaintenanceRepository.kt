package com.example.repository

import com.example.models.WorkOrder
import com.example.models.Vendor
import kotlinx.coroutines.flow.StateFlow

interface MaintenanceRepository {
    val workOrders: StateFlow<List<WorkOrder>>
    suspend fun fetchWorkOrders(): Result<List<WorkOrder>>
    suspend fun getWorkOrdersForAsset(assetId: String): Result<List<WorkOrder>>
    suspend fun createWorkOrder(workOrder: WorkOrder): Result<Unit>
    suspend fun updateWorkOrder(workOrder: WorkOrder): Result<Unit>
    suspend fun deleteWorkOrder(woId: String): Result<Unit>
    suspend fun getVendors(): Result<List<Vendor>>
    suspend fun addVendor(vendor: Vendor): Result<Unit>
}
