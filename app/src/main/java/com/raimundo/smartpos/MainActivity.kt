package com.raimundo.smartpos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.raimundo.smartpos.core.ui.theme.SmartPosTheme
import com.raimundo.smartpos.ui.SmartPosApp
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartPosTheme {
                SmartPosApp()
            }
        }
    }
}
