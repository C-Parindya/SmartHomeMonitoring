package com.example.smarthome.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smarthome.data.model.Area
import com.example.smarthome.data.model.Device
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
    val selectedCell: Pair<Int, Int>? = null,
    val newDeviceName: String = "",
    val newDeviceType: String = "Outlet"
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
            _uiState.update { it.copy(showAddDeviceDialog = true, selectedCell = row to col, newDeviceName = "") }
        }
    }

    fun dismissAddDeviceDialog() {
        _uiState.update { it.copy(showAddDeviceDialog = false, selectedCell = null) }
    }

    fun onNewDeviceNameChange(name: String) {
        _uiState.update { it.copy(newDeviceName = name) }
    }

    fun onNewDeviceTypeChange(type: String) {
        _uiState.update { it.copy(newDeviceType = type) }
    }

    fun addDevice() {
        val state = _uiState.value
        val cell = state.selectedCell ?: return
        if (state.newDeviceName.isNotBlank()) {
            val deviceId = "dev_${System.currentTimeMillis()}"
            val device = when (state.newDeviceType) {
                "Outlet" -> Device(id = deviceId, name = state.newDeviceName, floorId = floorId, areaId = areaId, row = cell.first, col = cell.second, type = DeviceType.OUTLET)
                "Camera" -> Device(id = deviceId, name = state.newDeviceName, floorId = floorId, areaId = areaId, row = cell.first, col = cell.second, type = DeviceType.CAMERA)
                "Switch" -> Device(id = deviceId, name = state.newDeviceName, floorId = floorId, areaId = areaId, row = cell.first, col = cell.second, type = DeviceType.MULTI_SWITCH, switches = emptyList())
                "Light" -> Device(id = deviceId, name = state.newDeviceName, floorId = floorId, areaId = areaId, row = cell.first, col = cell.second, type = DeviceType.SCHEDULED_DEVICE, deviceKind = ScheduledKind.LIGHT, maxDurationMinutes = 60)
                else -> Device(id = deviceId, name = state.newDeviceName, floorId = floorId, areaId = areaId, row = cell.first, col = cell.second, type = DeviceType.OUTLET)
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
