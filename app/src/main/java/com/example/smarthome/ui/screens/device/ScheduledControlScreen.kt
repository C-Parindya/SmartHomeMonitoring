package com.example.smarthome.ui.screens.device

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Iron
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smarthome.data.model.Device
import com.example.smarthome.data.model.DeviceState
import com.example.smarthome.data.model.ScheduledKind
import com.example.smarthome.ui.components.LoadingState
import com.example.smarthome.ui.components.SmartHomeTopBar
import com.example.smarthome.ui.components.StatusBadge
import com.example.smarthome.viewmodel.DeviceControlViewModel

@Composable
fun ScheduledControlScreen(
    deviceId: String,
    onBack: () -> Unit,
    viewModel: DeviceControlViewModel = viewModel(factory = DeviceControlViewModel.factory(deviceId))
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val device = uiState.device

    Scaffold(
        topBar = {
            SmartHomeTopBar(
                title = device?.name ?: "Scheduled Device",
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
            else -> ScheduledControlContent(
                device = device,
                onToggle = viewModel::toggleScheduledDevice,
                onSaveSchedule = viewModel::updateSchedule,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun ScheduledControlContent(
    device: Device,
    onToggle: () -> Unit,
    onSaveSchedule: (Int, String?, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var maxDuration by rememberSaveable(device.id) { mutableStateOf(device.maxDurationMinutes.toFloat()) }
    var onTime by rememberSaveable(device.id) { mutableStateOf(device.onTime ?: "") }
    var offTime by rememberSaveable(device.id) { mutableStateOf(device.offTime ?: "") }

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
                    imageVector = if (device.deviceKind == ScheduledKind.IRON) {
                        Icons.Outlined.Iron
                    } else {
                        Icons.Outlined.Lightbulb
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                StatusBadge(
                    state = device.state,
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

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Max Duration: ${maxDuration.toInt()} min",
                    style = MaterialTheme.typography.titleSmall
                )
                Slider(
                    value = maxDuration,
                    onValueChange = { maxDuration = it },
                    valueRange = 5f..240f,
                    steps = 46,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Schedule", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = onTime,
                    onValueChange = { onTime = it },
                    label = { Text("On Time (HH:mm)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = offTime,
                    onValueChange = { offTime = it },
                    label = { Text("Off Time (HH:mm)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Button(
                    onClick = {
                        onSaveSchedule(
                            maxDuration.toInt(),
                            onTime.ifBlank { null },
                            offTime.ifBlank { null }
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Schedule")
                }
            }
        }
    }
}
