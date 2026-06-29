package com.yaksperm.analyzer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.yaksperm.analyzer.presentation.navigation.AppNavGraph
import com.yaksperm.analyzer.presentation.theme.NavyBg
import com.yaksperm.analyzer.presentation.theme.YakSpermTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YakSpermTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = NavyBg
                ) {
                    val navController = rememberNavController()
                    AppNavGraph(navController = navController)
                }
            }
        }
    }
}
