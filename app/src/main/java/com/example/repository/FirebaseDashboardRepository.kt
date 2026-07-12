package com.example.repository

import android.content.Context
import android.util.Log
import com.example.models.*
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

@Singleton
class FirebaseDashboardRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : DashboardRepository {
    private val TAG = "DashboardRepository"

    private var firestore: FirebaseFirestore? = null
    private var isFirebaseEnabled = false

    private val _dashboardSummary = MutableStateFlow(DashboardSummary())
    override val dashboardSummary: StateFlow<DashboardSummary> = _dashboardSummary.asStateFlow()

    private val _upcomingInspections = MutableStateFlow<List<Asset>>(emptyList())
    override val upcomingInspections: StateFlow<List<Asset>> = _upcomingInspections.asStateFlow()

    private val _maintenanceSummary = MutableStateFlow(MaintenanceSummary())
    override val maintenanceSummary: StateFlow<MaintenanceSummary> = _maintenanceSummary.asStateFlow()

    private val _assetDistribution = MutableStateFlow<List<AssetSummary>>(emptyList())
    override val assetDistribution: StateFlow<List<AssetSummary>> = _assetDistribution.asStateFlow()

    private val _inspectionSummary = MutableStateFlow(InspectionSummary())
    override val inspectionSummary: StateFlow<InspectionSummary> = _inspectionSummary.asStateFlow()

    init {
        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                firestore = FirebaseFirestore.getInstance()
                isFirebaseEnabled = true
                Log.d(TAG, "Firestore initialized for Dashboard.")
                setupFirestoreListeners()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Firestore for Dashboard: ${e.message}", e)
        }

        if (!isFirebaseEnabled) {
            loadDemoData()
        }
    }

    private fun setupFirestoreListeners() {
        val db = firestore ?: return

        // Real-time listener for assets to calculate dashboard summary and upcoming list
        db.collection("assets").addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error listening to assets", error)
                return@addSnapshotListener
            }

            val assetList = snapshot?.toObjects(Asset::class.java) ?: emptyList()
            if (assetList.isEmpty()) {
                // If firestore contains nothing yet, use demo calculations
                loadDemoData()
            } else {
                calculateMetrics(assetList)
            }
        }
    }

    private fun calculateMetrics(assetList: List<Asset>) {
        val total = assetList.size
        val healthy = assetList.count { it.status == "Operational" }
        val inspectionDue = assetList.count { it.status == "Under Inspection" || it.nextInspectionDue.isNotEmpty() } // Or based on dates
        val critical = assetList.count { it.status == "Damaged" }
        
        // Mocking some other state variables from available data
        val overdue = assetList.count { it.status == "Damaged" || it.status == "Out of Service" }
        val maintenanceCount = assetList.count { it.status == "Damaged" || it.status == "Under Inspection" }

        _dashboardSummary.value = DashboardSummary(
            totalAssets = total,
            healthyAssets = healthy,
            inspectionDue = inspectionDue,
            overdueInspections = overdue,
            maintenanceDue = maintenanceCount,
            criticalAssets = critical
        )

        // Upcoming Inspections - Sort by nextInspectionDue
        val sortedUpcoming = assetList
            .filter { it.nextInspectionDue.isNotEmpty() }
            .sortedBy { it.nextInspectionDue }
            .take(10)
        _upcomingInspections.value = sortedUpcoming

        // Calculate distribution
        val distMap = assetList.groupBy { it.type }.map { (type, list) ->
            AssetSummary(type = type, count = list.size)
        }
        _assetDistribution.value = distMap

        // Standard Maintenance Breakdown based on Asset Type
        _maintenanceSummary.value = MaintenanceSummary(
            refillDue = assetList.count { it.type == "Fire Extinguisher" && it.status == "Damaged" },
            hydroTestDue = assetList.count { it.type == "Fire Hydrant" && it.status == "Under Inspection" },
            preventiveMaintenanceDue = assetList.count { it.status == "Under Inspection" },
            breakdownPending = assetList.count { it.status == "Damaged" },
            firePumpWeeklyTestDue = assetList.count { it.type == "Alarm Panel" && it.status == "Damaged" },
            batteryReplacementDue = assetList.count { it.type == "Smoke Detector" && it.status == "Damaged" },
            detectorCleaningDue = assetList.count { it.type == "Smoke Detector" && it.status == "Under Inspection" }
        )

        // Inspection summary
        _inspectionSummary.value = InspectionSummary(
            completed = healthy,
            pending = inspectionDue,
            overdue = overdue
        )
    }

    private fun loadDemoData() {
        val demoAssets = listOf(
            Asset(
                id = "FE-101",
                name = "ABC Dry Powder Extinguisher",
                type = "Fire Extinguisher",
                serialNumber = "DP-99812-X",
                location = "Main Reception Desk - Ground Floor",
                status = "Operational",
                lastInspectionDate = "2026-06-15",
                nextInspectionDue = "2026-12-15",
                model = "DP-9",
                manufacturer = "FireShield Ltd"
            ),
            Asset(
                id = "SD-204",
                name = "Optoelectronic Smoke Detector",
                type = "Smoke Detector",
                serialNumber = "SD-44510-O",
                location = "Server Room - 2nd Floor",
                status = "Operational",
                lastInspectionDate = "2026-05-10",
                nextInspectionDue = "2026-11-10",
                model = "OSD-Pro",
                manufacturer = "SafeCorp Sensing"
            ),
            Asset(
                id = "FH-301",
                name = "Wet Pillar Fire Hydrant",
                type = "Fire Hydrant",
                serialNumber = "HY-0129-W",
                location = "External Parking Lot - Gate B",
                status = "Under Inspection",
                lastInspectionDate = "2025-11-20",
                nextInspectionDue = "2026-05-20",
                model = "WPH-5",
                manufacturer = "HydraFlow Valves"
            ),
            Asset(
                id = "AP-401",
                name = "Main Fire Alarm Control Panel",
                type = "Alarm Panel",
                serialNumber = "AP-88129-L",
                location = "Security Command Center",
                status = "Damaged",
                lastInspectionDate = "2026-07-01",
                nextInspectionDue = "2026-08-01",
                model = "FACP-200",
                manufacturer = "Securitas Tech"
            ),
            Asset(
                id = "SP-501",
                name = "Wet Pipe Sprinkler Assembly",
                type = "Sprinkler",
                serialNumber = "SP-1109-P",
                location = "Basement Storage Warehouse",
                status = "Operational",
                lastInspectionDate = "2026-04-12",
                nextInspectionDue = "2026-10-12",
                model = "WPS-12",
                manufacturer = "AquaGuard Sprinklers"
            )
        )
        calculateMetrics(demoAssets)
    }

    override suspend fun fetchDashboardData(): Result<Unit> {
        if (isFirebaseEnabled && firestore != null) {
            return try {
                val snapshot = firestore!!.collection("assets").get().await()
                val assetList = snapshot.toObjects(Asset::class.java)
                if (assetList.isNotEmpty()) {
                    calculateMetrics(assetList)
                } else {
                    loadDemoData()
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching dashboard data", e)
                loadDemoData()
                Result.success(Unit)
            }
        } else {
            loadDemoData()
            return Result.success(Unit)
        }
    }
}
