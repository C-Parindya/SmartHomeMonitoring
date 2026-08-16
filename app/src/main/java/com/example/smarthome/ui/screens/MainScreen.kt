package com.example.smarthome.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smarthome.ui.theme.DarkBrown
import com.example.smarthome.ui.theme.NavColor
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.smarthome.data.repository.MockSmartHomeRepository
import com.example.smarthome.ui.components.SmartHomeBottomBar
import com.example.smarthome.ui.components.SmartHomeTopBar
import com.example.smarthome.ui.navigation.Screen
import com.example.smarthome.ui.screens.home.HomeScreen
import com.example.smarthome.ui.components.ScreenHeader
import com.example.smarthome.ui.components.SectionTitle
import com.example.smarthome.ui.screens.floors.FloorListScreen
import com.example.smarthome.ui.screens.settings.ProfileScreen
import com.example.smarthome.ui.screens.settings.EditProfileScreen
import com.example.smarthome.viewmodel.NotificationViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smarthome.viewmodel.SmartHomeViewModelFactory
import androidx.compose.material3.Text

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MainScreen(
    repository: MockSmartHomeRepository,
    onLogout: () -> Unit,
    onNavigateToFloorDetail: (String) -> Unit,
    onNavigateToUsageReport: () -> Unit,
    onNavigateToCamera: (String) -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val viewModelFactory = SmartHomeViewModelFactory(repository)

    val title = when (currentRoute) {
        Screen.Home.route -> "My Floors"
        Screen.FloorMap.route -> "Floor Map"
        Screen.Notification.route -> "Notifications"
        Screen.Profile.route -> "Profile"
        else -> "Smart Home"
    }

    Scaffold(
        topBar = {
            if (currentRoute != Screen.FloorMap.route && 
                currentRoute != Screen.Home.route &&
                currentRoute != Screen.Notification.route &&
                currentRoute != Screen.Profile.route) {
                SmartHomeTopBar(
                    title = title,
                    showBack = false,
                    actions = {
                        if (currentRoute == Screen.Home.route) {
                            IconButton(onClick = onNavigateToUsageReport) {
                                Icon(
                                    imageVector = Icons.Outlined.BarChart,
                                    contentDescription = "Usage report"
                                )
                            }
                        } else if (currentRoute == Screen.Notification.route) {
                            val notificationViewModel: NotificationViewModel = viewModel(factory = viewModelFactory)
                            IconButton(onClick = { notificationViewModel.clearNotifications() }) {
                                Icon(
                                    imageVector = Icons.Outlined.DeleteSweep,
                                    contentDescription = "Clear all"
                                )
                            }
                        }
                    }
                )
            }
        },
        bottomBar = {
            SmartHomeBottomBar(
                currentRoute = currentRoute,
                onNavigate = { screen ->
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onViewAllActivity = {
                        navController.navigate(Screen.Notification.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToCamera = onNavigateToCamera,
                    viewModel = viewModel(factory = viewModelFactory)
                )
            }
            composable(Screen.FloorMap.route) {
                FloorListScreen(
                    onFloorClick = onNavigateToFloorDetail,
                    viewModel = viewModel(factory = viewModelFactory)
                )
            }
            composable(Screen.Notification.route) {
                val notificationViewModel: NotificationViewModel = viewModel(factory = viewModelFactory)
                val notifications by notificationViewModel.notifications.collectAsStateWithLifecycle()

                Scaffold(
                    containerColor = Color.White,
                    topBar = {
                        // Empty spacer to handle edge-to-edge if needed, 
                        // but we'll let ScreenHeader handle the visual top
                    }
                ) { innerPadding ->
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = innerPadding.calculateBottomPadding()),
                    ) {
                        item {
                            ScreenHeader(
                                title = "Notifications",
                                subtitle = "Stay updated with\nyour home events"
                            )
                        }

                        item {
                            Column(modifier = Modifier.offset(y = (-40).dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    SectionTitle(text = "Recent Alerts")
                                    
                                    if (notifications.isNotEmpty()) {
                                        TextButton(
                                            onClick = { notificationViewModel.clearNotifications() },
                                            modifier = Modifier.padding(end = 16.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.DeleteSweep,
                                                contentDescription = "Clear all",
                                                tint = Color.Gray,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Clear all",
                                                color = Color.Gray,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }

                                if (notifications.isEmpty()) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 32.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            Icons.Default.Notifications, 
                                            contentDescription = null, 
                                            modifier = Modifier.size(64.dp), 
                                            tint = Color.LightGray.copy(alpha = 0.5f)
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            "No new notifications", 
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Color.Gray
                                        )
                                    }
                                } else {
                                    notifications.forEach { notification ->
                                        Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)) {
                                            NotificationItem(notification)
                                        }
                                    }
                                }
                            }
                        }
                        
                        item {
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onLogout = onLogout,
                    onEditProfile = { navController.navigate(Screen.EditProfile.route) },
                    onUsageReport = onNavigateToUsageReport,
                    viewModel = viewModel(factory = viewModelFactory)
                )
            }
            composable(Screen.EditProfile.route) {
                EditProfileScreen(
                    onBack = { navController.popBackStack() },
                    viewModel = viewModel(factory = viewModelFactory)
                )
            }
        }
    }
}

@Composable
fun NotificationItem(notification: com.example.smarthome.data.model.Notification) {
    val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    val dateStr = dateFormat.format(Date(notification.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.type == "SAFETY") 
                Color(0xFFFEF2F2) // Very light red
            else 
                Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = if (notification.type == "SAFETY") 
            androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFEE2E2)) 
        else null
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (notification.type == "SAFETY") 
                        MaterialTheme.colorScheme.error 
                    else 
                        DarkBrown
                )
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = notification.message,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
