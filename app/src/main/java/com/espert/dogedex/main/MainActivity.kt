package com.espert.dogedex.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.espert.dogedex.core.session.SessionManager
import com.espert.dogedex.core.ui.theme.DogedexTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var sessionManager: SessionManager
    private val viewModel: MainViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.setUpAssets(this)

        enableEdgeToEdge()
        setContent {
            DogedexTheme {
                DogedexNavHost(sessionManager = sessionManager)
            }
        }
    }

    companion object {
        const val DOGS_JSON_FILE = "dogs.json"
    }
}
