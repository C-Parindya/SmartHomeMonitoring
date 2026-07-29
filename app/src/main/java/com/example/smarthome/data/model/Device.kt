package com.example.smarthome.data.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
sealed class Device {
    abstract val id: String
    abstract val name: String
    abstract val floorId: String
    abstract val row: Int
    abstract val col: Int
    abstract val state: DeviceState

    @IgnoreExtraProperties
    data class Outlet(
        override val id: String = "",
        override val name: String = "",
        override val floorId: String = "",
        override val row: Int = 0,
        override val col: Int = 0,
        override val state: DeviceState = DeviceState.OFF
    ) : Device()

    @IgnoreExtraProperties
    data class MultiSwitch(
        override val id: String = "",
        override val name: String = "",
        override val floorId: String = "",
        override val row: Int = 0,
        override val col: Int = 0,
        override val state: DeviceState = DeviceState.OFF,
        val switches: List<SwitchState> = emptyList()
    ) : Device()

    @IgnoreExtraProperties
    data class ScheduledDevice(
        override val id: String = "",
        override val name: String = "",
        override val floorId: String = "",
        override val row: Int = 0,
        override val col: Int = 0,
        override val state: DeviceState = DeviceState.OFF,
        val deviceKind: ScheduledKind = ScheduledKind.LIGHT,
        val maxDurationMinutes: Int = 0,
        val onTime: String? = null,
        val offTime: String? = null
    ) : Device()

    @IgnoreExtraProperties
    data class Camera(
        override val id: String = "",
        override val name: String = "",
        override val floorId: String = "",
        override val row: Int = 0,
        override val col: Int = 0,
        override val state: DeviceState = DeviceState.OFF,
        val snapshotUrl: String? = null,
        val isStreaming: Boolean = false
    ) : Device()
}

@IgnoreExtraProperties
data class SwitchState(
    val id: String = "",
    val name: String = "",
    val isOn: Boolean = false
)

enum class ScheduledKind {
    LIGHT,
    IRON
}
