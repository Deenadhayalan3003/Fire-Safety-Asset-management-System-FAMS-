package com.example.repository

import android.content.Context
import android.util.Log
import com.example.models.Notification
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
class FirebaseNotificationRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : NotificationRepository {
    private val TAG = "NotificationRepo"

    private var firestore: FirebaseFirestore? = null
    private var isFirebaseEnabled = false

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    override val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    override val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    private val demoNotifications = mutableListOf<Notification>()

    init {
        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                firestore = FirebaseFirestore.getInstance()
                isFirebaseEnabled = true
                setupFirestoreListener()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Firestore for Notifications: ${e.message}", e)
        }

        initializeDemoNotifications()
        if (!isFirebaseEnabled) {
            _notifications.value = demoNotifications
            updateUnreadCount()
        }
    }

    private fun initializeDemoNotifications() {
        demoNotifications.clear()
        demoNotifications.addAll(
            listOf(
                Notification(
                    id = "notif-1",
                    title = "Critical Alert - AP-401 Fault",
                    message = "FACP-200 Zone 3 reports hardware system failure. Action required.",
                    type = "Critical Alert",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 15, // 15 mins ago
                    isRead = false
                ),
                Notification(
                    id = "notif-2",
                    title = "Inspection Due",
                    message = "FE-101 (ABC Dry Powder) is due for the monthly security seal check.",
                    type = "Inspection Due",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 2, // 2 hours ago
                    isRead = false
                ),
                Notification(
                    id = "notif-3",
                    title = "Maintenance Due",
                    message = "Dry pipe sprinkler system PM scheduled. Verify air compressor status.",
                    type = "Maintenance Due",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 24, // 1 day ago
                    isRead = true
                )
            )
        )
    }

    private fun setupFirestoreListener() {
        val db = firestore ?: return
        db.collection("notifications")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to notifications", error)
                    return@addSnapshotListener
                }

                val list = snapshot?.toObjects(Notification::class.java) ?: emptyList()
                if (list.isEmpty()) {
                    _notifications.value = demoNotifications
                } else {
                    _notifications.value = list
                }
                updateUnreadCount()
            }
    }

    private fun updateUnreadCount() {
        _unreadCount.value = _notifications.value.count { !it.isRead }
    }

    override suspend fun fetchNotifications(): Result<List<Notification>> {
        if (isFirebaseEnabled && firestore != null) {
            return try {
                val snapshot = firestore!!.collection("notifications")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .await()
                val list = snapshot.toObjects(Notification::class.java)
                if (list.isEmpty()) {
                    _notifications.value = demoNotifications
                    updateUnreadCount()
                    Result.success(demoNotifications)
                } else {
                    _notifications.value = list
                    updateUnreadCount()
                    Result.success(list)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch notifications from Firestore, using demo fallback", e)
                _notifications.value = demoNotifications
                updateUnreadCount()
                Result.success(demoNotifications)
            }
        } else {
            _notifications.value = demoNotifications
            updateUnreadCount()
            return Result.success(demoNotifications)
        }
    }

    override suspend fun markAsRead(notificationId: String): Result<Unit> {
        if (isFirebaseEnabled && firestore != null) {
            return try {
                firestore!!.collection("notifications").document(notificationId)
                    .update("isRead", true).await()
                fetchNotifications()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        } else {
            val index = demoNotifications.indexOfFirst { it.id == notificationId }
            if (index != -1) {
                demoNotifications[index] = demoNotifications[index].copy(isRead = true)
                _notifications.value = demoNotifications.toList()
                updateUnreadCount()
            }
            return Result.success(Unit)
        }
    }

    override suspend fun addNotification(notification: Notification): Result<Unit> {
        if (isFirebaseEnabled && firestore != null) {
            return try {
                val notifId = notification.id.ifEmpty { firestore!!.collection("notifications").document().id }
                val finalNotif = notification.copy(id = notifId)
                firestore!!.collection("notifications").document(notifId).set(finalNotif).await()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        } else {
            demoNotifications.add(0, notification)
            _notifications.value = demoNotifications.toList()
            updateUnreadCount()
            return Result.success(Unit)
        }
    }
}
