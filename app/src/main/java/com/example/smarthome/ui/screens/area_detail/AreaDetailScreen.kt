package com.example.smarthome.ui.screens.area_detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.smarthome.data.model.Device
import com.example.smarthome.ui.components.DeviceGridCell
import com.example.smarthome.ui.components.EmptyState
import com.example.smarthome.ui.components.LoadingState
import com.example.smarthome.ui.components.SmartHomeTopBar
import com.example.smarthome.viewmodel.AreaDetailViewModel

@Composable
fun AreaDetailScreen(
    floorId: String,
    areaId: String,
    onBack: () -> Unit,
    viewModel: AreaDetailViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            SmartHomeTopBar(
                title = uiState.area?.name ?: "Area",
                showBack = true,
                onBack = onBack
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> LoadingState(modifier = Modifier.padding(padding))
            uiState.area == null -> EmptyState(
                title = "Area not found",
                message = "This area may have been removed",
                modifier = Modifier.padding(padding)
            )
            else -> {
                val area = uiState.area!!
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Text(
                            text = "Grid Layout",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Tap cell to toggle, tap edit icon for details.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    item {
                        Card(
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                for (row in 0 until area.gridRows) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        for (col in 0 until area.gridCols) {
                                            val device = area.devices.find { it.row == row && it.col == col }
                                            Box(modifier = Modifier.weight(1f)) {
                                                if (device != null) {
                                                    DeviceGridCell(
                                                        device = device,
                                                        onClick = { viewModel.onDeviceClick(it) },
                                                        onToggle = { viewModel.toggleDevice(it) }
                                                    )
                                                } else {
                                                    EmptyGridCell(onClick = { viewModel.onCellClick(row, col) })
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (uiState.showAddDeviceDialog) {
        AddDeviceDialog(
            name = uiState.newDeviceName,
            onNameChange = viewModel::onNewDeviceNameChange,
            type = uiState.newDeviceType,
            onTypeChange = viewModel::onNewDeviceTypeChange,
            timerMinutes = uiState.newDeviceTimer,
            onTimerChange = viewModel::onNewDeviceTimerChange,
            onDismiss = viewModel::dismissAddDeviceDialog,
            onConfirm = viewModel::addDevice
        )
    }

    if (uiState.showDeviceDetailsDialog && uiState.selectedDevice != null) {
        DeviceDetailsDialog(
            device = uiState.selectedDevice!!,
            onDismiss = viewModel::dismissDeviceDetailsDialog,
            onEdit = { viewModel.onEditDeviceClick(it) },
            onDelete = viewModel::deleteDevice
        )
    }

    if (uiState.showEditDeviceDialog && uiState.selectedDevice != null) {
        EditDeviceDialog(
            name = uiState.newDeviceName,
            onNameChange = viewModel::onNewDeviceNameChange,
            timerMinutes = uiState.newDeviceTimer,
            onTimerChange = viewModel::onNewDeviceTimerChange,
            onDismiss = viewModel::dismissDeviceDetailsDialog,
            onConfirm = viewModel::updateDevice
        )
    }
}

@Composable
private fun EmptyGridCell(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text("+", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.outline)
    }
}

@Composable
private fun DeviceDetailsDialog(
    device: Device,
    onDismiss: () -> Unit,
    onEdit: (Device) -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Device Details") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Name: ${device.name}", style = MaterialTheme.typography.bodyLarge)
                Text("Type: ${device.type}", style = MaterialTheme.typography.bodyMedium)
                Text("Status: ${device.state}", style = MaterialTheme.typography.bodyMedium)
                if (device.maxDurationMinutes > 0) {
                    Text("Auto-Off Timer: ${device.maxDurationMinutes} mins", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = { onEdit(device) }) {
                    Text("Edit")
                }
                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            }
        }
    )
}

@Composable
private fun EditDeviceDialog(
    name: String,
    onNameChange: (String) -> Unit,
    timerMinutes: Int,
    onTimerChange: (Int) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Device") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("Device Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = if (timerMinutes == 0) "" else timerMinutes.toString(),
                    onValueChange = { onTimerChange(it.toIntOrNull() ?: 0) },
                    label = { Text("Auto-Off Timer (minutes)") },
                    placeholder = { Text("0 for no timer") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = name.isNotBlank()) {
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun AddDeviceDialog(
    name: String,
    onNameChange: (String) -> Unit,
    type: String,
    onTypeChange: (String) -> Unit,
    timerMinutes: Int,
    onTimerChange: (Int) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Device") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("Device Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text("Type:", style = MaterialTheme.typography.labelMedium)
                val types = listOf("Bulb", "Iron", "Camera", "Switch", "Fan")
                
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    types.forEach { deviceType ->
                        FilterChip(
                            selected = type == deviceType,
                            onClick = { onTypeChange(deviceType) },
                            label = { Text(deviceType) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = if (timerMinutes == 0) "" else timerMinutes.toString(),
                    onValueChange = { onTimerChange(it.toIntOrNull() ?: 0) },
                    label = { Text("Auto-Off Timer (minutes)") },
                    placeholder = { Text("0 for no timer") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = name.isNotBlank()) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
