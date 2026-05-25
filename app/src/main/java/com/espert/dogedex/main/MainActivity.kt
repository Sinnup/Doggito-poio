package com.espert.dogedex.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.espert.dogedex.core.ui.theme.DogedexTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        viewModel.setUpAssets(this)

        enableEdgeToEdge()
        setContent {
            DogedexTheme {
                DogedexNavHost()
            }
        }
    }

    companion object {
        const val DOGS_JSON_FILE = "dogs.json"
    }
}
