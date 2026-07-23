package com.example.smarthome.data.model

data class UsageStat(
    val deviceId: String,
    val deviceName: String,
    val deviceType: String,
    val floorName: String,
    val totalOnMinutes: Long,
    val lastUsedEpochMillis: Long?
)
