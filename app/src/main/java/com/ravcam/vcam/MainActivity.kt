package com.ravcam.vcam

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ravcam.vcam.ui.screens.home.RavCamDashboard
import com.ravcam.vcam.ui.theme.RavCamTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            RavCamTheme {
                RavCamDashboard()
            }
        }
    }
}