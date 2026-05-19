package com.fruse.dogedex.auth.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fruse.dogedex.core.R
import com.fruse.dogedex.api.responses.ApiResponseStatus
import com.fruse.dogedex.core.di.StringResolver
import com.fruse.dogedex.core.model.User
import com.fruse.dogedex.core.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val passwordConfirmationError: String? = null,
    val error: String? = null
)

sealed class AuthUiAction {
    data class Login(val email: String, val password: String) : AuthUiAction()
    data class SignUp(
        val email: String,
        val password: String,
        val passwordConfirmation: String
    ) : AuthUiAction()
    data object NavigateToSignUp : AuthUiAction()
    data object NavigateToLogin : AuthUiAction()
    data object DismissError : AuthUiAction()
    data object ResetFieldErrors : AuthUiAction()
}

sealed class AuthUiEffect {
    data object NavigateToHome : AuthUiEffect()
    data object NavigateToSignUp : AuthUiEffect()
    data object NavigateToLogin : AuthUiEffect()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthTasks,
    private val sessionManager: SessionManager,
    private val strings: StringResolver
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<AuthUiEffect>(Channel.BUFFERED)
    val uiEffect: Flow<AuthUiEffect> = _uiEffect.receiveAsFlow()

    fun handleAction(action: AuthUiAction) {
        when (action) {
            is AuthUiAction.Login -> login(action.email, action.password)
            is AuthUiAction.SignUp -> signUp(action.email, action.password, action.passwordConfirmation)
            is AuthUiAction.NavigateToSignUp -> viewModelScope.launch {
                _uiEffect.send(AuthUiEffect.NavigateToSignUp)
            }
            is AuthUiAction.NavigateToLogin -> viewModelScope.launch {
                _uiEffect.send(AuthUiEffect.NavigateToLogin)
            }
            is AuthUiAction.DismissError -> _uiState.update { it.copy(error = null) }
            is AuthUiAction.ResetFieldErrors -> _uiState.update {
                it.copy(emailError = null, passwordError = null, passwordConfirmationError = null)
            }
        }
    }

    private fun login(email: String, password: String) {
        when {
            email.isEmpty() -> _uiState.update {
                it.copy(emailError = strings.resolve(R.string.email_is_not_valid))
            }
            password.isEmpty() -> _uiState.update {
                it.copy(passwordError = strings.resolve(R.string.password_must_not_be_emtpy))
            }
            else -> viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }
                handleResponseStatus(authRepository.login(email, password))
            }
        }
    }

    private fun signUp(email: String, password: String, passwordConfirmation: String) {
        when {
            email.isEmpty() -> _uiState.update {
                it.copy(emailError = strings.resolve(R.string.email_is_not_valid))
            }
            password.isEmpty() -> _uiState.update {
                it.copy(passwordError = strings.resolve(R.string.password_must_not_be_emtpy))
            }
            passwordConfirmation.isEmpty() -> _uiState.update {
                it.copy(passwordConfirmationError = strings.resolve(R.string.password_must_not_be_emtpy))
            }
            password != passwordConfirmation -> _uiState.update {
                it.copy(
                    passwordError = strings.resolve(R.string.passwords_do_not_match),
                    passwordConfirmationError = strings.resolve(R.string.passwords_do_not_match)
                )
            }
            else -> viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }
                handleResponseStatus(authRepository.signUp(email, password, passwordConfirmation))
            }
        }
    }

    private suspend fun handleResponseStatus(apiResponseStatus: ApiResponseStatus<User>) {
        when (apiResponseStatus) {
            is ApiResponseStatus.Success -> {
                sessionManager.login(apiResponseStatus.data)
                _uiState.update { it.copy(isLoading = false) }
                _uiEffect.send(AuthUiEffect.NavigateToHome)
            }
            is ApiResponseStatus.Error -> _uiState.update {
                it.copy(isLoading = false, error = strings.resolve(apiResponseStatus.messageId))
            }
            is ApiResponseStatus.Loading -> _uiState.update { it.copy(isLoading = true) }
        }
    }
}
