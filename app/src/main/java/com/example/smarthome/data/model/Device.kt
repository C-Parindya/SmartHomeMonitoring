package com.example.smarthome.data.model

sealed class Device {
    abstract val id: String
    abstract val name: String
    abstract val floorId: String
    abstract val row: Int
    abstract val col: Int
    abstract val state: DeviceState

    data class Outlet(
        override val id: String,
        override val name: String,
        override val floorId: String,
        override val row: Int,
        override val col: Int,
        override val state: DeviceState = DeviceState.OFF
    ) : Device()

    data class MultiSwitch(
        override val id: String,
        override val name: String,
        override val floorId: String,
        override val row: Int,
        override val col: Int,
        override val state: DeviceState = DeviceState.OFF,
        val switches: List<SwitchState>
    ) : Device()

    data class ScheduledDevice(
        override val id: String,
        override val name: String,
        override val floorId: String,
        override val row: Int,
        override val col: Int,
        override val state: DeviceState = DeviceState.OFF,
        val deviceKind: ScheduledKind,
        val maxDurationMinutes: Int,
        val onTime: String?,
        val offTime: String?
    ) : Device()

    data class Camera(
        override val id: String,
        override val name: String,
        override val floorId: String,
        override val row: Int,
        override val col: Int,
        override val state: DeviceState = DeviceState.OFF,
        val snapshotUrl: String?,
        val isStreaming: Boolean = false
    ) : Device()
}

data class SwitchState(
    val id: String,
    val name: String,
    val isOn: Boolean
)

enum class ScheduledKind {
    LIGHT,
    IRON
}
