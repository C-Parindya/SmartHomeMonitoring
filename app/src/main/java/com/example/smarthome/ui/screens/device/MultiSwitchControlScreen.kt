package com.example.smarthome.ui.screens.device

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import com.example.smarthome.ui.components.LoadingState
import com.example.smarthome.ui.components.SmartHomeTopBar
import com.example.smarthome.ui.components.StatusBadge
import com.example.smarthome.viewmodel.DeviceControlViewModel

@Composable
fun MultiSwitchControlScreen(
    deviceId: String,
    onBack: () -> Unit,
    viewModel: DeviceControlViewModel = viewModel(factory = DeviceControlViewModel.factory(deviceId))
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val device = uiState.device as? Device.MultiSwitch

    Scaffold(
        topBar = {
            SmartHomeTopBar(
                title = device?.name ?: "Multi-Switch",
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
            else -> MultiSwitchContent(
                device = device,
                onToggleSwitch = viewModel::toggleSwitch,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun MultiSwitchContent(
    device: Device.MultiSwitch,
    onToggleSwitch: (String) -> Unit,
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Gang Box", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "${device.switches.count { it.isOn }} of ${device.switches.size} on",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusBadge(state = device.state)
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(8.dp)) {
                device.switches.forEachIndexed { index, switch ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(switch.name, style = MaterialTheme.typography.titleSmall)
                            Text(
                                text = if (switch.isOn) "ON" else "OFF",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = switch.isOn,
                            onCheckedChange = { onToggleSwitch(switch.id) },
                            enabled = device.state.isControllable
                        )
                    }
                    if (index < device.switches.lastIndex) {
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
