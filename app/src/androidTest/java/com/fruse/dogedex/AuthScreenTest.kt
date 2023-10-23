package com.fruse.dogedex

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.fruse.dogedex.api.responses.ApiResponseStatus
import com.fruse.dogedex.auth.auth.AuthScreen
import com.fruse.dogedex.auth.auth.AuthTasks
import com.fruse.dogedex.auth.auth.AuthViewModel
import com.fruse.dogedex.core.model.User
import org.junit.Rule
import org.junit.Test

class AuthScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testTappingRegisterButtonOpenSignUpScreen() {
        class FakeAuthRepository : AuthTasks {
            override suspend fun login(email: String, password: String): ApiResponseStatus<User> {
                TODO("Not yet implemented")
            }

            override suspend fun signUp(
                email: String,
                password: String,
                passwordConfirmation: String
            ): ApiResponseStatus<User> {
                TODO("Not yet implemented")
            }

        }

        val viewModel = AuthViewModel(
            authRepository = FakeAuthRepository()
        )

        composeTestRule.setContent {
            AuthScreen(
                onUserLoggedIn = {},
                authViewModel = viewModel
            )
        }

        composeTestRule.onNodeWithTag(testTag = "login-button").assertIsDisplayed()
        composeTestRule.onNodeWithTag(testTag = "login-screen-register-button").performClick()
        composeTestRule.onNodeWithTag(testTag = "signup-screen-register-button").assertIsDisplayed()
    }

    @Test
    fun testEmailErrorShowsIfTappingLoginButtonAndNotEmail() {
        class FakeAuthRepository : AuthTasks {
            override suspend fun login(email: String, password: String): ApiResponseStatus<User> {
                TODO("Not yet implemented")
            }

            override suspend fun signUp(
                email: String,
                password: String,
                passwordConfirmation: String
            ): ApiResponseStatus<User> {
                TODO("Not yet implemented")
            }
        }

        val viewModel = AuthViewModel(
            authRepository = FakeAuthRepository()
        )

        composeTestRule.setContent {
            AuthScreen(
                onUserLoggedIn = {},
                authViewModel = viewModel
            )
        }

        composeTestRule.onNodeWithTag(testTag = "login-button").performClick()
        composeTestRule.onNodeWithTag(testTag = "email-field-error").assertIsDisplayed()
        composeTestRule.onNodeWithTag(testTag = "email-field").performTextInput("fruse.experiencias@gmail.com")
        composeTestRule.onNodeWithTag(testTag = "login-button").performClick()
        composeTestRule.onNodeWithTag(testTag = "password-field-error").assertIsDisplayed()
    }

}