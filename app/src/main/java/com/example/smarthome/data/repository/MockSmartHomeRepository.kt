package com.example.smarthome.data.repository

import com.example.smarthome.data.model.Device
import com.example.smarthome.data.model.DeviceState
import com.example.smarthome.data.model.Floor
import com.example.smarthome.data.model.ScheduledKind
import com.example.smarthome.data.model.SwitchState
import com.example.smarthome.data.model.UsageStat
import com.example.smarthome.data.model.UserProfile
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MockSmartHomeRepository {

    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    private val _floors = MutableStateFlow(createSampleFloors())
    val floors: StateFlow<List<Floor>> = _floors.asStateFlow()

    private val _usageStats = MutableStateFlow(createSampleUsageStats())
    val usageStats: StateFlow<List<UsageStat>> = _usageStats.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    suspend fun login(email: String, password: String): Result<UserProfile> {
        _isLoading.value = true
        delay(800)
        _isLoading.value = false
        return if (email.isNotBlank() && password.isNotBlank()) {
            val user = UserProfile(
                email = email.trim(),
                displayName = email.substringBefore("@").replaceFirstChar { it.uppercase() }
            )
            _currentUser.value = user
            Result.success(user)
        } else {
            Result.failure(IllegalArgumentException("Email and password are required"))
        }
    }

    fun logout() {
        _currentUser.value = null
    }

    fun getFloor(floorId: String): Floor? = _floors.value.find { it.id == floorId }

    fun getDevice(deviceId: String): Device? {
        return _floors.value
            .flatMap { it.devices }
            .find { it.id == deviceId }
    }

    fun addFloor(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        val newFloor = Floor(
            id = "floor_${System.currentTimeMillis()}",
            name = trimmed,
            devices = emptyList()
        )
        _floors.update { it + newFloor }
    }

    fun toggleOutlet(deviceId: String) {
        updateDevice<Device.Outlet>(deviceId) { device ->
            if (!device.state.isControllable) return@updateDevice device
            val newState = if (device.state == DeviceState.ON) DeviceState.OFF else DeviceState.ON
            device.copy(state = newState)
        }
    }

    fun toggleSwitch(deviceId: String, switchId: String) {
        updateDevice<Device.MultiSwitch>(deviceId) { device ->
            if (!device.state.isControllable) return@updateDevice device
            val updatedSwitches = device.switches.map { switch ->
                if (switch.id == switchId) switch.copy(isOn = !switch.isOn) else switch
            }
            val anyOn = updatedSwitches.any { it.isOn }
            device.copy(
                switches = updatedSwitches,
                state = if (anyOn) DeviceState.ON else DeviceState.OFF
            )
        }
    }

    fun toggleScheduledDevice(deviceId: String) {
        updateDevice<Device.ScheduledDevice>(deviceId) { device ->
            if (!device.state.isControllable) return@updateDevice device
            val newState = if (device.state == DeviceState.ON) DeviceState.OFF else DeviceState.ON
            device.copy(state = newState)
        }
    }

    fun updateScheduledDevice(
        deviceId: String,
        maxDurationMinutes: Int,
        onTime: String?,
        offTime: String?
    ) {
        updateDevice<Device.ScheduledDevice>(deviceId) { device ->
            device.copy(
                maxDurationMinutes = maxDurationMinutes.coerceIn(1, 480),
                onTime = onTime,
                offTime = offTime
            )
        }
    }

    fun toggleCameraStream(deviceId: String) {
        updateDevice<Device.Camera>(deviceId) { device ->
            if (device.state == DeviceState.DISCONNECTED) return@updateDevice device
            device.copy(isStreaming = !device.isStreaming)
        }
    }

    private inline fun <reified T : Device> updateDevice(
        deviceId: String,
        crossinline transform: (T) -> T
    ) {
        _floors.update { floors ->
            floors.map { floor ->
                floor.copy(
                    devices = floor.devices.map { device ->
                        if (device.id == deviceId && device is T) {
                            transform(device)
                        } else {
                            device
                        }
                    }
                )
            }
        }
    }

    companion object {
        val instance = MockSmartHomeRepository()

        private fun createSampleFloors(): List<Floor> = listOf(
            Floor(
                id = "floor_ground",
                name = "Ground Floor",
                devices = listOf(
                    Device.Outlet("outlet_living", "Living Room Outlet", "floor_ground", 1, 2, DeviceState.ON),
                    Device.MultiSwitch(
                        id = "ms_kitchen",
                        name = "Kitchen Gang Box",
                        floorId = "floor_ground",
                        row = 3,
                        col = 4,
                        state = DeviceState.ON,
                        switches = listOf(
                            SwitchState("sw1", "Ceiling Light", true),
                            SwitchState("sw2", "Extractor Fan", false),
                            SwitchState("sw3", "Under-cabinet", true)
                        )
                    ),
                    Device.Camera(
                        id = "cam_front",
                        name = "Front Door Camera",
                        floorId = "floor_ground",
                        row = 0,
                        col = 6,
                        state = DeviceState.ON,
                        snapshotUrl = null,
                        isStreaming = true
                    ),
                    Device.Outlet("outlet_hall", "Hallway Outlet", "floor_ground", 5, 1, DeviceState.OFF)
                )
            ),
            Floor(
                id = "floor_first",
                name = "First Floor",
                devices = listOf(
                    Device.ScheduledDevice(
                        id = "sched_bedroom_light",
                        name = "Bedroom Light",
                        floorId = "floor_first",
                        row = 2,
                        col = 3,
                        state = DeviceState.OFF,
                        deviceKind = ScheduledKind.LIGHT,
                        maxDurationMinutes = 120,
                        onTime = "18:00",
                        offTime = "23:00"
                    ),
                    Device.Outlet("outlet_office", "Office Outlet", "floor_first", 4, 5, DeviceState.ON),
                    Device.MultiSwitch(
                        id = "ms_bathroom",
                        name = "Bathroom Gang Box",
                        floorId = "floor_first",
                        row = 1,
                        col = 7,
                        state = DeviceState.OFF,
                        switches = listOf(
                            SwitchState("sw1", "Vanity Light", false),
                            SwitchState("sw2", "Shower Light", false),
                            SwitchState("sw3", "Exhaust Fan", false),
                            SwitchState("sw4", "Heated Towel", false),
                            SwitchState("sw5", "Night Light", true)
                        )
                    ),
                    Device.Camera(
                        id = "cam_baby",
                        name = "Nursery Camera",
                        floorId = "floor_first",
                        row = 6,
                        col = 2,
                        state = DeviceState.ERROR,
                        snapshotUrl = null,
                        isStreaming = false
                    )
                )
            ),
            Floor(
                id = "floor_second",
                name = "Second Floor",
                devices = listOf(
                    Device.ScheduledDevice(
                        id = "sched_iron",
                        name = "Laundry Iron",
                        floorId = "floor_second",
                        row = 3,
                        col = 1,
                        state = DeviceState.OFF,
                        deviceKind = ScheduledKind.IRON,
                        maxDurationMinutes = 45,
                        onTime = "07:00",
                        offTime = "09:00"
                    ),
                    Device.Outlet("outlet_attic", "Attic Outlet", "floor_second", 0, 0, DeviceState.DISCONNECTED),
                    Device.MultiSwitch(
                        id = "ms_loft",
                        name = "Loft Gang Box",
                        floorId = "floor_second",
                        row = 5,
                        col = 5,
                        state = DeviceState.ON,
                        switches = listOf(
                            SwitchState("sw1", "Main Light", true),
                            SwitchState("sw2", "Desk Lamp", false)
                        )
                    ),
                    Device.Camera(
                        id = "cam_roof",
                        name = "Rooftop Camera",
                        floorId = "floor_second",
                        row = 7,
                        col = 7,
                        state = DeviceState.ON,
                        snapshotUrl = null,
                        isStreaming = false
                    )
                )
            )
        )

        private fun createSampleUsageStats(): List<UsageStat> = listOf(
            UsageStat("outlet_living", "Living Room Outlet", "Outlet", "Ground Floor", 340, System.currentTimeMillis() - 3_600_000),
            UsageStat("ms_kitchen", "Kitchen Gang Box", "Multi-Switch", "Ground Floor", 520, System.currentTimeMillis() - 7_200_000),
            UsageStat("sched_bedroom_light", "Bedroom Light", "Scheduled Light", "First Floor", 180, System.currentTimeMillis() - 86_400_000),
            UsageStat("sched_iron", "Laundry Iron", "Scheduled Iron", "Second Floor", 45, System.currentTimeMillis() - 172_800_000),
            UsageStat("cam_front", "Front Door Camera", "Camera", "Ground Floor", 1440, System.currentTimeMillis() - 1_800_000),
            UsageStat("outlet_office", "Office Outlet", "Outlet", "First Floor", 600, System.currentTimeMillis() - 10_800_000)
        )
    }
}
