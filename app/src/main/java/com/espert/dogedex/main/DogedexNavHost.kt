package com.espert.dogedex.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.espert.dogedex.auth.auth.LoginScreen
import com.espert.dogedex.auth.auth.SignUpScreen
import com.espert.dogedex.core.navigation.CameraKey
import com.espert.dogedex.core.navigation.DogDetailKey
import com.espert.dogedex.core.navigation.DogListKey
import com.espert.dogedex.core.navigation.DogType
import com.espert.dogedex.core.navigation.LoginKey
import com.espert.dogedex.core.navigation.SettingsKey
import com.espert.dogedex.core.navigation.SignUpKey
import com.espert.dogedex.core.session.SessionManager
import com.espert.dogedex.dogDetail.DogDetailScreen
import com.espert.dogedex.dogList.DogListScreen
import com.espert.dogedex.settings.SettingsScreen

@Composable
fun DogedexNavHost(sessionManager: SessionManager) {
    val navController = rememberNavController()
    val isLoggedIn by sessionManager.isLoggedIn.collectAsStateWithLifecycle()
    // val isLoggedIn by remember { mutableStateOf(value = true) }

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
        navController = navController,
        startDestination = startDestination,
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
                kotlin.reflect.typeOf<com.espert.dogedex.core.model.Dog>() to DogType,
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
