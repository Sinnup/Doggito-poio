package com.fruse.dogedex.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.fruse.dogedex.core.session.SessionManager
import com.fruse.dogedex.core.ui.theme.DogedexTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DogedexTheme {
                DogedexNavHost(sessionManager = sessionManager)
            }
        }
    }
}
