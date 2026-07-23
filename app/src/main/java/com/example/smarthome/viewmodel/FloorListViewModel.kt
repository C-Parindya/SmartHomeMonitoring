package com.example.smarthome.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarthome.data.model.Floor
import com.example.smarthome.data.repository.MockSmartHomeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FloorListUiState(
    val floors: List<Floor> = emptyList(),
    val showAddDialog: Boolean = false,
    val newFloorName: String = ""
)

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
    }

    fun showAddDialog() {
        _uiState.update { it.copy(showAddDialog = true, newFloorName = "") }
    }

    fun dismissAddDialog() {
        _uiState.update { it.copy(showAddDialog = false, newFloorName = "") }
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
}
