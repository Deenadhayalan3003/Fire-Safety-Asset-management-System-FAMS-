package com.example.repository

import android.content.Context
import android.util.Log
import com.example.models.WorkOrder
import com.example.models.Vendor
import com.example.models.TimelineEvent
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Singleton
class FirebaseMaintenanceRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : MaintenanceRepository {
    private val TAG = "MaintenanceRepository"

    private var firestore: FirebaseFirestore? = null
    private var isFirebaseEnabled = false

    private val _workOrders = MutableStateFlow<List<WorkOrder>>(emptyList())
    override val workOrders: StateFlow<List<WorkOrder>> = _workOrders.asStateFlow()

    private val demoWorkOrders = mutableListOf<WorkOrder>()
    private val demoVendors = mutableListOf<Vendor>()

    init {
        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                firestore = FirebaseFirestore.getInstance()
                isFirebaseEnabled = true
                Log.d(TAG, "Firebase Firestore initialized.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Firebase initialization failed: ${e.message}")
        }

        initializeDemoData()
        _workOrders.value = demoWorkOrders
    }

    private fun initializeDemoData() {
        demoVendors.clear()
        demoVendors.addAll(
            listOf(
                Vendor("VND-01", "FireShield Ltd", "Sarah Jenkins", "555-0192", "contact@fireshield.com", listOf("Fire Extinguisher Refill", "Hydro Test", "Pressure Check"), 4.8),
                Vendor("VND-02", "Securitas Tech", "Marcus Vance", "555-0283", "support@securitastech.com", listOf("Smoke Detector Cleaning", "Battery Replacement", "Panel Service"), 4.5),
                Vendor("VND-03", "HydraFlow Valves", "David Miller", "555-0374", "service@hydraflow.com", listOf("Fire Pump Test", "Hydrant Flow Test", "Valve Replacement"), 4.7),
                Vendor("VND-04", "AquaGuard Sprinklers", "Elena Rostova", "555-0465", "repairs@aquaguard.com", listOf("Hose Replacement", "Gauge Replacement"), 4.6)
            )
        )

        demoWorkOrders.clear()
        demoWorkOrders.addAll(
            listOf(
                WorkOrder(
                    workOrderNumber = "WO-1001",
                    assetId = "AP-401",
                    assetName = "Main Fire Alarm Control Panel",
                    priority = "High",
                    description = "Motherboard fault diagnostic check and loop circuit troubleshooting.",
                    problem = "Zone 3 fault indicator remains blinking even after a panel reset.",
                    assignedTo = "John Doe",
                    vendor = "Securitas Tech",
                    department = "Security",
                    startDate = "2026-07-02",
                    dueDate = "2026-07-15",
                    status = "Open",
                    maintenanceType = "Corrective",
                    cost = 320.0,
                    timeline = listOf(
                        TimelineEvent("Open", "2026-07-02 09:00", "Work order initiated automatically due to inspection failure.", "System Admin")
                    )
                ),
                WorkOrder(
                    workOrderNumber = "WO-1002",
                    assetId = "FE-101",
                    assetName = "ABC Dry Powder Extinguisher",
                    priority = "Medium",
                    description = "Perform standard hydrostatic testing and dry powder recharge.",
                    problem = "Weight is slightly low; pressure level gauge on border line.",
                    assignedTo = "John Doe",
                    vendor = "FireShield Ltd",
                    department = "Safety",
                    startDate = "2026-06-16",
                    dueDate = "2026-06-20",
                    completionDate = "2026-06-18",
                    status = "Completed",
                    maintenanceType = "Preventive",
                    cost = 45.0,
                    timeline = listOf(
                        TimelineEvent("Open", "2026-06-16 10:15", "Routine preventive work order created.", "System User"),
                        TimelineEvent("In Progress", "2026-06-17 11:30", "Recharge and seals updated.", "John Doe"),
                        TimelineEvent("Completed", "2026-06-18 16:45", "Hydro test passed and refilled correctly.", "John Doe")
                    )
                ),
                WorkOrder(
                    workOrderNumber = "WO-1003",
                    assetId = "FH-301",
                    assetName = "Wet Pillar Fire Hydrant",
                    priority = "High",
                    description = "Replace rusty valve packing and perform standard hydrant flow rate tests.",
                    problem = "Slow leak observed during high-pressure system check.",
                    assignedTo = "Jane Smith",
                    vendor = "HydraFlow Valves",
                    department = "Utility",
                    startDate = "2026-07-05",
                    dueDate = "2026-07-12",
                    status = "In Progress",
                    maintenanceType = "Corrective",
                    cost = 180.0,
                    timeline = listOf(
                        TimelineEvent("Open", "2026-07-05 08:30", "Created.", "System User"),
                        TimelineEvent("Assigned", "2026-07-05 13:45", "Assigned to Jane Smith.", "System Admin"),
                        TimelineEvent("In Progress", "2026-07-06 10:00", "Repacking gland and setting test gauge.", "Jane Smith")
                    )
                ),
                WorkOrder(
                    workOrderNumber = "WO-1004",
                    assetId = "SP-501",
                    assetName = "Wet Pipe Sprinkler Assembly",
                    priority = "Low",
                    description = "Procure and install replacement heavy-duty pressure gauge.",
                    problem = "Gauge face is cloudy and cracked.",
                    assignedTo = "John Doe",
                    vendor = "AquaGuard Sprinklers",
                    department = "Warehouse",
                    startDate = "2026-07-08",
                    dueDate = "2026-07-22",
                    status = "Waiting Spare",
                    maintenanceType = "Preventive",
                    cost = 75.0,
                    timeline = listOf(
                        TimelineEvent("Open", "2026-07-08 14:00", "Created work order.", "John Doe"),
                        TimelineEvent("Waiting Spare", "2026-07-09 09:15", "Special size pressure gauge on backorder.", "John Doe")
                    )
                )
            )
        )
    }

    override suspend fun fetchWorkOrders(): Result<List<WorkOrder>> {
        return try {
            if (isFirebaseEnabled) {
                val snapshot = firestore!!.collection("workOrders").get().await()
                val list = snapshot.toObjects(WorkOrder::class.java)
                _workOrders.value = list
                Result.success(list)
            } else {
                _workOrders.value = demoWorkOrders
                Result.success(demoWorkOrders)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch work orders: ${e.message}", e)
            _workOrders.value = demoWorkOrders
            Result.success(demoWorkOrders)
        }
    }

    override suspend fun getWorkOrdersForAsset(assetId: String): Result<List<WorkOrder>> {
        return try {
            if (isFirebaseEnabled) {
                val snapshot = firestore!!.collection("workOrders")
                    .whereEqualTo("assetId", assetId)
                    .get()
                    .await()
                val list = snapshot.toObjects(WorkOrder::class.java)
                Result.success(list)
            } else {
                val filtered = demoWorkOrders.filter { it.assetId == assetId }
                Result.success(filtered)
            }
        } catch (e: Exception) {
            Result.success(demoWorkOrders.filter { it.assetId == assetId })
        }
    }

    override suspend fun createWorkOrder(workOrder: WorkOrder): Result<Unit> {
        return try {
            val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
            val initialEvent = TimelineEvent("Open", dateStr, "Work order created.", "System User")
            val preparedWO = workOrder.copy(
                timeline = workOrder.timeline + initialEvent
            )

            if (isFirebaseEnabled) {
                firestore!!.collection("workOrders")
                    .document(preparedWO.workOrderNumber)
                    .set(preparedWO)
                    .await()
            }
            
            val index = demoWorkOrders.indexOfFirst { it.workOrderNumber == preparedWO.workOrderNumber }
            if (index >= 0) {
                demoWorkOrders[index] = preparedWO
            } else {
                demoWorkOrders.add(0, preparedWO)
            }
            _workOrders.value = ArrayList(demoWorkOrders)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating work order: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun updateWorkOrder(workOrder: WorkOrder): Result<Unit> {
        return try {
            if (isFirebaseEnabled) {
                firestore!!.collection("workOrders")
                    .document(workOrder.workOrderNumber)
                    .set(workOrder)
                    .await()
            }
            val index = demoWorkOrders.indexOfFirst { it.workOrderNumber == workOrder.workOrderNumber }
            if (index >= 0) {
                demoWorkOrders[index] = workOrder
            }
            _workOrders.value = ArrayList(demoWorkOrders)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating work order: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun deleteWorkOrder(woId: String): Result<Unit> {
        return try {
            if (isFirebaseEnabled) {
                firestore!!.collection("workOrders")
                    .document(woId)
                    .delete()
                    .await()
            }
            demoWorkOrders.removeAll { it.workOrderNumber == woId }
            _workOrders.value = ArrayList(demoWorkOrders)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getVendors(): Result<List<Vendor>> {
        return try {
            if (isFirebaseEnabled) {
                val snapshot = firestore!!.collection("vendors").get().await()
                val list = snapshot.toObjects(Vendor::class.java)
                if (list.isEmpty()) {
                    Result.success(demoVendors)
                } else {
                    Result.success(list)
                }
            } else {
                Result.success(demoVendors)
            }
        } catch (e: Exception) {
            Result.success(demoVendors)
        }
    }

    override suspend fun addVendor(vendor: Vendor): Result<Unit> {
        return try {
            if (isFirebaseEnabled) {
                firestore!!.collection("vendors")
                    .document(vendor.id)
                    .set(vendor)
                    .await()
            }
            demoVendors.add(vendor)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
