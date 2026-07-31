package com.example.smarthome.ui.screens.floors

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.smarthome.viewmodel.FloorListViewModel

@Composable
fun AddFloorScreen(
    viewModel: FloorListViewModel,
    onFloorAdded: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Create a New Floor",
            style = MaterialTheme.typography.headlineSmall
        )
        
        Text(
            text = "Give your floor a name to start adding areas and devices.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            value = uiState.newFloorName,
            onValueChange = viewModel::onNewFloorNameChange,
            label = { Text("Floor Name (e.g., First Floor)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                viewModel.addFloor()
                onFloorAdded()
            },
            enabled = uiState.newFloorName.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Floor")
        }
    }
}
