package com.example.smarthome.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

import androidx.compose.ui.graphics.Color
import com.example.smarthome.ui.theme.WarmBrown

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartHomeTopBar(
    title: String,
    modifier: Modifier = Modifier,
    showBack: Boolean = false,
    onBack: () -> Unit = {},
    actions: @Composable () -> Unit = {}
) {
    TopAppBar(
        modifier = modifier,
        title = { Text(title) },
        navigationIcon = {
            if (showBack) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            }
        },
        actions = { actions() },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = WarmBrown,
            titleContentColor = Color.White,
            actionIconContentColor = Color.White
        )
    )
}
