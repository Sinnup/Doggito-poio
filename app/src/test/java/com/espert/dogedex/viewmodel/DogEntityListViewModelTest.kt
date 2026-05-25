package com.espert.dogedex.viewmodel

import app.cash.turbine.test
import com.espert.dogedex.core.model.ResponseStatus
import com.espert.dogedex.core.model.Dog
import com.espert.dogedex.dogList.DogListUiAction
import com.espert.dogedex.dogList.DogListUiEffect
import com.espert.dogedex.dogList.DogListViewModel
import com.espert.dogedex.dogList.DogTasks
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test


class DogEntityListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val strings = com.espert.dogedex.core.di.StringResolver { id -> "str_$id" }

    class FakeDogRepository : DogTasks {
        var dogs = listOf(
            Dog(1, index = 1, "", "", "", "", "", "", "", "", "", false),
            Dog(19, index = 2, "", "", "", "", "", "", "", "", "", false)
        )
        var status: ResponseStatus<List<Dog>> = ResponseStatus.Success(dogs)

        override suspend fun getDogCollection(): ResponseStatus<List<Dog>> = status
        override suspend fun addDogToUser(dogId: Long): ResponseStatus<Any> = ResponseStatus.Success(Unit)
        override suspend fun getDogByMlId(mlDogId: String): ResponseStatus<Dog> = ResponseStatus.Success(dogs[0])
        override suspend fun getProbableDogs(probableDogsIds: List<String>): Flow<ResponseStatus<Dog>> = emptyFlow()
        override suspend fun insertAllDogs(dogs: List<Dog>) {}
    }

    @Test
    fun downloadDogListStatusesCorrect() = runTest {
        val dogListViewModel = DogListViewModel(dogRepository = FakeDogRepository(), strings = strings)

        assertEquals(2, dogListViewModel.uiState.value.dogs.size)
        assertEquals(19L, dogListViewModel.uiState.value.dogs[1].id)
        assertEquals(false, dogListViewModel.uiState.value.isLoading)
        assertEquals(null, dogListViewModel.uiState.value.error)
    }

    @Test
    fun downloadDogListError_StatusesCorrect() = runTest {
        val fakeRepo = FakeDogRepository()
        fakeRepo.status = ResponseStatus.Error(messageId = 12)
        val dogListViewModel = DogListViewModel(dogRepository = fakeRepo, strings = strings)

        assertEquals(0, dogListViewModel.uiState.value.dogs.size)
        assertEquals("str_12", dogListViewModel.uiState.value.error)
    }

    @Test
    fun downloadDogListResetStatus_StatusesCorrect() = runTest {
        val fakeRepo = FakeDogRepository()
        fakeRepo.status = ResponseStatus.Error(messageId = 12)
        val dogListViewModel = DogListViewModel(dogRepository = fakeRepo, strings = strings)

        dogListViewModel.handleAction(DogListUiAction.DismissError)
        assertEquals(null, dogListViewModel.uiState.value.error)
    }

    @Test
    fun onDogClicked_emitsNavigateToDogDetailEffect() = runTest {
        val fakeRepo = FakeDogRepository()
        val dogListViewModel = DogListViewModel(dogRepository = fakeRepo, strings = strings)

        dogListViewModel.uiEffect.test {
            dogListViewModel.handleAction(DogListUiAction.OnDogClicked(fakeRepo.dogs[0]))
            val effect = awaitItem()
            assertEquals(DogListUiEffect.NavigateToDogDetail(fakeRepo.dogs[0]), effect)
        }
    }
}
