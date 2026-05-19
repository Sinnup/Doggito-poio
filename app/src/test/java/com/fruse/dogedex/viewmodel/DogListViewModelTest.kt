package com.fruse.dogedex.viewmodel

import app.cash.turbine.test
import com.fruse.dogedex.api.responses.ApiResponseStatus
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


class DogListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val strings = com.fruse.dogedex.core.di.StringResolver { id -> "str_$id" }

    @Test
    fun downloadDogListStatusesCorrect() = runTest {
        class FakeDogRepository : DogTasks {
            override suspend fun getDogCollection(): ApiResponseStatus<List<Dog>> {
                return ApiResponseStatus.Success(
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

            override suspend fun addDogToUser(dogId: Long): ApiResponseStatus<Any> {
                return ApiResponseStatus.Success(Unit)
            }

            override suspend fun getDogBYMlId(mlDogId: String): ApiResponseStatus<Dog> {
                return ApiResponseStatus.Success(
                    Dog(
                        1, index = 1, "", "", "", "",
                        "", "", "", "", "", false
                    )
                )
            }

            override suspend fun getProbableDogs(probableDogsIds: List<String>): Flow<ApiResponseStatus<Dog>> = emptyFlow()
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
            override suspend fun getDogCollection(): ApiResponseStatus<List<Dog>> {
                return ApiResponseStatus.Error(messageId = 12)
            }

            override suspend fun addDogToUser(dogId: Long): ApiResponseStatus<Any> {
                return ApiResponseStatus.Success(Unit)
            }

            override suspend fun getDogBYMlId(mlDogId: String): ApiResponseStatus<Dog> {
                return ApiResponseStatus.Success(
                    Dog(
                        1, index = 1, "", "", "", "",
                        "", "", "", "", "", false
                    )
                )
            }

            override suspend fun getProbableDogs(probableDogsIds: List<String>): Flow<ApiResponseStatus<Dog>> = emptyFlow()
        }

        val dogListViewModel = DogListViewModel(dogRepository = FakeDogRepository(), strings = strings)

        assertEquals(0, dogListViewModel.uiState.value.dogs.size)
        assertEquals("str_12", dogListViewModel.uiState.value.error)
    }

    @Test
    fun downloadDogListResetStatus_StatusesCorrect() = runTest {
        class FakeDogRepository : DogTasks {
            override suspend fun getDogCollection(): ApiResponseStatus<List<Dog>> {
                return ApiResponseStatus.Error(messageId = 12)
            }

            override suspend fun addDogToUser(dogId: Long): ApiResponseStatus<Any> {
                return ApiResponseStatus.Success(Unit)
            }

            override suspend fun getDogBYMlId(mlDogId: String): ApiResponseStatus<Dog> {
                return ApiResponseStatus.Success(
                    Dog(
                        1, index = 1, "", "", "", "",
                        "", "", "", "", "", false
                    )
                )
            }

            override suspend fun getProbableDogs(probableDogsIds: List<String>): Flow<ApiResponseStatus<Dog>> = emptyFlow()
        }

        val dogListViewModel = DogListViewModel(dogRepository = FakeDogRepository(), strings = strings)

        dogListViewModel.handleAction(DogListUiAction.DismissError)
        assertEquals(null, dogListViewModel.uiState.value.error)
    }

    @Test
    fun onDogClicked_emitsNavigateToDogDetailEffect() = runTest {
        val fakeDog = Dog(1, index = 1, "", "", "", "", "", "", "", "", "", false)
        class FakeDogRepository : DogTasks {
            override suspend fun getDogCollection(): ApiResponseStatus<List<Dog>> =
                ApiResponseStatus.Success(listOf(fakeDog))

            override suspend fun addDogToUser(dogId: Long): ApiResponseStatus<Any> =
                ApiResponseStatus.Success(Unit)

            override suspend fun getDogBYMlId(mlDogId: String): ApiResponseStatus<Dog> =
                ApiResponseStatus.Success(fakeDog)

            override suspend fun getProbableDogs(probableDogsIds: List<String>): Flow<ApiResponseStatus<Dog>> = emptyFlow()
        }

        val dogListViewModel = DogListViewModel(dogRepository = FakeDogRepository(), strings = strings)

        dogListViewModel.uiEffect.test {
            dogListViewModel.handleAction(DogListUiAction.OnDogClicked(fakeDog))
            val effect = awaitItem()
            assertEquals(DogListUiEffect.NavigateToDogDetail(fakeDog), effect)
        }
    }
}
