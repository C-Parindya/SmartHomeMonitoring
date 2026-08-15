package com.example.smarthome.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smarthome.data.model.Device
import com.example.smarthome.data.repository.MockSmartHomeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DeviceControlUiState(
    val device: Device? = null,
    val isLoading: Boolean = true
)

class DeviceControlViewModel(
    private val deviceId: String,
    private val repository: MockSmartHomeRepository = MockSmartHomeRepository.instance
) : ViewModel() {

    private val _uiState = MutableStateFlow(DeviceControlUiState())
    val uiState: StateFlow<DeviceControlUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.floors.collect { floors ->
                val allDevices = floors.flatMap { it.devices + it.areas.flatMap { area -> area.devices } }
                val device = allDevices.find { it.id == deviceId }
                _uiState.update { it.copy(device = device, isLoading = false) }
            }
        }
    }

    fun toggleOutlet() = repository.toggleOutlet(deviceId)

    fun toggleSwitch(switchId: String) = repository.toggleSwitch(deviceId, switchId)

    fun toggleScheduledDevice() = repository.toggleScheduledDevice(deviceId)

    fun updateSchedule(maxDurationMinutes: Int, onTime: String?, offTime: String?) {
        repository.updateScheduledDevice(deviceId, maxDurationMinutes, onTime, offTime)
    }

    fun toggleCameraStream() = repository.toggleCameraStream(deviceId)

    fun toggleCameraPower() = repository.toggleCameraPower(deviceId)

    companion object {
        fun factory(
            deviceId: String,
            repository: MockSmartHomeRepository = MockSmartHomeRepository.instance
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return DeviceControlViewModel(deviceId, repository) as T
                }
            }
    }
}
