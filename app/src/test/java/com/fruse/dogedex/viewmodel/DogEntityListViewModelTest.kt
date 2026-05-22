package com.fruse.dogedex.viewmodel

import app.cash.turbine.test
import com.fruse.dogedex.core.api.responses.ResponseStatus
import com.fruse.dogedex.core.model.Dog
import com.fruse.dogedex.dogList.DogListUiAction
import com.fruse.dogedex.dogList.DogListUiEffect
import com.fruse.dogedex.dogList.DogListViewModel
import com.fruse.dogedex.dogList.DogTasks
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test


class DogEntityListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val strings = com.fruse.dogedex.core.di.StringResolver { id -> "str_$id" }

    @Test
    fun downloadDogListStatusesCorrect() = runTest {
        class FakeDogRepository : DogTasks {
            override suspend fun getDogCollection(): ResponseStatus<List<Dog>> {
                return ResponseStatus.Success(
                    listOf(
                        Dog(
                            1, index = 1, "", "", "", "",
                            "", "", "", "", "", false
                        ),
                        Dog(
                            19, index = 2, "", "", "", "",
                            "", "", "", "", "", false
                        )
                    )
                )
            }

            override suspend fun addDogToUser(dogId: Long): ResponseStatus<Any> {
                return ResponseStatus.Success(Unit)
            }

            override suspend fun getDogBYMlId(mlDogId: String): ResponseStatus<Dog> {
                return ResponseStatus.Success(
                    Dog(
                        1, index = 1, "", "", "", "",
                        "", "", "", "", "", false
                    )
                )
            }

            override suspend fun getProbableDogs(probableDogsIds: List<String>): Flow<ResponseStatus<Dog>> = emptyFlow()

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

        val dogListViewModel = DogListViewModel(dogRepository = FakeDogRepository(), strings = strings)

        assertEquals(2, dogListViewModel.uiState.value.dogs.size)
        assertEquals(19L, dogListViewModel.uiState.value.dogs[1].id)
        assertEquals(false, dogListViewModel.uiState.value.isLoading)
        assertEquals(null, dogListViewModel.uiState.value.error)
    }

    @Test
    fun downloadDogListError_StatusesCorrect() = runTest {
        class FakeDogRepository : DogTasks {
            override suspend fun getDogCollection(): ResponseStatus<List<Dog>> {
                return ResponseStatus.Error(messageId = 12)
            }

            override suspend fun addDogToUser(dogId: Long): ResponseStatus<Any> {
                return ResponseStatus.Success(Unit)
            }

            override suspend fun getDogBYMlId(mlDogId: String): ResponseStatus<Dog> {
                return ResponseStatus.Success(
                    Dog(
                        1, index = 1, "", "", "", "",
                        "", "", "", "", "", false
                    )
                )
            }

            override suspend fun getProbableDogs(probableDogsIds: List<String>): Flow<ResponseStatus<Dog>> = emptyFlow()

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

        val dogListViewModel = DogListViewModel(dogRepository = FakeDogRepository(), strings = strings)

        assertEquals(0, dogListViewModel.uiState.value.dogs.size)
        assertEquals("str_12", dogListViewModel.uiState.value.error)
    }

    @Test
    fun downloadDogListResetStatus_StatusesCorrect() = runTest {
        class FakeDogRepository : DogTasks {
            override suspend fun getDogCollection(): ResponseStatus<List<Dog>> {
                return ResponseStatus.Error(messageId = 12)
            }

            override suspend fun addDogToUser(dogId: Long): ResponseStatus<Any> {
                return ResponseStatus.Success(Unit)
            }

            override suspend fun getDogBYMlId(mlDogId: String): ResponseStatus<Dog> {
                return ResponseStatus.Success(
                    Dog(
                        1, index = 1, "", "", "", "",
                        "", "", "", "", "", false
                    )
                )
            }

            override suspend fun getProbableDogs(probableDogsIds: List<String>): Flow<ResponseStatus<Dog>> = emptyFlow()

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

        val dogListViewModel = DogListViewModel(dogRepository = FakeDogRepository(), strings = strings)

        dogListViewModel.handleAction(DogListUiAction.DismissError)
        assertEquals(null, dogListViewModel.uiState.value.error)
    }

    @Test
    fun onDogClicked_emitsNavigateToDogDetailEffect() = runTest {
        val fakeDog = Dog(1, index = 1, "", "", "", "", "", "", "", "", "", false)
        class FakeDogRepository : DogTasks {
            override suspend fun getDogCollection(): ResponseStatus<List<Dog>> =
                ResponseStatus.Success(listOf(fakeDog))

            override suspend fun addDogToUser(dogId: Long): ResponseStatus<Any> =
                ResponseStatus.Success(Unit)

            override suspend fun getDogBYMlId(mlDogId: String): ResponseStatus<Dog> =
                ResponseStatus.Success(fakeDog)

            override suspend fun getProbableDogs(probableDogsIds: List<String>): Flow<ResponseStatus<Dog>> = emptyFlow()

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

        val dogListViewModel = DogListViewModel(dogRepository = FakeDogRepository(), strings = strings)

        dogListViewModel.uiEffect.test {
            dogListViewModel.handleAction(DogListUiAction.OnDogClicked(fakeDog))
            val effect = awaitItem()
            assertEquals(DogListUiEffect.NavigateToDogDetail(fakeDog), effect)
        }
    }
}
