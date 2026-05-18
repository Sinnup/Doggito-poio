package com.fruse.dogedex.viewmodel

import com.fruse.dogedex.api.responses.ApiResponseStatus
import com.fruse.dogedex.core.model.Dog
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

        val dogListViewModel = DogListViewModel(dogRepository = FakeDogRepository())

        assertEquals(2, dogListViewModel.dogList.value.size)
        assertEquals(19L, dogListViewModel.dogList.value[1].id)
        assert(dogListViewModel.status.value is ApiResponseStatus.Success)
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

        val dogListViewModel = DogListViewModel(dogRepository = FakeDogRepository())

        assertEquals(0, dogListViewModel.dogList.value.size)
        assert(dogListViewModel.status.value is ApiResponseStatus.Error)
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

        val dogListViewModel = DogListViewModel(dogRepository = FakeDogRepository())

        dogListViewModel.resetApiResponseStatus()
        assert(dogListViewModel.status.value == null)
    }
}
