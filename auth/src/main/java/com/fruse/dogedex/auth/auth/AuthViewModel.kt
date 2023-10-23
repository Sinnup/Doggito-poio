package com.fruse.dogedex.auth.auth

import android.annotation.SuppressLint
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fruse.dogedex.core.R
import com.fruse.dogedex.api.responses.ApiResponseStatus
import com.fruse.dogedex.core.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(private val authRepository: AuthTasks) : ViewModel() {

    var user = mutableStateOf<User?>(null)
        private set

    var emailError = mutableStateOf<Int?>(null)
        private set

    var passwordError = mutableStateOf<Int?>(null)
        private set

    var passwordConfirmationError = mutableStateOf<Int?>(null)
        private set

    var status = mutableStateOf<ApiResponseStatus<User>?>(null)
        private set

    fun login(email: String, password: String) {

        when {
            email.isEmpty() -> emailError.value = R.string.email_is_not_valid
            password.isEmpty() -> passwordError.value =
                R.string.password_must_not_be_emtpy

            else -> {
                viewModelScope.launch {
                    status.value = ApiResponseStatus.Loading()
                    handleResponseStatus(
                        authRepository.login(
                            email,
                            password
                        )
                    )
                }
            }
        }
    }

    fun signUp(email: String, password: String, passwordConfirmation: String) {

        when {
            email.isEmpty() -> emailError.value = R.string.email_is_not_valid
            password.isEmpty() -> passwordError.value =
                R.string.password_must_not_be_emtpy

            passwordConfirmation.isEmpty() -> passwordConfirmationError.value =
                R.string.password_must_not_be_emtpy

            password != passwordConfirmation -> {
                passwordError.value = R.string.passwords_do_not_match
                passwordConfirmationError.value = R.string.passwords_do_not_match
            }

            else -> viewModelScope.launch {
                status.value = ApiResponseStatus.Loading()
                handleResponseStatus(
                    authRepository.signUp(
                        email, password, passwordConfirmation
                    )
                )
            }
        }
    }

    fun resetErrors() {
        emailError.value = null
        passwordError.value = null
        passwordConfirmationError.value = null
    }

    @SuppressLint("NullSafeMutableLiveData")
    private fun handleResponseStatus(apiResponseStatus: ApiResponseStatus<User>) {
        if (apiResponseStatus is ApiResponseStatus.Success) {
            user.value = apiResponseStatus.data
        }

        status.value = apiResponseStatus
    }

    fun resetApiResponseStatus() {
        status.value = null
    }
}