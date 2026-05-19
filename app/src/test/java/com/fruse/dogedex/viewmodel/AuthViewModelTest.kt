package com.fruse.dogedex.viewmodel

import app.cash.turbine.test
import com.fruse.dogedex.api.responses.ApiResponseStatus
import com.fruse.dogedex.auth.auth.AuthTasks
import com.fruse.dogedex.auth.auth.AuthUiAction
import com.fruse.dogedex.auth.auth.AuthUiEffect
import com.fruse.dogedex.auth.auth.AuthViewModel
import com.fruse.dogedex.core.di.StringResolver
import com.fruse.dogedex.core.model.User
import com.fruse.dogedex.core.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class AuthViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val strings = StringResolver { id -> "str_$id" }

    private val fakeSessionManager = object : SessionManager {
        override val isLoggedIn: StateFlow<Boolean> = MutableStateFlow(false)
        override fun login(user: User) {}
        override fun logout() {}
    }

    @Test
    fun testLogin_emptyEmail_setsEmailError() = runTest {
        class FakeAuthRepository : AuthTasks {
            override suspend fun login(email: String, password: String): ApiResponseStatus<User> =
                ApiResponseStatus.Success(User(1, email, ""))

            override suspend fun signUp(
                email: String,
                password: String,
                passwordConfirmation: String
            ): ApiResponseStatus<User> = ApiResponseStatus.Success(User(1, "", ""))
        }

        val viewModel = AuthViewModel(FakeAuthRepository(), fakeSessionManager, strings)

        viewModel.handleAction(AuthUiAction.Login("", "pass"))

        assertNotNull(viewModel.uiState.value.emailError)
    }

    @Test
    fun testLogin_emptyPassword_setsPasswordError() = runTest {
        class FakeAuthRepository : AuthTasks {
            override suspend fun login(email: String, password: String): ApiResponseStatus<User> =
                ApiResponseStatus.Success(User(1, email, ""))

            override suspend fun signUp(
                email: String,
                password: String,
                passwordConfirmation: String
            ): ApiResponseStatus<User> = ApiResponseStatus.Success(User(1, "", ""))
        }

        val viewModel = AuthViewModel(FakeAuthRepository(), fakeSessionManager, strings)

        viewModel.handleAction(AuthUiAction.Login("a@b.com", ""))

        assertNotNull(viewModel.uiState.value.passwordError)
    }

    @Test
    fun testLogin_success_emitsNavigateToHomeEffect() = runTest {
        val fakeUser = User(1, "someemail", "")

        class FakeAuthRepository : AuthTasks {
            override suspend fun login(email: String, password: String): ApiResponseStatus<User> =
                ApiResponseStatus.Success(fakeUser)

            override suspend fun signUp(
                email: String,
                password: String,
                passwordConfirmation: String
            ): ApiResponseStatus<User> = ApiResponseStatus.Success(User(1, "", ""))
        }

        val viewModel = AuthViewModel(FakeAuthRepository(), fakeSessionManager, strings)

        viewModel.uiEffect.test {
            viewModel.handleAction(AuthUiAction.Login("someemail", "pass1234"))
            val effect = awaitItem()
            assertEquals(AuthUiEffect.NavigateToHome, effect)
        }
    }

    @Test
    fun testLogin_error_setsErrorState() = runTest {
        class FakeAuthRepository : AuthTasks {
            override suspend fun login(email: String, password: String): ApiResponseStatus<User> =
                ApiResponseStatus.Error(42)

            override suspend fun signUp(
                email: String,
                password: String,
                passwordConfirmation: String
            ): ApiResponseStatus<User> = ApiResponseStatus.Success(User(1, "", ""))
        }

        val viewModel = AuthViewModel(FakeAuthRepository(), fakeSessionManager, strings)

        viewModel.handleAction(AuthUiAction.Login("a@b.com", "pass1234"))

        assertEquals("str_42", viewModel.uiState.value.error)
    }

    @Test
    fun testDismissError_clearsError() = runTest {
        class FakeAuthRepository : AuthTasks {
            override suspend fun login(email: String, password: String): ApiResponseStatus<User> =
                ApiResponseStatus.Error(42)

            override suspend fun signUp(
                email: String,
                password: String,
                passwordConfirmation: String
            ): ApiResponseStatus<User> = ApiResponseStatus.Success(User(1, "", ""))
        }

        val viewModel = AuthViewModel(FakeAuthRepository(), fakeSessionManager, strings)

        viewModel.handleAction(AuthUiAction.Login("a@b.com", "pass1234"))
        viewModel.handleAction(AuthUiAction.DismissError)

        assertNull(viewModel.uiState.value.error)
    }
}
