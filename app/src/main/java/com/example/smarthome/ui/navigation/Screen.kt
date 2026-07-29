package com.example.smarthome.ui.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object FloorList : Screen("floor_list")
    data object UsageReport : Screen("usage_report")
    data object Settings : Screen("settings")

    data object FloorDetail : Screen("floor_detail/{floorId}") {
        fun createRoute(floorId: String) = "floor_detail/$floorId"
    }

    data object OutletControl : Screen("device/outlet/{deviceId}") {
        fun createRoute(deviceId: String) = "device/outlet/$deviceId"
    }

    data object MultiSwitchControl : Screen("device/multi_switch/{deviceId}") {
        fun createRoute(deviceId: String) = "device/multi_switch/$deviceId"
    }

    data object ScheduledControl : Screen("device/scheduled/{deviceId}") {
        fun createRoute(deviceId: String) = "device/scheduled/$deviceId"
    }

    data object CameraView : Screen("device/camera/{deviceId}") {
        fun createRoute(deviceId: String) = "device/camera/$deviceId"
    }
}
