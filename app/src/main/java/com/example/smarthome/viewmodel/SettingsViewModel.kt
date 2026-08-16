package com.example.smarthome.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarthome.data.model.UsageStat
import com.example.smarthome.data.model.UserProfile
import com.example.smarthome.data.repository.MockSmartHomeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val user: UserProfile? = null,
    val usageStats: List<UsageStat> = emptyList()
)

class ProfileViewModel(
    private val repository: MockSmartHomeRepository = MockSmartHomeRepository.instance
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.currentUser.collect { user ->
                _uiState.update { it.copy(user = user) }
            }
        }
        viewModelScope.launch {
            repository.usageStats.collect { stats ->
                _uiState.update { it.copy(usageStats = stats) }
            }
        }
    }

    fun logout() {
        repository.logout()
    }

    fun updateProfile(displayName: String, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val result = repository.updateProfile(displayName)
            onResult(result)
        }
    }

    fun sendPasswordReset(onResult: (Result<Unit>) -> Unit) {
        val email = uiState.value.user?.email ?: return
        viewModelScope.launch {
            val result = repository.sendPasswordResetEmail(email)
            onResult(result)
        }
    }
}
