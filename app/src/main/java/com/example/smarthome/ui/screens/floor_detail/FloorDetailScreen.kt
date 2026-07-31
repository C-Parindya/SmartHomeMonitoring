package com.example.smarthome.ui.screens.floor_detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.smarthome.data.model.Area
import com.example.smarthome.data.model.Device
import com.example.smarthome.ui.components.EmptyState
import com.example.smarthome.ui.components.LoadingState
import com.example.smarthome.ui.components.SmartHomeTopBar
import com.example.smarthome.viewmodel.FloorDetailViewModel

@Composable
fun FloorDetailScreen(
    floorId: String,
    onBack: () -> Unit,
    onAreaClick: (String, String) -> Unit,
    viewModel: FloorDetailViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            SmartHomeTopBar(
                title = uiState.floor?.name ?: "Floor",
                showBack = true,
                onBack = onBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = viewModel::showAddAreaDialog,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = com.example.smarthome.ui.theme.DarkBrown
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Area")
            }
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
                        Text(
                            text = "Areas",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    if (floor.areas.isEmpty()) {
                        item {
                            Text(
                                text = "No areas added yet. Tap + to add rooms or kitchen.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        items(floor.areas, key = { it.id }) { area ->
                            AreaCard(
                                area = area,
                                onClick = { onAreaClick(floor.id, area.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (uiState.showAddAreaDialog) {
        AddAreaDialog(
            name = uiState.newAreaName,
            onNameChange = viewModel::onNewAreaNameChange,
            type = uiState.newAreaType,
            onTypeChange = viewModel::onNewAreaTypeChange,
            onDismiss = viewModel::dismissAddAreaDialog,
            onConfirm = viewModel::addArea
        )
    }
}

@Composable
private fun AreaCard(area: Area, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = area.name, style = MaterialTheme.typography.titleSmall)
                Text(text = area.type, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun AddAreaDialog(
    name: String,
    onNameChange: (String) -> Unit,
    type: String,
    onTypeChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Area") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("Area Name (e.g. Kitchen)") },
                    singleLine = true
                )
                
                Text("Type:", style = MaterialTheme.typography.labelMedium)
                val types = listOf("Room", "Kitchen", "Bathroom", "Living Room", "Bedroom", "Other")
                
                Column {
                    types.chunked(3).forEach { rowTypes ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            rowTypes.forEach { areaType ->
                                FilterChip(
                                    selected = type == areaType,
                                    onClick = { onTypeChange(areaType) },
                                    label = { Text(areaType) }
                                )
                            }
                        }
                    }
                }
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
