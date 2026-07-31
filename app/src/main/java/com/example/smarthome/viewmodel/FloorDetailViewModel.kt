package com.example.smarthome.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smarthome.data.model.Area
import com.example.smarthome.data.model.Floor
import com.example.smarthome.data.repository.MockSmartHomeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FloorDetailUiState(
    val floor: Floor? = null,
    val isLoading: Boolean = false,
    val showAddAreaDialog: Boolean = false,
    val newAreaName: String = "",
    val newAreaType: String = "Room"
)

class FloorDetailViewModel(
    private val floorId: String,
    private val repository: MockSmartHomeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FloorDetailUiState(isLoading = true))
    val uiState: StateFlow<FloorDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.floors.collect { floors ->
                val floor = floors.find { it.id == floorId }
                _uiState.update { it.copy(floor = floor, isLoading = false) }
            }
        }
    }

    fun showAddAreaDialog() {
        _uiState.update { it.copy(showAddAreaDialog = true, newAreaName = "", newAreaType = "Room") }
    }

    fun dismissAddAreaDialog() {
        _uiState.update { it.copy(showAddAreaDialog = false) }
    }

    fun onNewAreaNameChange(name: String) {
        _uiState.update { it.copy(newAreaName = name) }
    }

    fun onNewAreaTypeChange(type: String) {
        _uiState.update { it.copy(newAreaType = type) }
    }

    fun addArea() {
        val state = _uiState.value
        if (state.newAreaName.isNotBlank()) {
            repository.addArea(floorId, state.newAreaName, state.newAreaType)
            dismissAddAreaDialog()
        }
    }

    companion object {
        fun factory(
            floorId: String,
            repository: MockSmartHomeRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return FloorDetailViewModel(floorId, repository) as T
            }
        }
    }
}
