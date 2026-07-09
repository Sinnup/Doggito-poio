package com.espert.dogedex.viewmodel

import androidx.camera.core.ImageProxy
import app.cash.turbine.test
import com.espert.dogedex.camera.machinelearning.ClassifierTasks
import com.espert.dogedex.camera.machinelearning.DogRecognition
import com.espert.dogedex.core.di.StringResolver
import com.espert.dogedex.core.model.Dog
import com.espert.dogedex.core.model.ResponseStatus
import com.espert.dogedex.core.preferences.UserPreferencesRepository
import com.espert.dogedex.dogList.DogTasks
import com.espert.dogedex.dogList.ImageRepository
import com.espert.dogedex.main.MainUiAction
import com.espert.dogedex.main.MainUiEffect
import com.espert.dogedex.main.MainViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock

class MainViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val testDispatcher = UnconfinedTestDispatcher()
    private val strings = StringResolver { id -> "str_$id" }

    open class FakeDogRepository : DogTasks {
        override suspend fun getDogCollection(): ResponseStatus<List<Dog>> = ResponseStatus.Success(emptyList())
        override suspend fun addDogToUser(dogId: Long): ResponseStatus<Any> = ResponseStatus.Success(Unit)
        override suspend fun getDogByMlId(mlDogId: String): ResponseStatus<Dog> =
            ResponseStatus.Success(Dog(1L, 1, "Name", "", "", "", "", "", "", "", "", true))
        override suspend fun getProbableDogs(probableDogsIds: List<String>): Flow<ResponseStatus<Dog>> =
            throw AssertionError("unexpected call")
        override suspend fun insertAllDogs(dogs: List<Dog>) {}
    }

    open class FakeClassifierRepository : ClassifierTasks {
        override suspend fun recognizeImage(imageProxy: ImageProxy): List<DogRecognition> {
            return listOf(DogRecognition("1", 80f))
        }
    }

    class FakeImageRepository : ImageRepository {
        override suspend fun copyImagesToLocalStorage(): String = "path"
        override fun getLocalImagePath(imageName: String): String = "path/$imageName"
    }

    class FakeUserPreferencesRepository(
        initialHasSeenOnboarding: Boolean = true
    ) : UserPreferencesRepository {
        private val _hasSeenOnboarding = MutableStateFlow(initialHasSeenOnboarding)
        override val hasSeenOnboarding = _hasSeenOnboarding.asStateFlow()
        override suspend fun setOnboardingSeen() {
            _hasSeenOnboarding.value = true
        }
    }

    private fun createViewModel(
        dogRepo: DogTasks = FakeDogRepository(),
        classifierRepo: ClassifierTasks = FakeClassifierRepository(),
        imageRepo: ImageRepository = FakeImageRepository(),
        userPrefsRepo: UserPreferencesRepository = FakeUserPreferencesRepository()
    ): MainViewModel {
        return MainViewModel(
            dogRepository = dogRepo,
            classifierRepository = classifierRepo,
            strings = strings,
            dispatcher = testDispatcher,
            imageRepository = imageRepo,
            userPreferencesRepository = userPrefsRepo
        )
    }

    @Test
    fun `initial state is correct`() = runTest {
        val viewModel = createViewModel()
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertNull(state.dogRecognition)
        assertTrue(state.probableDogIds.isEmpty())
    }

    @Test
    fun `dismissError clears error`() = runTest {
        val failingRepo = object : FakeDogRepository() {
            override suspend fun getDogByMlId(mlDogId: String): ResponseStatus<Dog> =
                ResponseStatus.Error(404)
        }
        val viewModelWithError = createViewModel(dogRepo = failingRepo)

        viewModelWithError.handleAction(MainUiAction.GetDogByMlId("1"))
        assertEquals("str_404", viewModelWithError.uiState.value.error)

        viewModelWithError.handleAction(MainUiAction.DismissError)
        assertNull(viewModelWithError.uiState.value.error)
    }

    @Test
    fun `navigateToDogList emits correct effect`() = runTest {
        val viewModel = createViewModel()
        viewModel.uiEffect.test {
            viewModel.handleAction(MainUiAction.NavigateToDogList)
            assertEquals(MainUiEffect.NavigateToDogList, awaitItem())
        }
    }

    @Test
    fun `navigateToSettings emits correct effect`() = runTest {
        val viewModel = createViewModel()
        viewModel.uiEffect.test {
            viewModel.handleAction(MainUiAction.NavigateToSettings)
            assertEquals(MainUiEffect.NavigateToSettings, awaitItem())
        }
    }

    @Test
    fun `recognizeImage updates state with recognition and probable dogs`() = runTest {
        val classifierRepo = object : FakeClassifierRepository() {
            override suspend fun recognizeImage(imageProxy: ImageProxy): List<DogRecognition> {
                return listOf(
                    DogRecognition("1", 90f),
                    DogRecognition("2", 80f),
                    DogRecognition("3", 70f),
                    DogRecognition("4", 60f),
                    DogRecognition("5", 50f)
                )
            }
        }
        val viewModel = createViewModel(classifierRepo = classifierRepo)
        val mockProxy = mock(ImageProxy::class.java)

        viewModel.handleAction(MainUiAction.RecognizeImage(mockProxy))

        val state = viewModel.uiState.value
        assertEquals("1", state.dogRecognition?.id)
        assertEquals(3, state.probableDogIds.size)
        assertEquals(listOf("2", "3", "4"), state.probableDogIds)
    }

    @Test
    fun `getDogByMlId success emits NavigateToDogDetail effect`() = runTest {
        val viewModel = createViewModel()
        viewModel.uiEffect.test {
            viewModel.handleAction(MainUiAction.GetDogByMlId("1"))
            val effect = awaitItem()
            assertTrue(effect is MainUiEffect.NavigateToDogDetail)
            val detailEffect = effect as MainUiEffect.NavigateToDogDetail
            assertEquals("Name", detailEffect.dog.name)
        }
    }

    @Test
    fun `hasSeenOnboarding reflects the persisted preference`() = runTest {
        val prefsRepo = FakeUserPreferencesRepository(initialHasSeenOnboarding = false)
        val viewModel = createViewModel(userPrefsRepo = prefsRepo)

        viewModel.hasSeenOnboarding.test {
            assertEquals(false, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onOnboardingCompleted persists that onboarding was seen`() = runTest {
        val prefsRepo = FakeUserPreferencesRepository(initialHasSeenOnboarding = false)
        val viewModel = createViewModel(userPrefsRepo = prefsRepo)

        viewModel.hasSeenOnboarding.test {
            assertEquals(false, awaitItem())
            viewModel.onOnboardingCompleted()
            assertEquals(true, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
