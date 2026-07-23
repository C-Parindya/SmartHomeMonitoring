package com.example.smarthome.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.smarthome.data.model.Device
import com.example.smarthome.data.repository.MockSmartHomeRepository
import com.example.smarthome.ui.screens.device.CameraViewScreen
import com.example.smarthome.ui.screens.device.MultiSwitchControlScreen
import com.example.smarthome.ui.screens.device.OutletControlScreen
import com.example.smarthome.ui.screens.device.ScheduledControlScreen
import com.example.smarthome.ui.screens.floor_detail.FloorDetailScreen
import com.example.smarthome.ui.screens.floors.FloorListScreen
import com.example.smarthome.ui.screens.login.LoginScreen
import com.example.smarthome.ui.screens.report.UsageReportScreen
import com.example.smarthome.ui.screens.settings.SettingsScreen
import com.example.smarthome.viewmodel.DeviceControlViewModel
import com.example.smarthome.viewmodel.SmartHomeViewModelFactory

@Composable
fun SmartHomeNavGraph(
    repository: MockSmartHomeRepository,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val viewModelFactory = SmartHomeViewModelFactory(repository)

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route,
        modifier = modifier
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.FloorList.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                viewModel = viewModel(factory = viewModelFactory)
            )
        }

        composable(Screen.FloorList.route) {
            FloorListScreen(
                onFloorClick = { floorId ->
                    navController.navigate(Screen.FloorDetail.createRoute(floorId))
                },
                onUsageReportClick = {
                    navController.navigate(Screen.UsageReport.route)
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                },
                viewModel = viewModel(factory = viewModelFactory)
            )
        }

        composable(
            route = Screen.FloorDetail.route,
            arguments = listOf(navArgument("floorId") { type = NavType.StringType })
        ) { backStackEntry ->
            val floorId = backStackEntry.arguments?.getString("floorId") ?: ""
            FloorDetailScreen(
                floorId = floorId,
                onBack = { navController.popBackStack() },
                onDeviceClick = { device -> navigateToDevice(navController, device) }
            )
        }

        composable(Screen.UsageReport.route) {
            UsageReportScreen(
                onBack = { navController.popBackStack() },
                viewModel = viewModel(factory = viewModelFactory)
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(navController.graph.id) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                viewModel = viewModel(factory = viewModelFactory)
            )
        }

        composable(
            route = Screen.OutletControl.route,
            arguments = listOf(navArgument("deviceId") { type = NavType.StringType })
        ) { backStackEntry ->
            val deviceId = backStackEntry.arguments?.getString("deviceId").orEmpty()
            OutletControlScreen(
                deviceId = deviceId,
                onBack = { navController.popBackStack() },
                viewModel = viewModel(
                    factory = DeviceControlViewModel.factory(deviceId, repository)
                )
            )
        }

        composable(
            route = Screen.MultiSwitchControl.route,
            arguments = listOf(navArgument("deviceId") { type = NavType.StringType })
        ) { backStackEntry ->
            val deviceId = backStackEntry.arguments?.getString("deviceId").orEmpty()
            MultiSwitchControlScreen(
                deviceId = deviceId,
                onBack = { navController.popBackStack() },
                viewModel = viewModel(
                    factory = DeviceControlViewModel.factory(deviceId, repository)
                )
            )
        }

        composable(
            route = Screen.ScheduledControl.route,
            arguments = listOf(navArgument("deviceId") { type = NavType.StringType })
        ) { backStackEntry ->
            val deviceId = backStackEntry.arguments?.getString("deviceId").orEmpty()
            ScheduledControlScreen(
                deviceId = deviceId,
                onBack = { navController.popBackStack() },
                viewModel = viewModel(
                    factory = DeviceControlViewModel.factory(deviceId, repository)
                )
            )
        }

        composable(
            route = Screen.CameraView.route,
            arguments = listOf(navArgument("deviceId") { type = NavType.StringType })
        ) { backStackEntry ->
            val deviceId = backStackEntry.arguments?.getString("deviceId").orEmpty()
            CameraViewScreen(
                deviceId = deviceId,
                onBack = { navController.popBackStack() },
                viewModel = viewModel(
                    factory = DeviceControlViewModel.factory(deviceId, repository)
                )
            )
        }
    }
}

private fun navigateToDevice(navController: NavHostController, device: Device) {
    val route = when (device) {
        is Device.Outlet -> Screen.OutletControl.createRoute(device.id)
        is Device.MultiSwitch -> Screen.MultiSwitchControl.createRoute(device.id)
        is Device.ScheduledDevice -> Screen.ScheduledControl.createRoute(device.id)
        is Device.Camera -> Screen.CameraView.createRoute(device.id)
    }
    navController.navigate(route)
}
