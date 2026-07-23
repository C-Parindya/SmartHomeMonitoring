package com.example.smarthome.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smarthome.data.model.Floor
import com.example.smarthome.data.repository.MockSmartHomeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FloorDetailUiState(
    val floor: Floor? = null,
    val isLoading: Boolean = true
)

class FloorDetailViewModel(
    private val floorId: String,
    private val repository: MockSmartHomeRepository = MockSmartHomeRepository.instance
) : ViewModel() {

    private val _uiState = MutableStateFlow(FloorDetailUiState())
    val uiState: StateFlow<FloorDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                repository.floors.collect { floors ->
                    val floor = floors.find { it.id == floorId }
                    _uiState.update { it.copy(floor = floor, isLoading = false) }
                }
            } catch (e: Exception) {
                // Handle error and show "floor not found"
                _uiState.update { it.copy(floor = null, isLoading = false) }
            }
        }
    }

    companion object {
        fun factory(
            floorId: String,
            repository: MockSmartHomeRepository = MockSmartHomeRepository.instance
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return FloorDetailViewModel(floorId, repository) as T
                }
            }
    }
}
