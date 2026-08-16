package com.example.smarthome.data.model

import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.database.PropertyName

enum class DeviceType {
    OUTLET,
    MULTI_SWITCH,
    SCHEDULED_DEVICE,
    CAMERA
}

@IgnoreExtraProperties
data class Device(
    val id: String = "",
    val name: String = "",
    val floorId: String = "",
    val areaId: String = "",
    val row: Int = 0,
    val col: Int = 0,
    val state: DeviceState = DeviceState.OFF,
    val type: DeviceType = DeviceType.OUTLET,
    
    // MultiSwitch fields
    val switches: List<SwitchState> = emptyList(),
    
    // ScheduledDevice fields
    val deviceKind: ScheduledKind = ScheduledKind.LIGHT,
    val maxDurationMinutes: Int = 0,
    val onTime: String? = null,
    val offTime: String? = null,
    
    // Camera fields
    val snapshotUrl: String? = null,
    @get:PropertyName("streaming")
    @PropertyName("streaming")
    val isStreaming: Boolean = false
)

@IgnoreExtraProperties
data class SwitchState(
    val id: String = "",
    val name: String = "",
    @get:PropertyName("on")
    @PropertyName("on")
    val isOn: Boolean = false
)

enum class ScheduledKind {
    LIGHT,
    IRON,
    FAN
}
