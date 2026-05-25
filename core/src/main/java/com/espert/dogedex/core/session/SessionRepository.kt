package com.espert.dogedex.core.session

import android.content.Context
import com.espert.dogedex.core.api.ApiServiceInterceptor
import com.espert.dogedex.core.model.User
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

interface SessionManager {
    val isLoggedIn: StateFlow<Boolean>
    fun login(user: User)
    fun logout()
}

@Singleton
class SessionRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : SessionManager {
    private val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    private val _isLoggedIn = MutableStateFlow(
        prefs.getString("auth_token", null) != null
    )
    override val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    init {
        val token = prefs.getString("auth_token", null)
        if (token != null) ApiServiceInterceptor.setSessionToken(token)
    }

    override fun login(user: User) {
        prefs.edit()
            .putLong("ID", user.id)
            .putString("email", user.email)
            .putString("auth_token", user.authenticationToken)
            .apply()
        ApiServiceInterceptor.setSessionToken(user.authenticationToken)
        _isLoggedIn.value = true
    }

    override fun logout() {
        prefs.edit().clear().apply()
        ApiServiceInterceptor.setSessionToken("")
        _isLoggedIn.value = false
    }
}
