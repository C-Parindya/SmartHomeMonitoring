package com.example.smarthome

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.smarthome.data.repository.MockSmartHomeRepository
import com.example.smarthome.ui.navigation.SmartHomeNavGraph
import com.example.smarthome.ui.theme.SmartHomeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repository = MockSmartHomeRepository.instance
        setContent {
            SmartHomeTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SmartHomeNavGraph(repository = repository)
                }
            }
        }
    }
}
