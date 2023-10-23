package com.fruse.dogedex.viewmodel

import com.fruse.dogedex.R
import api.responses.ApiResponseStatus
import auth.AuthTasks
import auth.AuthViewModel
import com.fruse.dogedex.core.model.User
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Rule
import org.junit.Test

class AuthViewModelTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    var dogedexCoroutineRule = DogedexTestCoroutineRule()

    @Test
    fun testLoginValidationsCorrect() {

        class FakeAuthRepository : auth.AuthTasks {
            override suspend fun login(email: String, password: String): api.responses.ApiResponseStatus<User> {
                return api.responses.ApiResponseStatus.Success(
                    User(1, email, "")
                )
            }

            override suspend fun signUp(
                email: String,
                password: String,
                passwordConfirmation: String
            ): api.responses.ApiResponseStatus<User> {
                return api.responses.ApiResponseStatus.Success(
                    User(1, "", "")
                )
            }

        }

        val viewModel = auth.AuthViewModel(FakeAuthRepository())

        viewModel.login("", "test1234")
        assertEquals(R.string.email_is_not_valid, viewModel.emailError.value)

        viewModel.login("lalala@gmail.com", "")
        assertEquals(R.string.password_must_not_be_emtpy, viewModel.passwordError.value)
    }

    @Test
    fun testLoginStatesCorrect() {

        val fakeUser = User(1, "someemail", "")
        class FakeAuthRepository : auth.AuthTasks {
            override suspend fun login(email: String, password: String): api.responses.ApiResponseStatus<User> {
                return api.responses.ApiResponseStatus.Success(
                    fakeUser
                )
            }

            override suspend fun signUp(
                email: String,
                password: String,
                passwordConfirmation: String
            ): api.responses.ApiResponseStatus<User> {
                return api.responses.ApiResponseStatus.Success(
                    User(1, "", "")
                )
            }

        }

        val viewModel = auth.AuthViewModel(FakeAuthRepository())

        viewModel.login("lalala@gmail.com", "test1234")
        assertEquals(fakeUser.email, viewModel.user.value?.email)

    }
}