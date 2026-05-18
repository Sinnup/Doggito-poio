package com.fruse.dogedex.auth.auth

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fruse.dogedex.api.responses.ApiResponseStatus
import com.fruse.dogedex.auth.auth.AuthNavDestinations.LoginScreenDestination
import com.fruse.dogedex.auth.auth.AuthNavDestinations.SignUpScreenDestination
import com.fruse.dogedex.core.composables.ErrorDialog
import com.fruse.dogedex.core.composables.LoadingWheel
import com.fruse.dogedex.core.model.User

@Composable
fun AuthScreen(
    authViewModel: AuthViewModel = hiltViewModel(),
    onUserLoggedIn: (User) -> Unit
) {
    val user = authViewModel.user

    val userValue = user.value

    if (userValue != null) {
        onUserLoggedIn(userValue)
    }

    val navController = rememberNavController()
    val status = authViewModel.status.value

    AuthNavHost(
        navController = navController,
        onLoginButtonClick = { email, password ->
            authViewModel.login(email, password)
        },
        onSignUpButtonClick = { email, password, passwordConfirmation ->
            authViewModel.signUp(email, password, passwordConfirmation)
        },
        authViewModel = authViewModel
    )

    if (status is ApiResponseStatus.Loading) {
        LoadingWheel()
    } else if (status is ApiResponseStatus.Error) {
        ErrorDialog(messageId = status.messageId) {
            authViewModel.resetApiResponseStatus()
        }
    }
}

@Composable
private fun AuthNavHost(
    navController: NavHostController,
    onLoginButtonClick: (String, String) -> Unit,
    onSignUpButtonClick: (email: String, password: String, passwordConfirmation: String) -> (Unit),
    authViewModel: AuthViewModel
) {
    NavHost(
        navController = navController,
        startDestination = LoginScreenDestination,
        enterTransition = {
            slideInHorizontally(
                animationSpec = tween(300),
                initialOffsetX = { -it })
        },
        exitTransition = {
            slideOutHorizontally(
                animationSpec = tween(300),
                targetOffsetX = { it })
        },
    ) {
        composable(route = LoginScreenDestination) {
            LoginScreen(
                onRegisterButtonClick = {
                    navController.navigate(route = SignUpScreenDestination) {
                    }
                },
                onLoginButtonClick = onLoginButtonClick,
                authViewModel = authViewModel
            )
        }

        composable(route = SignUpScreenDestination) {
            SignUpScreen(
                onNavigationIconCLick = {
                    navController.navigateUp()
                },
                onSignUpButtonClick = onSignUpButtonClick,
                authViewModel = authViewModel
            )
        }
    }
}