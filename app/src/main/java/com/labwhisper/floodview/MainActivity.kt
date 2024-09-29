package com.labwhisper.floodview

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.labwhisper.floodview.ui.theme.FloodViewTheme

class MainActivity : ComponentActivity() {
    private val floodViewModel: FloodDetectionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FloodViewTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val floodImageBase64 by floodViewModel.floodData.collectAsState()
                    FloodMapScreen(
                        viewModel = floodViewModel,
                        floodData = floodImageBase64,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
