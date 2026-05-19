package com.fruse.dogedex.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fruse.dogedex.auth.auth.AuthViewModel
import com.fruse.dogedex.auth.auth.LoginScreen
import com.fruse.dogedex.auth.auth.SignUpScreen
import com.fruse.dogedex.core.navigation.CameraKey
import com.fruse.dogedex.core.navigation.DogDetailKey
import com.fruse.dogedex.core.navigation.DogListKey
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
    val isLoggedIn by sessionManager.isLoggedIn.collectAsStateWithLifecycle()

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

    val startDestination = if (sessionManager.isLoggedIn.value) CameraKey else LoginKey

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable<LoginKey> {
            val authViewModel: AuthViewModel = hiltViewModel()
            LoginScreen(
                onRegisterButtonClick = {
                    navController.navigate(SignUpKey)
                },
                onLoginButtonClick = { email, password ->
                    authViewModel.login(email, password)
                },
                authViewModel = authViewModel
            )
        }

        composable<SignUpKey> {
            val authViewModel: AuthViewModel = hiltViewModel()
            SignUpScreen(
                onNavigationIconCLick = {
                    navController.navigateUp()
                },
                onSignUpButtonClick = { email, password, passwordConfirmation ->
                    authViewModel.signUp(email, password, passwordConfirmation)
                },
                authViewModel = authViewModel
            )
        }

        composable<CameraKey> {
            CameraScreen(
                onNavigateToDogDetail = { dog, probableDogIds, isRecognition ->
                    navController.navigate(DogDetailKey(dog, probableDogIds, isRecognition))
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

        composable<DogDetailKey> {
            DogDetailScreen(
                finishActivity = {
                    navController.navigateUp()
                }
            )
        }

        composable<SettingsKey> {
            SettingsScreen()
        }
    }
}
