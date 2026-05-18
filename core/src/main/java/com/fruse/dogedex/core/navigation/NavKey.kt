package com.fruse.dogedex.core.navigation

import com.fruse.dogedex.core.model.Dog
import kotlinx.serialization.Serializable

sealed interface NavKey

@Serializable data object LoginKey : NavKey
@Serializable data object SignUpKey : NavKey
@Serializable data object CameraKey : NavKey
@Serializable data object DogListKey : NavKey
@Serializable data class DogDetailKey(
    val dog: Dog,
    val probableDogIds: List<String> = emptyList(),
    val isRecognition: Boolean = false
) : NavKey
@Serializable data object SettingsKey : NavKey
