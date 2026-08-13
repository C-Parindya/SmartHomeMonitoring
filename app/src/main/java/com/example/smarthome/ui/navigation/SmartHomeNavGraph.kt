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
import com.example.smarthome.data.model.DeviceType
import com.example.smarthome.data.repository.MockSmartHomeRepository
import com.example.smarthome.ui.screens.device.CameraViewScreen
import com.example.smarthome.ui.screens.device.MultiSwitchControlScreen
import com.example.smarthome.ui.screens.device.OutletControlScreen
import com.example.smarthome.ui.screens.device.ScheduledControlScreen
import com.example.smarthome.ui.screens.area_detail.AreaDetailScreen
import com.example.smarthome.ui.screens.floor_detail.FloorDetailScreen
import com.example.smarthome.ui.screens.login.LoginScreen
import com.example.smarthome.ui.screens.register.RegisterScreen
import com.example.smarthome.ui.screens.report.UsageReportScreen
import com.example.smarthome.viewmodel.AreaDetailViewModel
import com.example.smarthome.viewmodel.DeviceControlViewModel
import com.example.smarthome.viewmodel.FloorDetailViewModel
import com.example.smarthome.viewmodel.SmartHomeViewModelFactory

import com.example.smarthome.ui.screens.MainScreen
import com.example.smarthome.ui.screens.settings.ProfileScreen

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
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                viewModel = viewModel(factory = viewModelFactory)
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onBackToLogin = {
                    navController.popBackStack()
                },
                viewModel = viewModel(factory = viewModelFactory)
            )
        }

        composable(Screen.Main.route) {
            MainScreen(
                repository = repository,
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(navController.graph.id) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToFloorDetail = { floorId ->
                    navController.navigate(Screen.FloorDetail.createRoute(floorId))
                },
                onNavigateToUsageReport = {
                    navController.navigate(Screen.UsageReport.route)
                }
            )
        }

        composable(Screen.FloorList.route) {
            // This is now handled within MainScreen, but we can keep it as an alias or redirect
            navController.navigate(Screen.Main.route) {
                popUpTo(Screen.FloorList.route) { inclusive = true }
            }
        }

        composable(
            route = Screen.FloorDetail.route,
            arguments = listOf(navArgument("floorId") { type = NavType.StringType })
        ) { backStackEntry ->
            val floorId = backStackEntry.arguments?.getString("floorId") ?: ""
            FloorDetailScreen(
                onBack = { navController.popBackStack() },
                onAreaClick = { fId, areaId ->
                    navController.navigate(Screen.AreaDetail.createRoute(fId, areaId))
                },
                viewModel = viewModel(
                    factory = FloorDetailViewModel.factory(floorId, repository)
                )
            )
        }

        composable(
            route = Screen.AreaDetail.route,
            arguments = listOf(
                navArgument("floorId") { type = NavType.StringType },
                navArgument("areaId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val floorId = backStackEntry.arguments?.getString("floorId").orEmpty()
            val areaId = backStackEntry.arguments?.getString("areaId").orEmpty()
            AreaDetailScreen(
                floorId = floorId,
                areaId = areaId,
                onBack = { navController.popBackStack() },
                 viewModel = viewModel(
                    factory = AreaDetailViewModel.factory(floorId, areaId, repository)
                )
            )
        }

        composable(Screen.UsageReport.route) {
            UsageReportScreen(
                onBack = { navController.popBackStack() },
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
    val route = when (device.type) {
        DeviceType.OUTLET -> Screen.OutletControl.createRoute(device.id)
        DeviceType.MULTI_SWITCH -> Screen.MultiSwitchControl.createRoute(device.id)
        DeviceType.SCHEDULED_DEVICE -> Screen.ScheduledControl.createRoute(device.id)
        DeviceType.CAMERA -> Screen.CameraView.createRoute(device.id)
    }
    navController.navigate(route)
}
