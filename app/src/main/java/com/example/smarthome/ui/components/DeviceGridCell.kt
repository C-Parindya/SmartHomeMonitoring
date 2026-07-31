package com.example.smarthome.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.ToggleOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.smarthome.data.model.Device
import com.example.smarthome.data.model.DeviceState
import com.example.smarthome.data.model.DeviceType

@Composable
fun DeviceGridCell(
    device: Device?,
    modifier: Modifier = Modifier,
    onClick: (Device) -> Unit = {}
) {
    val shape = RoundedCornerShape(6.dp)
    val borderColor = device?.state?.toColor() ?: MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    val backgroundColor = device?.state?.toColor()?.copy(alpha = 0.15f)
        ?: MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)

    Box(
        modifier = modifier
            .size(40.dp)
            .clip(shape)
            .background(backgroundColor)
            .border(1.dp, borderColor, shape)
            .then(
                if (device != null) {
                    Modifier.clickable { onClick(device) }
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (device != null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = device.toIcon(),
                    contentDescription = device.name,
                    tint = device.state.toColor(),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = device.name.take(6),
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

private fun Device.toIcon(): ImageVector = when (this.type) {
    DeviceType.OUTLET -> Icons.Default.Power
    DeviceType.MULTI_SWITCH -> Icons.Outlined.ToggleOn
    DeviceType.SCHEDULED_DEVICE -> Icons.Outlined.Lightbulb
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
