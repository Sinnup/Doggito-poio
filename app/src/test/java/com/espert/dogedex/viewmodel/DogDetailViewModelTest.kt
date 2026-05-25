package com.espert.dogedex.viewmodel

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.espert.dogedex.core.model.ResponseStatus
import com.espert.dogedex.core.di.StringResolver
import com.espert.dogedex.core.model.Dog
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

/**
 * Unit tests for [DogDetailViewModel] action handlers.
 *
 * Note: [DogDetailViewModel] reads its initial state from [SavedStateHandle] via
 * `savedStateHandle.toRoute<DogDetailKey>(typeMap = ...)`. That code path calls
 * `android.net.Uri.decode` (an Android API) and `Bundle.getParcelable` (requires
 * Android runtime), so the ViewModel always starts with `dog = null` in these JVM
 * unit tests — there is no Robolectric configured in this project.
 *
 * All tests that need a non-null dog first dispatch [DogDetailUiAction.UpdateDog] to
 * inject the dog into the state before exercising the action under test. This is the
 * same code path used by the Compose screen when the DogDetailKey is rebuilt from
 * Navigation arguments.
 */
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

        override suspend fun addDogToUser(dogId: Long): ResponseStatus<Any> =
            throw AssertionError("unexpected call to addDogToUser")

        override suspend fun getDogBYMlId(mlDogId: String): ResponseStatus<Dog> =
            throw AssertionError("unexpected call to getDogBYMlId")

        override suspend fun getProbableDogs(probableDogsIds: List<String>): Flow<ResponseStatus<Dog>> =
            throw AssertionError("unexpected call to getProbableDogs")

        override suspend fun insertAllDogs(dogs: List<Dog>) =
            throw AssertionError("unexpected call to insertAllDogs")

        override suspend fun getDogCollectionDB(): ResponseStatus<List<Dog>> =
            throw AssertionError("unexpected call to getDogCollectionDB")

        override suspend fun addDogToUserDB(dogId: Long): ResponseStatus<Any> = addDogResult

        override suspend fun getDogBYMlIdDB(mlDogId: String): ResponseStatus<Dog> =
            throw AssertionError("unexpected call to getDogBYMlIdDB")

        override suspend fun getProbableDogsDB(probableDogsIds: List<String>): Flow<ResponseStatus<Dog>> =
            probableDogsFlow
    }

    /**
     * Creates a [DogDetailViewModel] with a minimal [SavedStateHandle].
     * The `dog`, `probableDogIds`, and `isRecognition` keys match the
     * [com.espert.dogedex.core.navigation.DogDetailKey] field names that Navigation
     * would populate at runtime. On the JVM without Robolectric the `dog` field
     * deserialization silently falls back to null due to missing Android API stubs,
     * so the initial `uiState.dog` will be null.
     */
    private fun createViewModel(repo: DogTasks = makeFakeRepo()): DogDetailViewModel {
        val handle = SavedStateHandle(
            mapOf(
                "probableDogIds" to emptyList<String>(),
                "isRecognition" to false
            )
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

        // Put the dog into state so addDogToUser has a non-trivial subject
        viewModel.handleAction(DogDetailUiAction.UpdateDog(testDog))
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

        viewModel.handleAction(DogDetailUiAction.UpdateDog(testDog))

        assertEquals(testDog, viewModel.uiState.value.dog)
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

        viewModel.handleAction(DogDetailUiAction.UpdateDog(testDog))
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

        viewModel.handleAction(DogDetailUiAction.UpdateDog(testDog))
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
    }
}
