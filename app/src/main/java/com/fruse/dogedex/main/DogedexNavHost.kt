package com.fruse.dogedex.main

import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.fruse.dogedex.auth.auth.LoginScreen
import com.fruse.dogedex.auth.auth.SignUpScreen
import com.fruse.dogedex.core.navigation.CameraKey
import com.fruse.dogedex.core.navigation.DogDetailKey
import com.fruse.dogedex.core.navigation.DogListKey
import com.fruse.dogedex.core.navigation.DogType
import com.fruse.dogedex.core.navigation.LoginKey
import com.fruse.dogedex.core.navigation.SettingsKey
import com.fruse.dogedex.core.navigation.SignUpKey
import com.fruse.dogedex.core.session.SessionManager
import com.fruse.dogedex.dogDetail.DogDetailScreen
import com.fruse.dogedex.dogList.DogListScreen
import com.fruse.dogedex.settings.SettingsScreen

@Composable
fun DogedexNavHost(sessionManager: SessionManager) {
    val navController = rememberNavController()
//    val isLoggedIn by sessionManager.isLoggedIn.collectAsStateWithLifecycle() // Use when login is good again.
    val isLoggedIn by remember { mutableStateOf(true) }

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            navController.navigate(CameraKey) {
                popUpTo(LoginKey) { inclusive = true }
            }
        } else {
            navController.navigate(LoginKey) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    val startDestination = if (isLoggedIn) CameraKey else LoginKey

    NavHost(
//        modifier = Modifier.safeContentPadding(),
        navController = navController,
        startDestination = startDestination
    ) {
        composable<LoginKey> {
            LoginScreen(
                onNavigateToSignUp = {
                    navController.navigate(SignUpKey)
                },
                onNavigateToHome = {
                    navController.navigate(CameraKey) {
                        popUpTo(LoginKey) { inclusive = true }
                    }
                }
            )
        }

        composable<SignUpKey> {
            SignUpScreen(
                onNavigateBack = {
                    navController.navigateUp()
                },
                onNavigateToHome = {
                    navController.navigate(CameraKey) {
                        popUpTo(LoginKey) { inclusive = true }
                    }
                }
            )
        }

        composable<CameraKey> {
            LaunchedEffect(CameraKey) {
                navController.clearBackStack(CameraKey)
            }
            CameraScreen(
                onNavigateToDogDetail = { dog, probableDogIds ->
                    navController.navigate(DogDetailKey(dog, probableDogIds, true))
                },
                onNavigateToDogList = {
                    navController.navigate(DogListKey)
                },
                onNavigateToSettings = {
                    navController.navigate(SettingsKey)
                }
            )
        }

        composable<DogListKey> {
            DogListScreen(
                onNavigateToDogDetail = { dog ->
                    navController.navigate(DogDetailKey(dog, emptyList(), false))
                },
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }

        composable<DogDetailKey>(
            typeMap = mapOf(
                kotlin.reflect.typeOf<com.fruse.dogedex.core.model.Dog>() to DogType,
            )
        ) {
            val args = it.toRoute<DogDetailKey>()
            DogDetailScreen(
                dog = args.dog,
                probableDogIds = args.probableDogIds,
                isRecognition = args.isRecognition,
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }

        composable<SettingsKey> {
            SettingsScreen()
        }
    }
}
