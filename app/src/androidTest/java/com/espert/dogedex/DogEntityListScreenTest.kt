package com.espert.dogedex

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.espert.dogedex.core.model.ResponseStatus
import com.espert.dogedex.dogList.DogListScreen
import com.espert.dogedex.dogList.DogListViewModel
import com.espert.dogedex.dogList.DogTasks
import com.espert.dogedex.core.model.Dog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Rule
import org.junit.Test

class DogEntityListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun progressBarShowsWhenLoadingState() {
        class FakeDogRepository : DogTasks {
            override suspend fun getDogCollection(): ResponseStatus<List<Dog>> {
                return ResponseStatus.Loading()
            }

            override suspend fun addDogToUser(dogId: Long): ResponseStatus<Any> {
                return ResponseStatus.Success(Any())
            }

            override suspend fun getDogByMlId(mlDogId: String): ResponseStatus<Dog> {
                return ResponseStatus.Success(
                    Dog(
                        89, index = 70, "Chow chow", "", "", "",
                        "", "", "", "", "", true
                    )
                )
            }

            override suspend fun getProbableDogs(probableDogsIds: List<String>): Flow<ResponseStatus<Dog>> {
                return emptyFlow()
            }

            override suspend fun insertAllDogs(dogs: List<Dog>) {
                // No-op
            }
        }

        val viewModel = DogListViewModel(
            dogRepository = FakeDogRepository(),
            strings = { "str" }
        )

        composeTestRule.setContent {
            DogListScreen(
                onNavigateBack = { },
                onNavigateToDogDetail = {},
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithTag(testTag = "loading-wheel").assertIsDisplayed()
    }

    @Test
    fun errorDialogShowsIfErrorGettingDogs() {
        class FakeDogRepository : DogTasks {
            override suspend fun getDogCollection(): ResponseStatus<List<Dog>> {
                return ResponseStatus.Error(messageId = R.string.there_was_an_error)
            }

            override suspend fun addDogToUser(dogId: Long): ResponseStatus<Any> {
                return ResponseStatus.Success(Any())
            }

            override suspend fun getDogByMlId(mlDogId: String): ResponseStatus<Dog> {
                return ResponseStatus.Success(
                    Dog(
                        89, index = 70, "Chow chow", "", "", "",
                        "", "", "", "", "", true
                    )
                )
            }

            override suspend fun getProbableDogs(probableDogsIds: List<String>): Flow<ResponseStatus<Dog>> {
                return emptyFlow()
            }

            override suspend fun insertAllDogs(dogs: List<Dog>) {
                // No-op
            }
        }

        val viewModel = DogListViewModel(
            dogRepository = FakeDogRepository(),
            strings = { "str" }
        )

        composeTestRule.setContent {
            DogListScreen(
                onNavigateBack = { },
                onNavigateToDogDetail = {},
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithTag(testTag = "error-dialog").assertIsDisplayed()
    }

    @Test
    fun dogListShowsIfSuccessGettingDogs() {

        val dog1Name = "Chihuahua"

        class FakeDogRepository : DogTasks {
            override suspend fun getDogCollection(): ResponseStatus<List<Dog>> {
                return ResponseStatus.Success(
                    listOf(
                        Dog(
                            1, index = 1, "Chihuahua", "", "", "",
                            "", "", "", "", "", true
                        ),
                        Dog(
                            19, index = 23, "Guillermo", "", "", "",
                            "", "", "", "", "", false
                        )
                    )
                )
            }

            override suspend fun addDogToUser(dogId: Long): ResponseStatus<Any> {
                return ResponseStatus.Success(Any())
            }

            override suspend fun getDogByMlId(mlDogId: String): ResponseStatus<Dog> {
                return ResponseStatus.Success(
                    Dog(
                        89, index = 70, "Chow chow", "", "", "",
                        "", "", "", "", "", true
                    )
                )
            }

            override suspend fun getProbableDogs(probableDogsIds: List<String>): Flow<ResponseStatus<Dog>> {
                return emptyFlow()
            }

            override suspend fun insertAllDogs(dogs: List<Dog>) {
                // No-op
            }
        }

        val viewModel = DogListViewModel(
            dogRepository = FakeDogRepository(),
            strings = { "str" }
        )

        composeTestRule.setContent {
            DogListScreen(
                onNavigateBack = { },
                onNavigateToDogDetail = {},
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithTag(useUnmergedTree = true, testTag = "dog-$dog1Name")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("23").assertIsDisplayed()
    }
}
