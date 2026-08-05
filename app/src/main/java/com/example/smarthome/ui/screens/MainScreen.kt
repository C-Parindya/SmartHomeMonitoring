package com.example.smarthome.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
import com.example.smarthome.ui.screens.floors.FloorListScreen
import com.example.smarthome.ui.screens.settings.ProfileScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smarthome.viewmodel.SmartHomeViewModelFactory
import androidx.compose.material3.Text

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton

@Composable
fun MainScreen(
    repository: MockSmartHomeRepository,
    onLogout: () -> Unit,
    onNavigateToFloorDetail: (String) -> Unit,
    onNavigateToUsageReport: () -> Unit
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
                    }
                }
            )
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
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(64.dp), tint = DarkBrown)
                    Text("No new notifications", style = MaterialTheme.typography.titleLarge)
                }
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onLogout = onLogout,
                    viewModel = viewModel(factory = viewModelFactory)
                )
            }
        }
    }
}
