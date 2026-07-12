package com.example.repository

import android.content.Context
import android.util.Log
import com.example.models.Asset
import com.example.models.Inspection
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
class FirebaseAssetRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : AssetRepository {
    private val TAG = "AssetRepository"

    private var firestore: FirebaseFirestore? = null
    private var isFirebaseEnabled = false

    private val _assets = MutableStateFlow<List<Asset>>(emptyList())
    override val assets: StateFlow<List<Asset>> = _assets.asStateFlow()

    // Local Demo Data store
    private val demoAssets = mutableListOf<Asset>()
    private val demoInspections = mutableListOf<Inspection>()

    init {
        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                firestore = FirebaseFirestore.getInstance()
                isFirebaseEnabled = true
                Log.d(TAG, "Firebase Firestore initialized.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Firebase initialization failed for Firestore: ${e.message}", e)
        }

        // Initialize with default demo assets in case Firestore is unavailable or empty
        initializeDemoData()
        _assets.value = demoAssets
    }

    private fun initializeDemoData() {
        demoAssets.clear()
        demoAssets.addAll(
            listOf(
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
        )

        // Preload some inspection records
        demoInspections.addAll(
            listOf(
                Inspection(
                    id = "INSP-1001",
                    assetId = "FE-101",
                    inspectorName = "John Doe",
                    inspectorId = "tech-uid",
                    date = "2026-06-15",
                    status = "Passed",
                    comments = "Extinguisher weight within limits, pressure gauge in green zone. Security seal intact.",
                    pressureLevel = "Normal",
                    batteryStatus = "Normal",
                    structuralIntegrity = "Good"
                ),
                Inspection(
                    id = "INSP-1002",
                    assetId = "AP-401",
                    inspectorName = "System Admin",
                    inspectorId = "admin-uid",
                    date = "2026-07-01",
                    status = "Failed",
                    comments = "Zone 3 fault light is persistently flashing. Requires motherboard diagnostic check.",
                    pressureLevel = "Normal",
                    batteryStatus = "Low",
                    structuralIntegrity = "Good"
                )
            )
        )
    }

    override suspend fun fetchAssets(): Result<List<Asset>> {
        if (isFirebaseEnabled && firestore != null) {
            return try {
                val snapshot = firestore!!.collection("assets").get().await()
                val list = snapshot.toObjects(Asset::class.java)
                if (list.isEmpty()) {
                    // Seed Firestore if it is empty
                    for (asset in demoAssets) {
                        firestore!!.collection("assets").document(asset.id).set(asset).await()
                    }
                    _assets.value = demoAssets
                    Result.success(demoAssets)
                } else {
                    _assets.value = list
                    Result.success(list)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch assets from Firestore, using local demo data.", e)
                Result.success(demoAssets)
            }
        } else {
            _assets.value = demoAssets
            return Result.success(demoAssets)
        }
    }

    override suspend fun addAsset(asset: Asset): Result<Unit> {
        if (isFirebaseEnabled && firestore != null) {
            return try {
                firestore!!.collection("assets").document(asset.id).set(asset).await()
                fetchAssets()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        } else {
            demoAssets.add(asset)
            _assets.value = demoAssets.toList()
            return Result.success(Unit)
        }
    }

    override suspend fun updateAssetStatus(assetId: String, status: String): Result<Unit> {
        if (isFirebaseEnabled && firestore != null) {
            return try {
                firestore!!.collection("assets").document(assetId).update("status", status).await()
                fetchAssets()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        } else {
            val index = demoAssets.indexOfFirst { it.id == assetId }
            if (index != -1) {
                val updated = demoAssets[index].copy(status = status)
                demoAssets[index] = updated
                _assets.value = demoAssets.toList()
            }
            return Result.success(Unit)
        }
    }

    override suspend fun submitInspection(inspection: Inspection): Result<Unit> {
        if (isFirebaseEnabled && firestore != null) {
            return try {
                firestore!!.collection("inspections").document(inspection.id).set(inspection).await()
                val assetUpdateMap = mapOf(
                    "status" to if (inspection.status == "Passed") "Operational" else "Under Inspection",
                    "lastInspectionDate" to inspection.date
                )
                firestore!!.collection("assets").document(inspection.assetId).update(assetUpdateMap).await()
                fetchAssets()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        } else {
            demoInspections.add(inspection)
            val index = demoAssets.indexOfFirst { it.id == inspection.assetId }
            if (index != -1) {
                val updatedAsset = demoAssets[index].copy(
                    status = if (inspection.status == "Passed") "Operational" else "Under Inspection",
                    lastInspectionDate = inspection.date
                )
                demoAssets[index] = updatedAsset
                _assets.value = demoAssets.toList()
            }
            return Result.success(Unit)
        }
    }

    override suspend fun getInspectionHistory(assetId: String): Result<List<Inspection>> {
        if (isFirebaseEnabled && firestore != null) {
            return try {
                val snapshot = firestore!!.collection("inspections")
                    .whereEqualTo("assetId", assetId)
                    .get()
                    .await()
                val list = snapshot.toObjects(Inspection::class.java)
                Result.success(list)
            } catch (e: Exception) {
                Result.failure(e)
            }
        } else {
            val list = demoInspections.filter { it.assetId == assetId }
            return Result.success(list)
        }
    }

    override suspend fun deleteAsset(assetId: String): Result<Unit> {
        if (isFirebaseEnabled && firestore != null) {
            return try {
                firestore!!.collection("assets").document(assetId).delete().await()
                fetchAssets()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        } else {
            demoAssets.removeAll { it.id == assetId }
            _assets.value = demoAssets.toList()
            return Result.success(Unit)
        }
    }

    override suspend fun updateAsset(asset: Asset): Result<Unit> {
        if (isFirebaseEnabled && firestore != null) {
            return try {
                firestore!!.collection("assets").document(asset.id).set(asset).await()
                fetchAssets()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        } else {
            val index = demoAssets.indexOfFirst { it.id == asset.id }
            if (index != -1) {
                demoAssets[index] = asset
                _assets.value = demoAssets.toList()
            }
            return Result.success(Unit)
        }
    }
}
