package com.example.smarthome.ui.screens.floor_detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smarthome.data.model.Device
import com.example.smarthome.data.model.DeviceState
import com.example.smarthome.ui.components.DeviceGridCell
import com.example.smarthome.ui.components.EmptyState
import com.example.smarthome.ui.components.LoadingState
import com.example.smarthome.ui.components.SmartHomeTopBar
import com.example.smarthome.ui.components.StatusBadge
import com.example.smarthome.viewmodel.FloorDetailViewModel

@Composable
fun FloorDetailScreen(
    floorId: String,
    onBack: () -> Unit,
    onDeviceClick: (Device) -> Unit,
    viewModel: FloorDetailViewModel = viewModel(
        factory = FloorDetailViewModel.factory(floorId)
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            SmartHomeTopBar(
                title = uiState.floor?.name ?: "Floor",
                showBack = true,
                onBack = onBack
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> LoadingState(modifier = Modifier.padding(padding))
            uiState.floor == null -> EmptyState(
                title = "Floor not found",
                message = "This floor may have been removed",
                modifier = Modifier.padding(padding)
            )
            else -> {
                val floor = uiState.floor!!
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Card(
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Floor Plan",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "${floor.gridRows}×${floor.gridCols} grid — tap a device to control",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                                FloorPlanGrid(
                                    floor = floor,
                                    onDeviceClick = onDeviceClick
                                )
                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    LegendItem(DeviceState.ON, "ON")
                                    LegendItem(DeviceState.OFF, "OFF")
                                    LegendItem(DeviceState.ERROR, "ERROR")
                                    LegendItem(DeviceState.DISCONNECTED, "DISC")
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Devices (${floor.deviceCount})",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    items(floor.devices, key = { it.id }) { device ->
                        DeviceListItem(device = device, onClick = { onDeviceClick(device) })
                    }
                }
            }
        }
    }
}

@Composable
private fun FloorPlanGrid(
    floor: com.example.smarthome.data.model.Floor,
    onDeviceClick: (Device) -> Unit
) {
    val deviceMap = floor.devices.associateBy { it.row to it.col }
    val cells = buildList {
        for (row in 0 until floor.gridRows) {
            for (col in 0 until floor.gridCols) {
                add(row to col)
            }
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(floor.gridCols),
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        userScrollEnabled = false
    ) {
        items(cells) { (row, col) ->
            DeviceGridCell(
                device = deviceMap[row to col],
                onClick = onDeviceClick
            )
        }
    }
}

@Composable
private fun LegendItem(state: DeviceState, label: String) {
    Row {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .then(Modifier)
        )
        StatusBadge(state = state)
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
        )
    }
}

@Composable
private fun DeviceListItem(
    device: Device,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(Modifier),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = device.name, style = MaterialTheme.typography.titleSmall)
                Text(
                    text = "Position: (${device.row}, ${device.col})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            StatusBadge(state = device.state)
        }
    }
}
