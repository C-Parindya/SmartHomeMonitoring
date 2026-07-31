package com.example.smarthome.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.smarthome.ui.navigation.Screen
import com.example.smarthome.ui.theme.DarkBrown
import com.example.smarthome.ui.theme.NavColor

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val screen: Screen
)

@Composable
fun SmartHomeBottomBar(
    currentRoute: String?,
    onNavigate: (Screen) -> Unit
) {
    val items = listOf(
        BottomNavItem("Home", Icons.Default.Home, Screen.Home),
        BottomNavItem("Floor Map", Icons.Default.Layers, Screen.FloorMap),
        BottomNavItem("Add Floor", Icons.Default.AddCircle, Screen.AddFloor),
        BottomNavItem("Notification", Icons.Default.Notifications, Screen.Notification),
        BottomNavItem("Profile", Icons.Default.Person, Screen.Profile)
    )

    NavigationBar(
        containerColor = NavColor,
        contentColor = DarkBrown
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.screen.route
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.screen) },
                label = { Text(item.label, color = DarkBrown) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = if (isSelected) DarkBrown else DarkBrown.copy(alpha = 0.6f)
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = DarkBrown.copy(alpha = 0.1f),
                    selectedIconColor = DarkBrown,
                    unselectedIconColor = DarkBrown.copy(alpha = 0.6f),
                    selectedTextColor = DarkBrown,
                    unselectedTextColor = DarkBrown.copy(alpha = 0.6f)
                )
            )
        }
    }
}
