package com.example.smarthome.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarthome.data.model.UsageStat
import com.example.smarthome.data.repository.MockSmartHomeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UsageReportUiState(
    val stats: List<UsageStat> = emptyList(),
    val isLoading: Boolean = true
)

class UsageReportViewModel(
    private val repository: MockSmartHomeRepository = MockSmartHomeRepository.instance
) : ViewModel() {

    private val _uiState = MutableStateFlow(UsageReportUiState())
    val uiState: StateFlow<UsageReportUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.usageStats.collect { stats ->
                _uiState.update { it.copy(stats = stats, isLoading = false) }
            }
        }
    }
}
