package com.fruse.dogedex.auth.auth

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.TopAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fruse.dogedex.core.R
import com.fruse.dogedex.core.composables.AuthField
import com.fruse.dogedex.core.composables.ErrorDialog
import com.fruse.dogedex.core.ui.theme.DogedexTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onNavigateToSignUp: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is AuthUiEffect.NavigateToHome -> onNavigateToHome()
                is AuthUiEffect.NavigateToSignUp -> onNavigateToSignUp()
                is AuthUiEffect.NavigateToLogin -> Unit
            }
        }
    }

    Scaffold(
        topBar = { LoginScreenToolbar() },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { paddingValues ->
        Surface {
            LoginContent(
                paddingValues = paddingValues,
                uiState = uiState,
                onNavigateToSignUp = onNavigateToSignUp,
                onLoginClick = { email, password ->
                    viewModel.handleAction(AuthUiAction.Login(email, password))
                },
                onFieldChanged = { viewModel.handleAction(AuthUiAction.ResetFieldErrors) },
                onDismissError = { viewModel.handleAction(AuthUiAction.DismissError) }
            )
        }
    }
}

@Composable
fun LoginScreenToolbar() {
    TopAppBar(
        title = { Text(text = stringResource(id = R.string.app_name)) },
        backgroundColor = Color.Red,
        contentColor = Color.White
    )
}

@Composable
private fun LoginContent(
    paddingValues: PaddingValues,
    uiState: AuthUiState,
    onNavigateToSignUp: () -> Unit,
    onLoginClick: (email: String, password: String) -> Unit,
    onFieldChanged: () -> Unit,
    onDismissError: () -> Unit
) {
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(paddingValues)
            .padding(
                top = 32.dp,
                start = 16.dp,
                end = 16.dp,
                bottom = 16.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AuthField(
            label = stringResource(id = R.string.email),
            email = email.value,
            ontTextChanged = {
                email.value = it
                onFieldChanged()
            },
            modifier = Modifier.fillMaxWidth(),
            errorMessage = uiState.emailError,
            errorSemantic = "email-field-error",
            fieldSemantic = "email-field"
        )

        AuthField(
            label = stringResource(id = R.string.password),
            email = password.value,
            ontTextChanged = { password.value = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            visualTransformation = PasswordVisualTransformation(),
            errorMessage = uiState.passwordError,
            errorSemantic = "password-field-error",
            fieldSemantic = "password-field"
        )

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .semantics { testTag = "login-button" },
            onClick = { onLoginClick(email.value, password.value) },
        ) {
            Text(
                text = stringResource(id = R.string.login),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )
        }

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            textAlign = TextAlign.Center,
            text = stringResource(id = R.string.do_not_have_an_account)
        )

        Text(
            modifier = Modifier
                .clickable(enabled = true, onClick = onNavigateToSignUp)
                .fillMaxWidth()
                .padding(16.dp)
                .semantics { testTag = "login-screen-register-button" },
            textAlign = TextAlign.Center,
            text = stringResource(id = R.string.register),
            fontWeight = FontWeight.Medium,
        )
    }

    if (uiState.error != null) {
        ErrorDialog(message = uiState.error) {
            onDismissError()
        }
    }
}

@Preview(
    uiMode = UI_MODE_NIGHT_YES,
    name = "DefaultPreviewDark"
)
@Preview(
    uiMode = UI_MODE_NIGHT_NO,
    name = "DefaultPreviewLight"
)
@Composable
fun LoginScreenPreview() {
    DogedexTheme {
        Surface {
            LoginContent(
                paddingValues = PaddingValues(0.dp),
                uiState = AuthUiState(),
                onNavigateToSignUp = {},
                onLoginClick = { _, _ -> },
                onFieldChanged = {},
                onDismissError = {}
            )
        }
    }
}
