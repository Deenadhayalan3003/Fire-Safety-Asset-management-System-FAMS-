package com.example.repository

import com.example.models.Activity
import kotlinx.coroutines.flow.StateFlow

interface ActivityRepository {
    val activities: StateFlow<List<Activity>>
    suspend fun fetchActivities(limit: Int = 20): Result<List<Activity>>
    suspend fun addActivity(activity: Activity): Result<Unit>
}
