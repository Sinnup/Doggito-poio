package com.espert.dogedex.core.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Persisted user preferences. Currently backs the one-time onboarding walkthrough flag.
 */
interface UserPreferencesRepository {
    /** Emits whether the onboarding walkthrough has already been seen/completed. */
    val hasSeenOnboarding: Flow<Boolean>

    /** Marks the onboarding walkthrough as seen so it is never shown again. */
    suspend fun setOnboardingSeen()
}

@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : UserPreferencesRepository {

    override val hasSeenOnboarding: Flow<Boolean> =
        dataStore.data.map { preferences -> preferences[HAS_SEEN_ONBOARDING] ?: false }

    override suspend fun setOnboardingSeen() {
        dataStore.edit { preferences -> preferences[HAS_SEEN_ONBOARDING] = true }
    }

    private companion object {
        val HAS_SEEN_ONBOARDING = booleanPreferencesKey("has_seen_onboarding")
    }
}
