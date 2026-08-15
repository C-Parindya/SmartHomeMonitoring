package com.example.smarthome.ui.screens.floors

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.smarthome.data.model.Floor
import com.example.smarthome.ui.components.EmptyState
import com.example.smarthome.ui.components.ScreenHeader
import com.example.smarthome.ui.components.SectionTitle
import com.example.smarthome.ui.theme.DarkBrown
import com.example.smarthome.viewmodel.FloorListViewModel

@Composable
fun FloorListScreen(
    onFloorClick: (String) -> Unit,
    viewModel: FloorListViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val floorToDeleteState = remember { mutableStateOf<Floor?>(null) }
    val floorToDelete = floorToDeleteState.value

    Scaffold(
        containerColor = Color.White
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding()),
        ) {
            item {
                ScreenHeader(
                    title = "Floor Map",
                    subtitle = "Navigate and explore\nyour building easily"
                )
            }

            item {
                Column(modifier = Modifier.offset(y = (-40).dp)) {
                    SectionTitle(text = "Select a floor to view details")

                    if (uiState.floors.isEmpty()) {
                        Box(modifier = Modifier.padding(24.dp)) {
                            EmptyState(
                                title = "No floors yet",
                                message = "Tap the + button to add your first floor",
                                icon = Icons.Outlined.Layers
                            )
                        }
                    } else {
                        uiState.floors.forEach { floor ->
                            FloorCard(
                                floor = floor,
                                onClick = { onFloorClick(floor.id) },
                                onEdit = { viewModel.showEditDialog(floor) },
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { viewModel.showAddDialog() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(56.dp)
                        .offset(y = (-40).dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkBrown)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add New Floor", fontWeight = FontWeight.Bold)
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(60.dp))
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

    if (uiState.showEditDialog && uiState.selectedFloor != null) {
        AddFloorDialog(
            title = "Edit Floor",
            confirmLabel = "Update",
            name = uiState.newFloorName,
            onNameChange = { viewModel.onNewFloorNameChange(it) },
            onDismiss = { viewModel.dismissAddDialog() },
            onConfirm = { viewModel.updateFloor() },
            onDelete = {
                floorToDeleteState.value = uiState.selectedFloor
                viewModel.dismissAddDialog()
            }
        )
    }

    floorToDelete?.let { floor ->
        AlertDialog(
            onDismissRequest = { floorToDeleteState.value = null },
            title = { Text("Delete Floor") },
            text = { Text("Are you sure you want to delete '${floor.name}'? This will also remove all areas and devices on this floor.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteFloor(floor.id)
                        floorToDeleteState.value = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { floorToDeleteState.value = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun FloorCard(
    floor: Floor,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon container
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFF3E5DC)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Layers,
                    contentDescription = null,
                    tint = Color(0xFF5D4037),
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = floor.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "${floor.deviceCount} devices",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.height(60.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color.LightGray
                )
                
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = Color(0xFFD0B29A),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AddFloorDialog(
    title: String = "Add New Floor",
    confirmLabel: String = "Add",
    name: String,
    onNameChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    onDelete: (() -> Unit)? = null
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onDelete != null) {
                    TextButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete")
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }
                
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Button(
                    onClick = onConfirm,
                    enabled = name.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkBrown)
                ) {
                    Text(confirmLabel)
                }
            }
        },
        dismissButton = null
    )
}
