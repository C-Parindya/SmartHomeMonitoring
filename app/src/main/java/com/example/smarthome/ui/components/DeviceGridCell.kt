package com.example.smarthome.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.WindPower
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.ToggleOn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.smarthome.data.model.Device
import com.example.smarthome.data.model.DeviceState
import com.example.smarthome.data.model.DeviceType
import com.example.smarthome.ui.theme.DarkBrown

@Composable
fun DeviceGridCell(
    device: Device?,
    modifier: Modifier = Modifier,
    onClick: (Device) -> Unit = {},
    onToggle: (Device) -> Unit = {}
) {
    val shape = RoundedCornerShape(8.dp)
    
    // Status colors: Green for ON, White/Gray for OFF, Red for ERROR, Black for DISCONNECTED
    val stateColor = when (device?.state) {
        DeviceState.ON -> Color(0xFF4CAF50) // 🟢 Green
        DeviceState.OFF -> Color(0xFFE0E0E0) // ⚪ OFF
        DeviceState.ERROR -> Color(0xFFF44336) // 🔴 ERROR
        DeviceState.DISCONNECTED -> Color(0xFF000000) // ⚫ DISCONNECTED
        null -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    }
    
    val backgroundColor = if (device?.state == DeviceState.OFF) {
        Color.White
    } else {
        stateColor.copy(alpha = 0.1f)
    }

    Box(
        modifier = modifier
            .fillMaxSize() 
            .aspectRatio(1f)
            .clip(shape)
            .background(backgroundColor)
            .border(
                width = 1.dp, 
                color = if (device?.state == DeviceState.OFF) Color.LightGray else stateColor.copy(alpha = 0.5f), 
                shape = shape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (device != null) {
            // Main content area for toggling device
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onToggle(device) },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(4.dp)
                ) {
                    Icon(
                        imageVector = device.toIcon(),
                        contentDescription = device.name,
                        tint = if (device.state == DeviceState.OFF) Color.Gray else stateColor,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = device.name,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp),
                        color = DarkBrown
                    )
                }
            }

            // Edit button in top-right
            IconButton(
                onClick = { onClick(device) },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(24.dp)
                    .padding(2.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color.LightGray.copy(alpha = 0.2f),
                    contentColor = DarkBrown.copy(alpha = 0.6f)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Device",
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

private fun Device.toIcon(): ImageVector = when (this.type) {
    DeviceType.OUTLET -> Icons.Default.Power
    DeviceType.MULTI_SWITCH -> Icons.Outlined.ToggleOn
    DeviceType.SCHEDULED_DEVICE -> {
        when (this.deviceKind) {
            com.example.smarthome.data.model.ScheduledKind.IRON -> Icons.Default.Power
            com.example.smarthome.data.model.ScheduledKind.FAN -> Icons.Default.WindPower
            else -> Icons.Default.Lightbulb
        }
    }
    DeviceType.CAMERA -> Icons.Default.Videocam
}

@Composable
fun DeviceLegendItem(
    state: DeviceState,
    label: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(12.dp)
            .background(state.toColor(), RoundedCornerShape(2.dp))
    )
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier.padding(start = 4.dp)
    )
}
