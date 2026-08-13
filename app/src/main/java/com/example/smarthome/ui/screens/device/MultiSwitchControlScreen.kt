package com.example.smarthome.ui.screens.device

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smarthome.data.model.Device
import com.example.smarthome.ui.components.LoadingState
import com.example.smarthome.ui.components.SmartHomeTopBar
import com.example.smarthome.ui.components.StatusBadge
import com.example.smarthome.ui.components.TimePickerDialog
import com.example.smarthome.viewmodel.DeviceControlViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun MultiSwitchControlScreen(
    deviceId: String,
    onBack: () -> Unit,
    viewModel: DeviceControlViewModel = viewModel(factory = DeviceControlViewModel.factory(deviceId))
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val device = uiState.device

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
                onSaveSchedule = viewModel::updateSchedule,
                onBack = onBack,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun MultiSwitchContent(
    device: Device,
    onToggleSwitch: (String) -> Unit,
    onSaveSchedule: (Int, String?, String?) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var onTime by rememberSaveable(device.id) { mutableStateOf(device.onTime ?: "") }
    var offTime by rememberSaveable(device.id) { mutableStateOf(device.offTime ?: "") }

    val format24 = SimpleDateFormat("HH:mm", Locale.getDefault())
    val format12 = SimpleDateFormat("hh:mm a", Locale.getDefault())

    fun formatToDisplay(timeStr: String): String {
        if (timeStr.isBlank()) return "Not set"
        return try {
            val date = format24.parse(timeStr)
            format12.format(date!!)
        } catch (e: Exception) {
            timeStr
        }
    }

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

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Schedule (All Switches)", style = MaterialTheme.typography.titleMedium)
                
                var showOnPicker by remember { mutableStateOf(false) }
                var showOffPicker by remember { mutableStateOf(false) }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val onInteractionSource = remember { MutableInteractionSource() }
                    val isOnPressed by onInteractionSource.collectIsPressedAsState()
                    LaunchedEffect(isOnPressed) { if (isOnPressed) showOnPicker = true }

                    OutlinedTextField(
                        value = formatToDisplay(onTime),
                        onValueChange = { },
                        label = { Text("On Time") },
                        modifier = Modifier.weight(1f),
                        readOnly = true,
                        interactionSource = onInteractionSource
                    )

                    val offInteractionSource = remember { MutableInteractionSource() }
                    val isOffPressed by offInteractionSource.collectIsPressedAsState()
                    LaunchedEffect(isOffPressed) { if (isOffPressed) showOffPicker = true }

                    OutlinedTextField(
                        value = formatToDisplay(offTime),
                        onValueChange = { },
                        label = { Text("Off Time") },
                        modifier = Modifier.weight(1f),
                        readOnly = true,
                        interactionSource = offInteractionSource
                    )
                }
                
                if (showOnPicker) {
                    TimePickerDialog(
                        onDismiss = { showOnPicker = false },
                        onConfirm = { h, m ->
                            onTime = "%02d:%02d".format(h, m)
                            showOnPicker = false
                        }
                    )
                }
                
                if (showOffPicker) {
                    TimePickerDialog(
                        onDismiss = { showOffPicker = false },
                        onConfirm = { h, m ->
                            offTime = "%02d:%02d".format(h, m)
                            showOffPicker = false
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            onSaveSchedule(
                                device.maxDurationMinutes,
                                onTime.ifBlank { null },
                                offTime.ifBlank { null }
                            )
                            onBack()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save Schedule")
                    }
                    
                    TextButton(
                        onClick = {
                            onTime = ""
                            offTime = ""
                            onSaveSchedule(device.maxDurationMinutes, null, null)
                        }
                    ) {
                        Text("Clear")
                    }
                }
            }
        }
    }
}
