package com.fruse.dogedex.main

import com.fruse.dogedex.dogList.ImageRepository
import android.content.Context
import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fruse.dogedex.camera.machinelearning.ClassifierTasks
import com.fruse.dogedex.camera.machinelearning.DogRecognition
import com.fruse.dogedex.core.api.responses.ResponseStatus
import com.fruse.dogedex.core.di.StringResolver
import com.fruse.dogedex.core.model.Dog
import com.fruse.dogedex.core.session.SessionManager
import com.fruse.dogedex.dogList.DogTasks
import com.fruse.dogedex.main.MainActivity.Companion.DOGS_JSON_FILE
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject

data class MainUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val dogRecognition: DogRecognition? = null,
    val probableDogIds: List<String> = emptyList()
)

sealed class MainUiAction {
    data class RecognizeImage(val imageProxy: ImageProxy) : MainUiAction()
    data class GetDogByMlId(val mlId: String) : MainUiAction()
    data object DismissError : MainUiAction()
    data object NavigateToDogList : MainUiAction()
    data object NavigateToSettings : MainUiAction()
}

sealed class MainUiEffect {
    data class NavigateToDogDetail(val dog: Dog, val probableDogIds: List<String>) : MainUiEffect()
    data object NavigateToDogList : MainUiEffect()
    data object NavigateToSettings : MainUiEffect()
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val dogRepository: DogTasks,
    private val classifierRepository: ClassifierTasks,
    val sessionRepository: SessionManager,
    private val strings: StringResolver,
    private val dispatcher: CoroutineDispatcher,
    private val imageRepository: ImageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<MainUiEffect>(Channel.BUFFERED)
    val uiEffect: Flow<MainUiEffect> = _uiEffect.receiveAsFlow()

    fun handleAction(action: MainUiAction) {
        when (action) {
            is MainUiAction.RecognizeImage -> recognizeImage(action.imageProxy)
            is MainUiAction.GetDogByMlId -> getDogByMlId(action.mlId)
            is MainUiAction.DismissError -> _uiState.update { it.copy(error = null) }
            is MainUiAction.NavigateToDogList -> viewModelScope.launch {
                _uiEffect.send(MainUiEffect.NavigateToDogList)
            }

            is MainUiAction.NavigateToSettings -> viewModelScope.launch {
                _uiEffect.send(MainUiEffect.NavigateToSettings)
            }
        }
    }

    private fun recognizeImage(imageProxy: ImageProxy) {
        viewModelScope.launch {
            val dogRecognitionList = classifierRepository.recognizeImage(imageProxy)
            updateDogRecognition(dogRecognitionList)
            updateProbableDogIds(dogRecognitionList)
            imageProxy.close()
        }
    }

    private fun updateProbableDogIds(dogRecognitionList: List<DogRecognition>) {
        val ids = if (dogRecognitionList.size >= 5) {
            dogRecognitionList.subList(1, 4).map { it.id }
        } else {
            emptyList()
        }
        _uiState.update { it.copy(probableDogIds = ids) }
    }

    private fun updateDogRecognition(dogRecognitionList: List<DogRecognition>) {
        _uiState.update { it.copy(dogRecognition = dogRecognitionList.firstOrNull()) }
    }

    private fun getDogByMlId(mlDogId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
//            when (val result = dogRepository.getDogBYMlId(mlDogId)) {
            when (val result = dogRepository.getDogBYMlIdDB(mlDogId)) {
                is ResponseStatus.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _uiEffect.send(
                        MainUiEffect.NavigateToDogDetail(result.data, _uiState.value.probableDogIds)
                    )
                }

                is ResponseStatus.Error ->
                    _uiState.update {
                        it.copy(isLoading = false, error = strings.resolve(result.messageId))
                    }

                is ResponseStatus.Loading ->
                    _uiState.update { it.copy(isLoading = true) }
            }
        }
    }

    fun setUpAssets(context: Context) {
        viewModelScope.launch(dispatcher) {

            val jsonDogList = context.assets.open(DOGS_JSON_FILE).bufferedReader().use {
                it.readText()
            }

            val dogsList = Json.decodeFromString<List<Dog>>(jsonDogList)
            dogRepository.insertAllDogs(dogsList)


        }
        copyImagesToLocalStorage()
    }

    private val _copyStatus = MutableStateFlow("Copying...")
    val copyStatus: StateFlow<String> = _copyStatus.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun copyImagesToLocalStorage() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val path = imageRepository.copyImagesToLocalStorage()
                _copyStatus.value = "Images copied successfully to: $path"
            } catch (e: Exception) {
                _copyStatus.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
