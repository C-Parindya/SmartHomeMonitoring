package com.example.smarthome.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarthome.data.model.Device
import com.example.smarthome.data.model.DeviceType
import com.example.smarthome.data.model.Floor
import com.example.smarthome.data.model.UserProfile
import com.example.smarthome.data.repository.MockSmartHomeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FloorListUiState(
    val floors: List<Floor> = emptyList(),
    val user: UserProfile? = null,
    val showAddDialog: Boolean = false,
    val showEditDialog: Boolean = false,
    val selectedFloor: Floor? = null,
    val newFloorName: String = ""
) {
    val allDevices: List<Device> get() = floors.flatMap { it.areas }.flatMap { it.devices }
}

class FloorListViewModel(
    private val repository: MockSmartHomeRepository = MockSmartHomeRepository.instance
) : ViewModel() {

    private val _uiState = MutableStateFlow(FloorListUiState())
    val uiState: StateFlow<FloorListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.floors.collect { floors ->
                _uiState.update { it.copy(floors = floors) }
            }
        }
        viewModelScope.launch {
            repository.currentUser.collect { user ->
                _uiState.update { it.copy(user = user) }
            }
        }
    }

    fun showAddDialog() {
        _uiState.update { it.copy(showAddDialog = true, showEditDialog = false, newFloorName = "", selectedFloor = null) }
    }

    fun showEditDialog(floor: Floor) {
        _uiState.update { it.copy(showEditDialog = true, showAddDialog = false, newFloorName = floor.name, selectedFloor = floor) }
    }

    fun dismissAddDialog() {
        _uiState.update { it.copy(showAddDialog = false, showEditDialog = false, newFloorName = "", selectedFloor = null) }
    }

    fun onNewFloorNameChange(name: String) {
        _uiState.update { it.copy(newFloorName = name) }
    }

    fun addFloor() {
        val name = _uiState.value.newFloorName
        if (name.isNotBlank()) {
            repository.addFloor(name)
            dismissAddDialog()
        }
    }

    fun updateFloor() {
        val state = _uiState.value
        val floorId = state.selectedFloor?.id ?: return
        if (state.newFloorName.isNotBlank()) {
            repository.editFloor(floorId, state.newFloorName)
            dismissAddDialog()
        }
    }

    fun deleteFloor(floorId: String) {
        repository.deleteFloor(floorId)
    }

    fun toggleDevice(device: Device) {
        when (device.type) {
            DeviceType.OUTLET -> repository.toggleOutlet(device.id)
            DeviceType.SCHEDULED_DEVICE -> repository.toggleScheduledDevice(device.id)
            DeviceType.CAMERA -> repository.toggleCameraStream(device.id)
            DeviceType.MULTI_SWITCH -> {
                // For simplicity on home screen, toggle all switches or just the first one?
                // Usually multi-switch might need specific control, but let's toggle first switch for quick action
                device.switches.firstOrNull()?.let { 
                    repository.toggleSwitch(device.id, it.id)
                }
            }
        }
    }
}
