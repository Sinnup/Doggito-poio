package com.fruse.dogedex

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.fruse.dogedex.core.api.responses.ResponseStatus
import com.fruse.dogedex.dogList.DogListScreen
import com.fruse.dogedex.dogList.DogListViewModel
import com.fruse.dogedex.dogList.DogTasks
import com.fruse.dogedex.core.model.Dog
import kotlinx.coroutines.flow.Flow
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
                TODO("Not yet implemented")
            }

            override suspend fun getDogBYMlId(mlDogId: String): ResponseStatus<Dog> {
                TODO("Not yet implemented")
            }

            override suspend fun getProbableDogs(probableDogsIds: List<String>): Flow<ResponseStatus<Dog>> {
                TODO("Not yet implemented")
            }

            override suspend fun insertAllDogs(dogs: List<Dog>) {
                TODO("Not yet implemented")
            }

            override suspend fun getDogCollectionDB(): ResponseStatus<List<Dog>> {
                TODO("Not yet implemented")
            }

            override suspend fun addDogToUserDB(dogId: Long): ResponseStatus<Any> {
                TODO("Not yet implemented")
            }

            override suspend fun getDogBYMlIdDB(mlDogId: String): ResponseStatus<Dog> {
                TODO("Not yet implemented")
            }

            override suspend fun getProbableDogsDB(probableDogsIds: List<String>): Flow<ResponseStatus<Dog>> {
                TODO("Not yet implemented")
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
                TODO("Not yet implemented")
            }

            override suspend fun getDogBYMlId(mlDogId: String): ResponseStatus<Dog> {
                TODO("Not yet implemented")
            }

            override suspend fun getProbableDogs(probableDogsIds: List<String>): Flow<ResponseStatus<Dog>> {
                TODO("Not yet implemented")
            }

            override suspend fun insertAllDogs(dogs: List<Dog>) {
                TODO("Not yet implemented")
            }

            override suspend fun getDogCollectionDB(): ResponseStatus<List<Dog>> {
                TODO("Not yet implemented")
            }

            override suspend fun addDogToUserDB(dogId: Long): ResponseStatus<Any> {
                TODO("Not yet implemented")
            }

            override suspend fun getDogBYMlIdDB(mlDogId: String): ResponseStatus<Dog> {
                TODO("Not yet implemented")
            }

            override suspend fun getProbableDogsDB(probableDogsIds: List<String>): Flow<ResponseStatus<Dog>> {
                TODO("Not yet implemented")
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
        val dog2Name = "Guillermo"

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
                TODO("Not yet implemented")
            }

            override suspend fun getDogBYMlId(mlDogId: String): ResponseStatus<Dog> {
                TODO("Not yet implemented")
            }

            override suspend fun getProbableDogs(probableDogsIds: List<String>): Flow<ResponseStatus<Dog>> {
                TODO("Not yet implemented")
            }

            override suspend fun insertAllDogs(dogs: List<Dog>) {
                TODO("Not yet implemented")
            }

            override suspend fun getDogCollectionDB(): ResponseStatus<List<Dog>> {
                TODO("Not yet implemented")
            }

            override suspend fun addDogToUserDB(dogId: Long): ResponseStatus<Any> {
                TODO("Not yet implemented")
            }

            override suspend fun getDogBYMlIdDB(mlDogId: String): ResponseStatus<Dog> {
                TODO("Not yet implemented")
            }

            override suspend fun getProbableDogsDB(probableDogsIds: List<String>): Flow<ResponseStatus<Dog>> {
                TODO("Not yet implemented")
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
