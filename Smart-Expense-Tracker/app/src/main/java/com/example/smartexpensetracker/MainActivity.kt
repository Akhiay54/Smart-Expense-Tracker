package com.example.smartexpensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.smartexpensetracker.ui.navigation.AppNavHost
import com.example.smartexpensetracker.ui.theme.SmartExpenseTrackerTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
        SmartExpenseTrackerTheme(darkTheme = isSystemInDarkTheme()) {
                val navController = rememberNavController()
                AppNavHost(navController = navController, modifier = Modifier)
            }
        }
    }
}

