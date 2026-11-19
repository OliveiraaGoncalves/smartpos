package com.raimundo.smartpos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.raimundo.smartpos.ui.navigation.SmartPosNavHost
import com.raimundo.smartpos.ui.theme.SmartPosTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as SmartPosApplication).container
        setContent {
            SmartPosTheme {
                SmartPosNavHost(container)
            }
        }
    }
}
