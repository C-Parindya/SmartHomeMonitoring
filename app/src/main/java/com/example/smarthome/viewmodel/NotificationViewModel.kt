package com.example.smarthome.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarthome.data.model.Notification
import com.example.smarthome.data.repository.MockSmartHomeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class NotificationViewModel(
    private val repository: MockSmartHomeRepository
) : ViewModel() {

    val notifications: StateFlow<List<Notification>> = repository.notifications
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun clearNotifications() {
        repository.clearNotifications()
    }
}
