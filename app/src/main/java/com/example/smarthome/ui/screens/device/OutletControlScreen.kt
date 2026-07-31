package com.example.smarthome.ui.screens.device

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Power
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smarthome.data.model.Device
import com.example.smarthome.data.model.DeviceState
import com.example.smarthome.ui.components.LoadingState
import com.example.smarthome.ui.components.SmartHomeTopBar
import com.example.smarthome.ui.components.StatusBadge
import com.example.smarthome.viewmodel.DeviceControlViewModel

@Composable
fun OutletControlScreen(
    deviceId: String,
    onBack: () -> Unit,
    viewModel: DeviceControlViewModel = viewModel(factory = DeviceControlViewModel.factory(deviceId))
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val device = uiState.device

    Scaffold(
        topBar = {
            SmartHomeTopBar(
                title = device?.name ?: "Outlet",
                showBack = true,
                onBack = onBack
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> LoadingState(modifier = Modifier.padding(padding))
            device == null -> Text(
                text = "Device not found",
                modifier = Modifier.padding(padding).padding(16.dp)
            )
            else -> OutletControlContent(
                device = device,
                onToggle = viewModel::toggleOutlet,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun OutletControlContent(
    device: Device,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Power,
                    contentDescription = null,
                    modifier = Modifier.padding(bottom = 16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                StatusBadge(state = device.state)
                Text(
                    text = if (device.state == DeviceState.ON) "Outlet is ON" else "Outlet is OFF",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Power", style = MaterialTheme.typography.titleMedium)
                Switch(
                    checked = device.state == DeviceState.ON,
                    onCheckedChange = { if (device.state.isControllable) onToggle() },
                    enabled = device.state.isControllable
                )
            }
        }

        if (!device.state.isControllable) {
            Text(
                text = "Device is ${device.state.toLabel()} and cannot be controlled.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private fun DeviceState.toLabel(): String = name
