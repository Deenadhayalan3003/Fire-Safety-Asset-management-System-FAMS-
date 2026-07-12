package com.example.repository

import android.content.Context
import android.util.Log
import com.example.models.Activity
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

@Singleton
class FirebaseActivityRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : ActivityRepository {
    private val TAG = "ActivityRepository"

    private var firestore: FirebaseFirestore? = null
    private var isFirebaseEnabled = false

    private val _activities = MutableStateFlow<List<Activity>>(emptyList())
    override val activities: StateFlow<List<Activity>> = _activities.asStateFlow()

    private val demoActivities = mutableListOf<Activity>()

    init {
        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                firestore = FirebaseFirestore.getInstance()
                isFirebaseEnabled = true
                setupFirestoreListener()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Firestore for Activities: ${e.message}", e)
        }

        initializeDemoActivities()
        if (!isFirebaseEnabled) {
            _activities.value = demoActivities
        }
    }

    private fun initializeDemoActivities() {
        demoActivities.clear()
        demoActivities.addAll(
            listOf(
                Activity(
                    id = "act-1",
                    type = "Asset Added",
                    date = "2026-07-09",
                    time = "09:12 AM",
                    user = "John Doe",
                    description = "New ABC Dry Powder Extinguisher added at Main Reception Desk - Ground Floor"
                ),
                Activity(
                    id = "act-2",
                    type = "Inspection Completed",
                    date = "2026-07-09",
                    time = "10:15 AM",
                    user = "John Doe",
                    description = "Monthly physical check passed for FE-101. Pressure level is normal."
                ),
                Activity(
                    id = "act-3",
                    type = "Asset Updated",
                    date = "2026-07-08",
                    time = "02:30 PM",
                    user = "John Doe",
                    description = "FACP-200 alarm panel status updated to Damaged due to Zone 3 system fault."
                ),
                Activity(
                    id = "act-4",
                    type = "QR Scanned",
                    date = "2026-07-08",
                    time = "11:05 AM",
                    user = "System Admin",
                    description = "QR tag scanned successfully for Server Room Smoke Detector (SD-204)."
                ),
                Activity(
                    id = "act-5",
                    type = "Maintenance Closed",
                    date = "2026-07-07",
                    time = "04:15 PM",
                    user = "Technical Admin",
                    description = "Sprinkler Assembly hydro-test successfully finished and certified."
                ),
                Activity(
                    id = "act-6",
                    type = "Report Generated",
                    date = "2026-07-07",
                    time = "09:00 AM",
                    user = "John Doe",
                    description = "Monthly Fire Safety Compliance Audit Report generated for Plant A."
                ),
                Activity(
                    id = "act-7",
                    type = "Inspection Completed",
                    date = "2026-07-06",
                    time = "03:45 PM",
                    user = "John Doe",
                    description = "Failed inspection recorded for Main Alarm Panel (AP-401). Low battery warning."
                ),
                Activity(
                    id = "act-8",
                    type = "QR Scanned",
                    date = "2026-07-06",
                    time = "01:20 PM",
                    user = "Technical Admin",
                    description = "Barcode scanned for external Wet Pillar Fire Hydrant (FH-301)."
                )
            )
        )
    }

    private fun setupFirestoreListener() {
        val db = firestore ?: return
        db.collection("activities")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(20)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to activities collection", error)
                    return@addSnapshotListener
                }

                val list = snapshot?.toObjects(Activity::class.java) ?: emptyList()
                if (list.isEmpty()) {
                    _activities.value = demoActivities
                } else {
                    _activities.value = list
                }
            }
    }

    override suspend fun fetchActivities(limit: Int): Result<List<Activity>> {
        if (isFirebaseEnabled && firestore != null) {
            return try {
                val snapshot = firestore!!.collection("activities")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(limit.toLong())
                    .get()
                    .await()
                val list = snapshot.toObjects(Activity::class.java)
                if (list.isEmpty()) {
                    _activities.value = demoActivities
                    Result.success(demoActivities)
                } else {
                    _activities.value = list
                    Result.success(list)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching activities from Firestore, using demo fallback", e)
                _activities.value = demoActivities
                Result.success(demoActivities)
            }
        } else {
            _activities.value = demoActivities
            return Result.success(demoActivities)
        }
    }

    override suspend fun addActivity(activity: Activity): Result<Unit> {
        if (isFirebaseEnabled && firestore != null) {
            return try {
                val actId = activity.id.ifEmpty { firestore!!.collection("activities").document().id }
                val finalAct = activity.copy(id = actId)
                firestore!!.collection("activities").document(actId).set(finalAct).await()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        } else {
            demoActivities.add(0, activity)
            _activities.value = demoActivities.toList()
            return Result.success(Unit)
        }
    }
}
