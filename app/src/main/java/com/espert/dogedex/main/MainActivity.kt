package com.espert.dogedex.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.espert.dogedex.core.ui.theme.DogedexTheme
import com.espert.dogedex.onboarding.OnboardingScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // Keep the splash on screen until we know whether to show the walkthrough.
        splashScreen.setKeepOnScreenCondition { viewModel.hasSeenOnboarding.value == null }

        viewModel.setUpAssets(this)

        enableEdgeToEdge()
        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            val hasSeenOnboarding by viewModel.hasSeenOnboarding.collectAsStateWithLifecycle()
            DogedexTheme {
                when (hasSeenOnboarding) {
                    // null: still loading — the splash screen is still covering the UI.
                    null -> Unit
                    false -> OnboardingScreen(onFinish = viewModel::onOnboardingCompleted)
                    true -> DogedexNavHost(windowSizeClass = windowSizeClass)
                }
            }
        }
    }

    companion object {
        const val DOGS_JSON_FILE = "dogs.json"
    }
}
