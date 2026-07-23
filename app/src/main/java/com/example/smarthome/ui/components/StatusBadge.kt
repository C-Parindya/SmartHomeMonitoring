package com.example.smarthome.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.smarthome.data.model.DeviceState
import com.example.smarthome.ui.theme.DeviceDisconnected
import com.example.smarthome.ui.theme.DeviceError
import com.example.smarthome.ui.theme.DeviceOff
import com.example.smarthome.ui.theme.DeviceOn

fun DeviceState.toColor(): Color = when (this) {
    DeviceState.ON -> DeviceOn
    DeviceState.OFF -> DeviceOff
    DeviceState.ERROR -> DeviceError
    DeviceState.DISCONNECTED -> DeviceDisconnected
}

fun DeviceState.toLabel(): String = when (this) {
    DeviceState.ON -> "ON"
    DeviceState.OFF -> "OFF"
    DeviceState.ERROR -> "ERROR"
    DeviceState.DISCONNECTED -> "DISCONNECTED"
}

@Composable
fun StatusBadge(
    state: DeviceState,
    modifier: Modifier = Modifier
) {
    Text(
        text = state.toLabel(),
        style = MaterialTheme.typography.labelSmall,
        color = Color.White,
        modifier = modifier
            .background(state.toColor(), RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}
