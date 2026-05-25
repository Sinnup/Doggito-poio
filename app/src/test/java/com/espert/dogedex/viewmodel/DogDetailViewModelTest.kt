package com.espert.dogedex.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.testing.invoke
import app.cash.turbine.test
import com.espert.dogedex.core.model.ResponseStatus
import com.espert.dogedex.core.di.StringResolver
import com.espert.dogedex.core.model.Dog
import com.espert.dogedex.core.navigation.DogDetailKey
import com.espert.dogedex.core.navigation.DogType
import com.espert.dogedex.dogDetail.DogDetailUiAction
import com.espert.dogedex.dogDetail.DogDetailUiEffect
import com.espert.dogedex.dogDetail.DogDetailViewModel
import com.espert.dogedex.dogList.DogTasks
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests for [DogDetailViewModel] action handlers.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class DogDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val strings = StringResolver { id -> "str_$id" }

    private val testDog = Dog(
        id = 89L,
        index = 70,
        name = "Chow chow",
        type = "Non-Sporting",
        heightFemale = "43",
        heightMale = "46",
        imageUrl = "",
        lifeExpectancy = "11",
        temperament = "Loyal",
        weightFemale = "20",
        weightMale = "25",
        inCollection = true
    )

    // -------------------------------------------------------------------------
    // Fake repository helpers
    // -------------------------------------------------------------------------

    private fun makeFakeRepo(
        addDogResult: ResponseStatus<Any> = ResponseStatus.Success(Unit),
        probableDogsFlow: Flow<ResponseStatus<Dog>> = emptyFlow()
    ): DogTasks = object : DogTasks {
        override suspend fun getDogCollection(): ResponseStatus<List<Dog>> =
            throw AssertionError("unexpected call to getDogCollection")

        override suspend fun addDogToUser(dogId: Long): ResponseStatus<Any> = addDogResult

        override suspend fun getDogByMlId(mlDogId: String): ResponseStatus<Dog> =
            throw AssertionError("unexpected call to getDogByMlId")

        override suspend fun getProbableDogs(probableDogsIds: List<String>): Flow<ResponseStatus<Dog>> =
            probableDogsFlow

        override suspend fun insertAllDogs(dogs: List<Dog>) =
            throw AssertionError("unexpected call to insertAllDogs")
    }

    private fun createViewModel(repo: DogTasks = makeFakeRepo()): DogDetailViewModel {
        val route = DogDetailKey(testDog)
        val handle = SavedStateHandle(
            route = route,
            typeMap = mapOf(kotlin.reflect.typeOf<Dog>() to DogType)
        )
        return DogDetailViewModel(
            dogRepository = repo,
            strings = strings,
            savedStateHandle = handle
        )
    }

    // -------------------------------------------------------------------------
    // DismissError
    // -------------------------------------------------------------------------

    @Test
    fun dismissError_clearsErrorField() = runTest {
        val errorRepo = makeFakeRepo(addDogResult = ResponseStatus.Error(42))
        val viewModel = createViewModel(errorRepo)

        viewModel.handleAction(DogDetailUiAction.AddDogToUser)

        // Error should be set
        assertEquals("str_42", viewModel.uiState.value.error)

        viewModel.handleAction(DogDetailUiAction.DismissError)

        assertNull(viewModel.uiState.value.error)
    }

    // -------------------------------------------------------------------------
    // UpdateDog
    // -------------------------------------------------------------------------

    @Test
    fun updateDog_setsNewDogInState() = runTest {
        val viewModel = createViewModel()
        val otherDog = testDog.copy(name = "Other")

        viewModel.handleAction(DogDetailUiAction.UpdateDog(otherDog))

        assertEquals(otherDog, viewModel.uiState.value.dog)
    }

    // -------------------------------------------------------------------------
    // NavigateBack
    // -------------------------------------------------------------------------

    @Test
    fun navigateBack_emitsNavigateBackEffect() = runTest {
        val viewModel = createViewModel()

        viewModel.uiEffect.test {
            viewModel.handleAction(DogDetailUiAction.NavigateBack)

            val effect = awaitItem()
            assertTrue(effect is DogDetailUiEffect.NavigateBack)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // -------------------------------------------------------------------------
    // AddDogToUser — success
    // -------------------------------------------------------------------------

    @Test
    fun addDogToUser_success_setsHasDogBeenAddedTrue() = runTest {
        val viewModel = createViewModel(makeFakeRepo(addDogResult = ResponseStatus.Success(Unit)))

        viewModel.handleAction(DogDetailUiAction.AddDogToUser)

        assertTrue(viewModel.uiState.value.hasDogBeenAdded)
        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.error)
    }

    // -------------------------------------------------------------------------
    // AddDogToUser — error
    // -------------------------------------------------------------------------

    @Test
    fun addDogToUser_error_setsErrorAndClearsLoading() = runTest {
        val errorMessageId = 99
        val viewModel = createViewModel(
            makeFakeRepo(addDogResult = ResponseStatus.Error(errorMessageId))
        )

        viewModel.handleAction(DogDetailUiAction.AddDogToUser)

        assertEquals("str_$errorMessageId", viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isLoading)
        assertFalse(viewModel.uiState.value.hasDogBeenAdded)
    }

    // -------------------------------------------------------------------------
    // Initial state
    // -------------------------------------------------------------------------

    @Test
    fun initialState_isLoadingFalseAndNullError() = runTest {
        val viewModel = createViewModel()

        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.hasDogBeenAdded)
        assertTrue(viewModel.uiState.value.probableDogs.isEmpty())
        assertEquals(testDog, viewModel.uiState.value.dog)
    }
}
