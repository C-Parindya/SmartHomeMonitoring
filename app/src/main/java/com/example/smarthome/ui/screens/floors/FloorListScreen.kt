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

    Scaffold(
        containerColor = Color.White,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showAddDialog() },
                containerColor = DarkBrown,
                contentColor = Color.White,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.padding(bottom = 16.dp, end = 8.dp).size(64.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Floor", modifier = Modifier.size(32.dp))
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding()),
        ) {
            item {
                ScreenHeader(
                    title = "Floor Map",
                    subtitle = "Navigate and explore\nyour building easily",
                    icon = Icons.Outlined.Layers
                )
            }

            item {
                SectionTitle(text = "Select a floor to view details")
            }

            if (uiState.floors.isEmpty()) {
                item {
                    Box(modifier = Modifier.padding(24.dp)) {
                        EmptyState(
                            title = "No floors yet",
                            message = "Tap the + button to add your first floor",
                            icon = Icons.Outlined.Layers
                        )
                    }
                }
            } else {
                items(uiState.floors, key = { it.id }) { floor ->
                    FloorCard(
                        floor = floor,
                        onClick = { onFloorClick(floor.id) },
                        onEdit = { viewModel.showEditDialog(floor) },
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                    )
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(100.dp))
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
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = DarkBrown)
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
