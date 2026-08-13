package com.example.smarthome.ui.screens.device

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Power
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import com.example.smarthome.data.model.DeviceState
import com.example.smarthome.ui.components.LoadingState
import com.example.smarthome.ui.components.SmartHomeTopBar
import com.example.smarthome.ui.components.StatusBadge
import com.example.smarthome.ui.components.TimePickerDialog
import com.example.smarthome.viewmodel.DeviceControlViewModel
import java.text.SimpleDateFormat
import java.util.Locale

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
                onSaveSchedule = viewModel::updateSchedule,
                onBack = onBack,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun OutletControlContent(
    device: Device,
    onToggle: () -> Unit,
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

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Schedule", style = MaterialTheme.typography.titleMedium)
                
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

        if (!device.state.isControllable) {
            Text(
                text = "Device is ${device.state} and cannot be controlled.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
