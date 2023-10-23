package com.fruse.dogedex.auth.auth

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.fruse.dogedex.core.R
import com.fruse.dogedex.core.composables.AuthField
import com.fruse.dogedex.core.composables.BackNavigationIcon
import com.fruse.dogedex.core.api.DogsApi

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onNavigationIconCLick: () -> Unit,
    onSignUpButtonClick: (email: String, password: String, passwordConfirmation: String) -> (Unit),
    authViewModel: AuthViewModel
) {

    Scaffold(topBar = { SignUpScreenToolbar(onNavigationIconCLick) }) {
        Content(
            it.calculateTopPadding(),
            onSignUpButtonClick = onSignUpButtonClick,
            authViewModel,
            resetFieldErrors = { authViewModel.resetErrors() }
        )
    }
}

@Composable
fun SignUpScreenToolbar(
    onNavigationIconCLick: () -> Unit
) {
    TopAppBar(title = { Text(text = stringResource(id = R.string.app_name)) },
        backgroundColor = Color.Red,
        contentColor = Color.White,
        navigationIcon = {
            BackNavigationIcon {
                onNavigationIconCLick()
            }
        })
}

@Composable
private fun Content(
    topPadding: Dp,
    onSignUpButtonClick: (email: String, password: String, passwordConfirmation: String) -> (Unit),
    authViewModel: AuthViewModel,
    resetFieldErrors: () -> Unit
) {

    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val confirmPassword = remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = topPadding)
            .padding(
                top = 32.dp, start = 16.dp, end = 16.dp, bottom = 16.dp
            ), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AuthField(
            label = stringResource(id = R.string.email),
            email = email.value,
            ontTextChanged = {
                email.value = it
                resetFieldErrors()
            },
            modifier = Modifier.fillMaxWidth(),
            errorMessageId = authViewModel.emailError.value
        )

        AuthField(
            label = stringResource(id = R.string.password),
            email = password.value,
            ontTextChanged = {
                password.value = it
                resetFieldErrors()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            visualTransformation = PasswordVisualTransformation(),
            errorMessageId = authViewModel.passwordError.value
        )

        AuthField(
            label = stringResource(id = R.string.confirm_password),
            email = confirmPassword.value,
            ontTextChanged = {
                confirmPassword.value = it
                resetFieldErrors()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            visualTransformation = PasswordVisualTransformation(),
            errorMessageId = authViewModel.passwordConfirmationError.value
        )

        Button(modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .semantics { testTag = "signup-screen-register-button" },
            onClick = {
                onSignUpButtonClick(email.value, password.value, confirmPassword.value)
            }) {
            Text(
                text = stringResource(id = R.string.sign_up),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Preview
@Composable
fun SignUpScreenPreview() {
    SignUpScreen(
        onNavigationIconCLick = {},
        onSignUpButtonClick = { _, _, _ ->

        },
        authViewModel = AuthViewModel(
            authRepository = AuthRepository(
                apiService = DogsApi.retrofitService
            )
        ),
    )
}