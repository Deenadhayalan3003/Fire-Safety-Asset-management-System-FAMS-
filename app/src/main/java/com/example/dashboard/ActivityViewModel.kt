package com.example.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.models.Activity
import com.example.repository.ActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ActivityViewModel @Inject constructor(
    private val activityRepository: ActivityRepository
) : ViewModel() {

    val activities = activityRepository.activities

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    init {
        loadActivities()
    }

    fun loadActivities() {
        _loading.value = true
        viewModelScope.launch {
            activityRepository.fetchActivities()
            _loading.value = false
        }
    }

    fun addActivity(activity: Activity) {
        viewModelScope.launch {
            activityRepository.addActivity(activity)
        }
    }
}
