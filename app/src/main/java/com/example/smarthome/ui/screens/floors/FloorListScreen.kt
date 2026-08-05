package com.example.smarthome.ui.screens.floors

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.smarthome.data.model.Floor
import com.example.smarthome.ui.components.EmptyState
import com.example.smarthome.viewmodel.FloorListViewModel

@Composable
fun FloorListScreen(
    onFloorClick: (String) -> Unit,
    viewModel: FloorListViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var floorToDelete by remember { mutableStateOf<Floor?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showAddDialog() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Floor")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (uiState.floors.isEmpty()) {
                EmptyState(
                    title = "No floors yet",
                    message = "Tap the + button to add your first floor",
                    icon = Icons.Outlined.Layers
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "Select a floor to view details",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    items(uiState.floors, key = { it.id }) { floor ->
                        FloorCard(
                            floor = floor, 
                            onClick = { onFloorClick(floor.id) },
                            onEdit = { viewModel.showEditDialog(floor) },
                            onDelete = { floorToDelete = floor }
                        )
                    }
                }
            }
        }
    }

    if (uiState.showAddDialog) {
        AddFloorDialog(
            name = uiState.newFloorName,
            onNameChange = { viewModel.onNewFloorNameChange(it) },
            onDismiss = { viewModel.dismissAddDialog() },
            onConfirm = { viewModel.addFloor() }
        )
    }

    if (uiState.showEditDialog) {
        AddFloorDialog(
            title = "Edit Floor",
            confirmLabel = "Update",
            name = uiState.newFloorName,
            onNameChange = { viewModel.onNewFloorNameChange(it) },
            onDismiss = { viewModel.dismissAddDialog() },
            onConfirm = { viewModel.updateFloor() }
        )
    }

    floorToDelete?.let { floor ->
        AlertDialog(
            onDismissRequest = { floorToDelete = null },
            title = { Text("Delete Floor") },
            text = { Text("Are you sure you want to delete '${floor.name}'? This will also remove all areas and devices on this floor.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteFloor(floor.id)
                        floorToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { floorToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AddFloorDialog(
    title: String = "Add New Floor",
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
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("Floor Name") },
                placeholder = { Text("e.g. Ground Floor, First Floor") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = name.isNotBlank()
            ) {
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

@Composable
private fun FloorCard(
    floor: Floor,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Layers,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                
                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = floor.name,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "${floor.deviceCount} device${if (floor.deviceCount != 1) "s" else ""}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                // Keep some space for bottom icons if needed, or just let them overlap slightly if text is long
                Spacer(modifier = Modifier.width(64.dp))
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Edit, 
                        contentDescription = "Edit Floor", 
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Delete, 
                        contentDescription = "Delete Floor", 
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
