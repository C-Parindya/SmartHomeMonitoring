package com.example.smarthome.data.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Notification(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val type: String = "INFO" // e.g., "SAFETY", "INFO", "ALERT"
)
