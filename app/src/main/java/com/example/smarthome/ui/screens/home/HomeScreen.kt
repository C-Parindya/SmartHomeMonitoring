package com.example.smarthome.ui.screens.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.smarthome.data.model.Device
import com.example.smarthome.data.model.DeviceState
import com.example.smarthome.data.model.DeviceType
import com.example.smarthome.ui.theme.DarkBrown
import com.example.smarthome.viewmodel.FloorListViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    onViewAllActivity: () -> Unit,
    onNavigateToCamera: (String) -> Unit,
    viewModel: FloorListViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = Color(0xFFF9F9F9)
    ) { padding ->
        val allDevices = uiState.allDevices
        val totalDevices = allDevices.size
        val totalFloors = uiState.floors.size
        val devicesOn = allDevices.count { it.state == DeviceState.ON }
        val devicesError = allDevices.count { it.state == DeviceState.ERROR }
        val cameras = allDevices.filter { it.type == DeviceType.CAMERA }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding()),
        ) {
            item {
                HomeHeader(userName = uiState.user?.displayName ?: "User")
            }

            item {
                OverviewCard(
                    totalDevices = totalDevices,
                    totalFloors = totalFloors,
                    devicesOn = devicesOn,
                    devicesError = devicesError,
                    modifier = Modifier.padding(horizontal = 24.dp).offset(y = (-40).dp)
                )
            }

            // Camera Section
            if (cameras.isNotEmpty()) {
                item {
                    SectionHeader(title = "Live Cameras", onViewAll = {})
                }
                item {
                    CameraSection(
                        cameras = cameras,
                        onCameraClick = onNavigateToCamera,
                        onTogglePower = viewModel::toggleDevice,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }

            item {
                SectionHeader(title = "System Recent Activity", onViewAll = onViewAllActivity)
            }

            item {
                ActivityList(
                    notifications = uiState.notifications.take(5),
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun HomeHeader(userName: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .background(Color.White)
    ) {
        // Header Background Image
        Image(
            painter = painterResource(id = com.example.smarthome.R.drawable.homescreen),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
            contentScale = ContentScale.Crop
        )

        // Wavy separation
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .align(Alignment.BottomCenter)
        ) {
            val path = Path().apply {
                moveTo(0f, 0f)
                cubicTo(
                    size.width * 0.25f, size.height * 0.5f,
                    size.width * 0.75f, -size.height * 0.5f,
                    size.width, size.height * 0.5f
                )
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
            drawPath(path, color = Color(0xFFF9F9F9))
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .padding(top = 48.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Welcome back,",
                    style = MaterialTheme.typography.headlineSmall,
                    color = DarkBrown,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                )
                Text(
                    text = "$userName!",
                    style = MaterialTheme.typography.headlineSmall,
                    color = DarkBrown,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                )
            }
        }
    }
}

@Composable
private fun OverviewCard(
    totalDevices: Int,
    totalFloors: Int,
    devicesOn: Int,
    devicesError: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Smart Home Overview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DarkBrown
            )
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OverviewItem(icon = Icons.Default.Power, count = totalDevices, label = "Devices", iconColor = Color(0xFF5D4037))
                OverviewItem(icon = Icons.Default.CheckCircle, count = totalFloors, label = "Floors", iconColor = Color(0xFF4CAF50))
                OverviewItem(icon = Icons.Default.PowerSettingsNew, count = devicesOn, label = "ON", iconColor = Color(0xFFFFB74D))
                OverviewItem(icon = Icons.Default.Warning, count = devicesError, label = "Error", iconColor = Color(0xFFEF5350))
            }
        }
    }
}

@Composable
private fun OverviewItem(icon: ImageVector, count: Int, label: String, iconColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(iconColor.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = count.toString(), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = DarkBrown)
        Text(text = label, fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
private fun CameraSection(
    cameras: List<Device>,
    onCameraClick: (String) -> Unit,
    onTogglePower: (Device) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        cameras.forEach { camera ->
            CameraItem(
                camera = camera, 
                onCameraClick = onCameraClick,
                onTogglePower = onTogglePower
            )
        }
    }
}

@Composable
private fun CameraItem(
    camera: Device,
    onCameraClick: (String) -> Unit,
    onTogglePower: (Device) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            if (camera.state == DeviceState.ON) Color(0xFFE3F2FD) else Color(0xFFF5F5F5), 
                            CircleShape
                        )
                        .clickable { onTogglePower(camera) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = null,
                        tint = if (camera.state == DeviceState.ON) Color(0xFF2196F3) else Color.Gray
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = camera.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = DarkBrown
                    )
                    val statusText = when {
                        camera.state != DeviceState.ON -> "Power Off"
                        camera.isStreaming -> "Streaming"
                        else -> null
                    }
                    if (statusText != null) {
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (camera.isStreaming) Color(0xFF4CAF50) else Color.Gray
                        )
                    }
                }
            }
            
            IconButton(onClick = { onCameraClick(camera.id) }) {
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = "View Stream",
                    tint = Color.Gray
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, onViewAll: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = DarkBrown)
        TextButton(onClick = onViewAll) {
            Text(text = "View all", color = Color.Gray)
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun ActivityList(
    notifications: List<com.example.smarthome.data.model.Notification>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            if (notifications.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No recent activity",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            } else {
                notifications.forEach { notification ->
                    val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
                    val dateStr = dateFormat.format(Date(notification.timestamp))
                    
                    val color = when(notification.type) {
                        "SAFETY" -> Color(0xFFEF5350)
                        "ALERT" -> Color(0xFFFFB74D)
                        else -> Color(0xFF42A5F5)
                    }
                    
                    val icon = when(notification.type) {
                        "SAFETY" -> Icons.Default.Warning
                        "ALERT" -> Icons.Default.Notifications
                        else -> Icons.Default.Info
                    }

                    ActivityItem(
                        ActivityItemData(
                            description = notification.title,
                            time = dateStr,
                            color = color,
                            icon = icon
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun ActivityItem(data: ActivityItemData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(data.color.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = data.icon, contentDescription = null, tint = data.color, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = data.description, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = DarkBrown)
            Text(text = data.time, fontSize = 12.sp, color = Color.Gray)
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.LightGray)
    }
}

data class ActivityItemData(val description: String, val time: String, val color: Color, val icon: ImageVector)
