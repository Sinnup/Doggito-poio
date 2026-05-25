package com.espert.dogedex.core.navigation

import com.espert.dogedex.core.model.Dog
import kotlinx.serialization.Serializable

sealed interface NavKey

@Serializable data object CameraKey : NavKey
@Serializable data object DogListKey : NavKey
@Serializable data class DogDetailKey(
    val dog: Dog,
    val probableDogIds: List<String> = emptyList(),
    val isRecognition: Boolean = false
) : NavKey
@Serializable data object SettingsKey : NavKey
