package com.example.smarthome.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smarthome.ui.navigation.Screen
import com.example.smarthome.ui.theme.DarkBrown
import com.example.smarthome.ui.theme.NavColor

data class BottomNavItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val screen: Screen
)

@Composable
fun SmartHomeBottomBar(
    currentRoute: String?,
    onNavigate: (Screen) -> Unit
) {
    val items = listOf(
        BottomNavItem("Home", Icons.Default.Home, Icons.Outlined.Home, Screen.Home),
        BottomNavItem("Floor Map", Icons.Default.Layers, Icons.Outlined.Layers, Screen.FloorMap),
        BottomNavItem("Notification", Icons.Default.Notifications, Icons.Outlined.Notifications, Screen.Notification),
        BottomNavItem("Profile", Icons.Default.Person, Icons.Outlined.Person, Screen.Profile)
    )

    NavigationBar(
        containerColor = Color.White,
        contentColor = DarkBrown,
        tonalElevation = 8.dp
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.screen.route
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.screen) },
                label = { Text(item.label, color = if (isSelected) DarkBrown else DarkBrown.copy(alpha = 0.6f), fontSize = 10.sp) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label,
                        tint = if (isSelected) DarkBrown else DarkBrown.copy(alpha = 0.6f)
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color(0xFFF3E5DC),
                    selectedIconColor = DarkBrown,
                    unselectedIconColor = DarkBrown.copy(alpha = 0.6f),
                    selectedTextColor = DarkBrown,
                    unselectedTextColor = DarkBrown.copy(alpha = 0.6f)
                )
            )
        }
    }
}
