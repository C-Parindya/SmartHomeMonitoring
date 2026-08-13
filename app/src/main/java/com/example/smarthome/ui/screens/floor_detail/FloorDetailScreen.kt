package com.example.smarthome.ui.screens.floor_detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Power
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.smarthome.data.model.Area
import com.example.smarthome.data.model.DeviceState
import com.example.smarthome.ui.components.EmptyState
import com.example.smarthome.ui.components.LoadingState
import com.example.smarthome.ui.components.SmartHomeTopBar
import com.example.smarthome.ui.theme.CreamBrown
import com.example.smarthome.ui.theme.DarkBrown
import com.example.smarthome.viewmodel.FloorDetailViewModel

@Composable
fun FloorDetailScreen(
    floorId: String,
    onBack: () -> Unit,
    onAreaClick: (String, String) -> Unit,
    viewModel: FloorDetailViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var areaToDelete by remember { mutableStateOf<Area?>(null) }

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
                containerColor = DarkBrown,
                contentColor = CreamBrown
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
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item(span = { GridItemSpan(2) }) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Areas",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }

                    if (floor.areas.isEmpty()) {
                        item(span = { GridItemSpan(2) }) {
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
                                onEdit = { viewModel.showEditAreaDialog(area) },
                                onDelete = { areaToDelete = area },
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
            onDismiss = viewModel::dismissAddAreaDialog,
            onConfirm = viewModel::addArea
        )
    }

    if (uiState.showEditAreaDialog) {
        AddAreaDialog(
            title = "Edit Area",
            confirmLabel = "Update",
            name = uiState.newAreaName,
            onNameChange = viewModel::onNewAreaNameChange,
            onDismiss = viewModel::dismissAddAreaDialog,
            onConfirm = viewModel::updateArea
        )
    }

    areaToDelete?.let { area ->
        AlertDialog(
            onDismissRequest = { areaToDelete = null },
            title = { Text("Delete Area") },
            text = { Text("Are you sure you want to delete '${area.name}'? This will also remove all devices in this area.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteArea(area.id)
                        areaToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { areaToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun AreaCard(
    area: Area, 
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    val activeCount = area.devices.count { 
        it.state == DeviceState.ON || 
        it.switches.any { sw -> sw.isOn } ||
        it.isStreaming
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = CreamBrown
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = area.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = DarkBrown
                )
            }
            
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Edit, 
                        contentDescription = "Edit Area", 
                        tint = DarkBrown.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete, 
                        contentDescription = "Delete Area", 
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            if (activeCount > 0) {
                Surface(
                    color = DarkBrown,
                    shape = RoundedCornerShape(topStart = 0.dp, bottomEnd = 0.dp, topEnd = 8.dp, bottomStart = 8.dp),
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Power,
                            contentDescription = null,
                            tint = CreamBrown,
                            modifier = Modifier.size(10.dp)
                        )
                        Text(
                            text = "$activeCount ON",
                            style = MaterialTheme.typography.labelSmall,
                            color = CreamBrown
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AddAreaDialog(
    title: String = "Add Area",
    confirmLabel: String = "Add",
    name: String,
    onNameChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("Area Name (e.g. Kitchen)") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = name.isNotBlank()) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
