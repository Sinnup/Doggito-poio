package com.espert.dogedex.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.espert.dogedex.core.navigation.CameraKey
import com.espert.dogedex.core.navigation.DogDetailKey
import com.espert.dogedex.core.navigation.DogListKey
import com.espert.dogedex.core.navigation.DogType
import com.espert.dogedex.core.navigation.SettingsKey
import com.espert.dogedex.dogDetail.DogDetailScreen
import com.espert.dogedex.dogList.DogListScreen
import com.espert.dogedex.settings.SettingsScreen

@Composable
fun DogedexNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = CameraKey,
    ) {
        composable<CameraKey> {
            LaunchedEffect(CameraKey) {
                navController.clearBackStack(CameraKey)
            }
            CameraScreen(
                onNavigateToDogDetail = { dog, probableDogIds ->
                    navController.navigate(DogDetailKey(dog, probableDogIds, true))
                },
                onNavigateToDogList = {
                    navController.navigate(DogListKey)
                },
                onNavigateToSettings = {
                    navController.navigate(SettingsKey)
                }
            )
        }

        composable<DogListKey> {
            DogListScreen(
                onNavigateToDogDetail = { dog ->
                    navController.navigate(DogDetailKey(dog, emptyList(), false))
                },
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }

        composable<DogDetailKey>(
            typeMap = mapOf(
                kotlin.reflect.typeOf<com.espert.dogedex.core.model.Dog>() to DogType,
            )
        ) {
            val args = it.toRoute<DogDetailKey>()
            DogDetailScreen(
                dog = args.dog,
                probableDogIds = args.probableDogIds,
                isRecognition = args.isRecognition,
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }

        composable<SettingsKey> {
            SettingsScreen()
        }
    }
}
