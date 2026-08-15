package com.example.smarthome.data.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Area(
    val id: String = "",
    val name: String = "",
    val floorId: String = "",
    val type: String = "Room", // e.g., "Room", "Kitchen", "Bathroom"
    val gridRows: Int = 8,
    val gridCols: Int = 4,
    val devices: List<Device> = emptyList()
)
