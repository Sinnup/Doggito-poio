package com.fruse.dogedex.auth.auth

import android.annotation.SuppressLint
import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.TopAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.fruse.dogedex.core.api.DogsApi
import com.fruse.dogedex.core.composables.AuthField
import com.fruse.dogedex.core.ui.theme.DogedexTheme

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onRegisterButtonClick: () -> Unit,
    onLoginButtonClick: (email: String, password: String) -> Unit,
    authViewModel: AuthViewModel
) {
    Scaffold(
        topBar = { LoginScreenToolbar() }
    ) {
        Surface {
            Content(
                it.calculateTopPadding(),
                onRegisterButtonClick,
                onLoginButtonClick,
                authViewModel
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
private fun Content(
    topPadding: Dp,
    onRegisterButtonClick: () -> Unit,
    onLoginButtonClick: (email: String, password: String) -> Unit,
    authViewModel: AuthViewModel
) {

    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = topPadding)
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
                authViewModel.resetErrors()
            },
            modifier = Modifier
                .fillMaxWidth(),
            errorMessageId = authViewModel.emailError.value,
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
            errorMessageId = authViewModel.passwordError.value,
            errorSemantic = "password-field-error",
            fieldSemantic = "password-field"
        )

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .semantics { testTag = "login-button" },
            onClick = {
                onLoginButtonClick(email.value, password.value)
            },
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
                .clickable(enabled = true, onClick = onRegisterButtonClick)
                .fillMaxWidth()
                .padding(16.dp)
                .semantics { testTag = "login-screen-register-button" },
            textAlign = TextAlign.Center,
            text = stringResource(id = R.string.register),
            fontWeight = FontWeight.Medium,
        )
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
//        LoginScreen(
//            onRegisterButtonClick = {},
//            onLoginButtonClick = { _, _ -> },
//            authViewModel = AuthViewModel(
//                authRepository = AuthRepository(
//                    apiService = DogsApi.retrofitService
//                )
//            )
//        )
        Surface {
            Content(
                topPadding = 0.dp,
                onRegisterButtonClick = { },
                onLoginButtonClick = { _, _ -> },
                authViewModel = AuthViewModel(
                    authRepository = AuthRepository(
                        apiService = DogsApi.retrofitService
                    )
                )
            )
        }

    }
}