package com.fruse.dogedex.settings

import androidx.lifecycle.ViewModel
import com.fruse.dogedex.core.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {
    fun logout() = sessionManager.logout()
}
