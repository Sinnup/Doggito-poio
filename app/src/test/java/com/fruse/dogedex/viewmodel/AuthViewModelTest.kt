package com.fruse.dogedex.viewmodel

import com.fruse.dogedex.api.responses.ApiResponseStatus
import com.fruse.dogedex.auth.auth.AuthTasks
import com.fruse.dogedex.auth.auth.AuthViewModel
import com.fruse.dogedex.core.model.User
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class AuthViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun testLoginValidationsCorrect() = runTest {

        class FakeAuthRepository : AuthTasks {
            override suspend fun login(email: String, password: String): ApiResponseStatus<User> {
                return ApiResponseStatus.Success(
                    User(1, email, "")
                )
            }

            override suspend fun signUp(
                email: String,
                password: String,
                passwordConfirmation: String
            ): ApiResponseStatus<User> {
                return ApiResponseStatus.Success(
                    User(1, "", "")
                )
            }

        }

        val viewModel = AuthViewModel(FakeAuthRepository())

        viewModel.login("", "test1234")
        assert(viewModel.emailError.value != null)

        viewModel.login("lalala@gmail.com", "")
        assert(viewModel.passwordError.value != null)
    }

    @Test
    fun testLoginStatesCorrect() = runTest {

        val fakeUser = User(1, "someemail", "")
        class FakeAuthRepository : AuthTasks {
            override suspend fun login(email: String, password: String): ApiResponseStatus<User> {
                return ApiResponseStatus.Success(
                    fakeUser
                )
            }

            override suspend fun signUp(
                email: String,
                password: String,
                passwordConfirmation: String
            ): ApiResponseStatus<User> {
                return ApiResponseStatus.Success(
                    User(1, "", "")
                )
            }

        }

        val viewModel = AuthViewModel(FakeAuthRepository())

        viewModel.login("lalala@gmail.com", "test1234")
        assertEquals(fakeUser.email, viewModel.user.value?.email)

    }
}
