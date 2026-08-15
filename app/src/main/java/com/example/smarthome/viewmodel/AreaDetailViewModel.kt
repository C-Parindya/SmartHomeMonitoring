package com.example.smarthome.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smarthome.data.model.Area
import com.example.smarthome.data.model.Device
import com.example.smarthome.data.model.DeviceState
import com.example.smarthome.data.model.DeviceType
import com.example.smarthome.data.model.ScheduledKind
import com.example.smarthome.data.repository.MockSmartHomeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AreaDetailUiState(
    val area: Area? = null,
    val isLoading: Boolean = false,
    val showAddDeviceDialog: Boolean = false,
    val showDeviceDetailsDialog: Boolean = false,
    val showEditDeviceDialog: Boolean = false,
    val showMultiSwitchDialog: Boolean = false,
    val selectedDevice: Device? = null,
    val selectedCell: Pair<Int, Int>? = null,
    val newDeviceName: String = "",
    val newDeviceType: String = "Bulb",
    val newDeviceTimer: Int = 0,
    val newDeviceSwitchCount: Int = 1
)

class AreaDetailViewModel(
    private val floorId: String,
    private val areaId: String,
    private val repository: MockSmartHomeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AreaDetailUiState(isLoading = true))
    val uiState: StateFlow<AreaDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.floors.collect { floors ->
                val floor = floors.find { it.id == floorId }
                val area = floor?.areas?.find { it.id == areaId }
                _uiState.update { it.copy(area = area, isLoading = false) }
            }
        }
    }

    fun onCellClick(row: Int, col: Int) {
        val existingDevice = _uiState.value.area?.devices?.find { it.row == row && it.col == col }
        if (existingDevice == null) {
            _uiState.update { it.copy(
                showAddDeviceDialog = true, 
                selectedCell = row to col, 
                newDeviceName = "",
                newDeviceType = "Bulb",
                newDeviceTimer = 0,
                newDeviceSwitchCount = 1
            ) }
        }
    }

    fun onDeviceClick(device: Device) {
        _uiState.update { it.copy(showDeviceDetailsDialog = true, selectedDevice = device) }
    }

    fun onEditDeviceClick(device: Device) {
        val timer = if (device.deviceKind == ScheduledKind.IRON && device.maxDurationMinutes == 0) 10 else device.maxDurationMinutes
        _uiState.update { it.copy(
            showEditDeviceDialog = true, 
            showDeviceDetailsDialog = false, 
            selectedDevice = device, 
            newDeviceName = device.name,
            newDeviceTimer = timer
        ) }
    }

    fun dismissDeviceDetailsDialog() {
        _uiState.update { it.copy(
            showDeviceDetailsDialog = false, 
            showEditDeviceDialog = false, 
            showMultiSwitchDialog = false,
            selectedDevice = null, 
            newDeviceTimer = 0
        ) }
    }

    fun deleteDevice() {
        val deviceId = _uiState.value.selectedDevice?.id ?: return
        repository.deleteDevice(floorId, areaId, deviceId)
        dismissDeviceDetailsDialog()
    }

    fun updateDevice() {
        val state = _uiState.value
        val deviceId = state.selectedDevice?.id ?: return
        if (state.newDeviceName.isNotBlank()) {
            repository.editDevice(floorId, areaId, deviceId, state.newDeviceName, state.newDeviceTimer)
            dismissDeviceDetailsDialog()
        }
    }

    fun dismissAddDeviceDialog() {
        _uiState.update { it.copy(showAddDeviceDialog = false, selectedCell = null, newDeviceTimer = 0) }
    }

    fun onNewDeviceNameChange(name: String) {
        _uiState.update { it.copy(newDeviceName = name) }
    }

    fun onNewDeviceTimerChange(minutes: Int) {
        _uiState.update { it.copy(newDeviceTimer = minutes) }
    }

    fun onNewDeviceTypeChange(type: String) {
        _uiState.update { 
            it.copy(
                newDeviceType = type,
                newDeviceTimer = if (type == "Iron") 10 else it.newDeviceTimer,
                newDeviceSwitchCount = if (type == "Switch") 4 else 1
            ) 
        }
    }

    fun onNewDeviceSwitchCountChange(count: Int) {
        _uiState.update { it.copy(newDeviceSwitchCount = count) }
    }

    fun toggleDevice(device: Device) {
        when (device.type) {
            DeviceType.OUTLET -> repository.toggleOutlet(device.id)
            DeviceType.SCHEDULED_DEVICE -> repository.toggleScheduledDevice(device.id)
            DeviceType.CAMERA -> repository.toggleCameraPower(device.id)
            DeviceType.MULTI_SWITCH -> {
                _uiState.update { it.copy(showMultiSwitchDialog = true, selectedDevice = device) }
            }
        }
    }

    fun toggleIndividualSwitch(switchId: String) {
        val deviceId = _uiState.value.selectedDevice?.id ?: return
        repository.toggleSwitch(deviceId, switchId)
    }

    fun addDevice() {
        val state = _uiState.value
        val cell = state.selectedCell ?: return
        if (state.newDeviceName.isNotBlank()) {
            val deviceId = "dev_${System.currentTimeMillis()}"
            val device = when (state.newDeviceType) {
                "Bulb" -> Device(
                    id = deviceId, 
                    name = state.newDeviceName, 
                    floorId = floorId, 
                    areaId = areaId, 
                    row = cell.first, 
                    col = cell.second, 
                    type = DeviceType.SCHEDULED_DEVICE, 
                    deviceKind = ScheduledKind.LIGHT,
                    state = DeviceState.OFF,
                    maxDurationMinutes = state.newDeviceTimer
                )
                "Iron" -> Device(
                    id = deviceId, 
                    name = state.newDeviceName, 
                    floorId = floorId, 
                    areaId = areaId, 
                    row = cell.first, 
                    col = cell.second, 
                    type = DeviceType.SCHEDULED_DEVICE, 
                    deviceKind = ScheduledKind.IRON,
                    state = DeviceState.OFF,
                    maxDurationMinutes = state.newDeviceTimer
                )
                "Camera" -> Device(
                    id = deviceId, 
                    name = state.newDeviceName, 
                    floorId = floorId, 
                    areaId = areaId, 
                    row = cell.first, 
                    col = cell.second, 
                    type = DeviceType.CAMERA,
                    state = DeviceState.OFF,
                    maxDurationMinutes = state.newDeviceTimer
                )
                "Fan" -> Device(
                    id = deviceId, 
                    name = state.newDeviceName, 
                    floorId = floorId, 
                    areaId = areaId, 
                    row = cell.first, 
                    col = cell.second, 
                    type = DeviceType.SCHEDULED_DEVICE, 
                    deviceKind = ScheduledKind.FAN,
                    state = DeviceState.OFF,
                    maxDurationMinutes = state.newDeviceTimer
                )
                "Switch" -> Device(
                    id = deviceId, 
                    name = state.newDeviceName, 
                    floorId = floorId, 
                    areaId = areaId, 
                    row = cell.first, 
                    col = cell.second, 
                    type = DeviceType.MULTI_SWITCH, 
                    switches = (1..state.newDeviceSwitchCount).map { i ->
                        com.example.smarthome.data.model.SwitchState("s$i", "Switch $i", false)
                    },
                    state = DeviceState.OFF,
                    maxDurationMinutes = state.newDeviceTimer
                )
                else -> Device(
                    id = deviceId, 
                    name = state.newDeviceName, 
                    floorId = floorId, 
                    areaId = areaId, 
                    row = cell.first, 
                    col = cell.second, 
                    type = DeviceType.OUTLET,
                    state = DeviceState.OFF,
                    maxDurationMinutes = state.newDeviceTimer
                )
            }
            repository.addDeviceToArea(floorId, areaId, device)
            dismissAddDeviceDialog()
        }
    }

    companion object {
        fun factory(
            floorId: String,
            areaId: String,
            repository: MockSmartHomeRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AreaDetailViewModel(floorId, areaId, repository) as T
            }
        }
    }
}
