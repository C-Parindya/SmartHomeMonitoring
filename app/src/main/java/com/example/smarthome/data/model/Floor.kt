package com.example.smarthome.data.model

data class Floor(
    val id: String,
    val name: String,
    val gridRows: Int = 8,
    val gridCols: Int = 8,
    val devices: List<Device> = emptyList()
) {
    val deviceCount: Int get() = devices.size
}
