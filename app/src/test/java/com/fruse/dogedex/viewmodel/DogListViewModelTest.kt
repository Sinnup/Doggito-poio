package com.fruse.dogedex.viewmodel

import api.responses.ApiResponseStatus
import com.fruse.dogedex.dogList.DogListViewModel
import com.fruse.dogedex.dogList.DogTasks
import com.fruse.dogedex.core.model.Dog
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Rule
import org.junit.Test


class DogListViewModelTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    var dogedexCoroutineRule = DogedexTestCoroutineRule()

    @Test
    fun downloadDogListStatusesCorrect() {
        class FakeDogRepository() : DogTasks {
            override suspend fun getDogCollection(): api.responses.ApiResponseStatus<List<Dog>> {
                return api.responses.ApiResponseStatus.Success(
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

            override suspend fun addDogToUser(dogId: Long): api.responses.ApiResponseStatus<Any> {
                return api.responses.ApiResponseStatus.Success(Unit)
            }

            override suspend fun getDogBYMlId(mlDogId: String): api.responses.ApiResponseStatus<Dog> {
                return api.responses.ApiResponseStatus.Success(
                    Dog(
                        1, index = 1, "", "", "", "",
                        "", "", "", "", "", false
                    )
                )
            }

        }

        val dogListViewModel = DogListViewModel(dogRepository = FakeDogRepository())

        assertEquals(2, dogListViewModel.dogList.value.size)
        assertEquals(19, dogListViewModel.dogList.value[1].id)
        assert(dogListViewModel.status.value is api.responses.ApiResponseStatus.Success)
    }

    @Test
    fun downloadDogListError_StatusesCorrect() {
        class FakeDogRepository() : DogTasks {
            override suspend fun getDogCollection(): api.responses.ApiResponseStatus<List<Dog>> {
                return api.responses.ApiResponseStatus.Error(messageId = 12)
            }

            override suspend fun addDogToUser(dogId: Long): api.responses.ApiResponseStatus<Any> {
                return api.responses.ApiResponseStatus.Success(Unit)
            }

            override suspend fun getDogBYMlId(mlDogId: String): api.responses.ApiResponseStatus<Dog> {
                return api.responses.ApiResponseStatus.Success(
                    Dog(
                        1, index = 1, "", "", "", "",
                        "", "", "", "", "", false
                    )
                )
            }

        }

        val dogListViewModel = DogListViewModel(dogRepository = FakeDogRepository())

        assertEquals(0, dogListViewModel.dogList.value.size)
        assert(dogListViewModel.status.value is api.responses.ApiResponseStatus.Error)
    }


    @Test
    fun downloadDogListResetStatus_StatusesCorrect() {
        class FakeDogRepository() : DogTasks {
            override suspend fun getDogCollection(): api.responses.ApiResponseStatus<List<Dog>> {
                return api.responses.ApiResponseStatus.Error(messageId = 12)
            }

            override suspend fun addDogToUser(dogId: Long): api.responses.ApiResponseStatus<Any> {
                return api.responses.ApiResponseStatus.Success(Unit)
            }

            override suspend fun getDogBYMlId(mlDogId: String): api.responses.ApiResponseStatus<Dog> {
                return api.responses.ApiResponseStatus.Success(
                    Dog(
                        1, index = 1, "", "", "", "",
                        "", "", "", "", "", false
                    )
                )
            }

        }

        val dogListViewModel = DogListViewModel(dogRepository = FakeDogRepository())

        dogListViewModel.resetApiResponseStatus()
        assert(dogListViewModel.status.value == null)
    }
}