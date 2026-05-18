package com.fruse.dogedex.main

import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fruse.dogedex.api.responses.ApiResponseStatus
import com.fruse.dogedex.core.model.Dog
import com.fruse.dogedex.core.session.SessionManager
import com.fruse.dogedex.core.session.SessionRepository
import com.fruse.dogedex.dogList.DogTasks
import com.fruse.dogedex.camera.machinelearning.ClassifierTasks
import com.fruse.dogedex.camera.machinelearning.DogRecognition
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MainUiState(
    val isLoading: Boolean = false,
    val recognizedDog: Dog? = null,
    val error: Int? = null,
    val requiresLogin: Boolean = false,
    val dogRecognition: DogRecognition? = null,
    val probableDogIds: List<String> = emptyList()
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val dogRepository: DogTasks,
    private val classifierRepository: ClassifierTasks,
    val sessionRepository: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    fun recognizeImage(imageProxy: ImageProxy) {
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

    fun getDogBYMlId(mlDogID: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = dogRepository.getDogBYMlId(mlDogID)) {
                is ApiResponseStatus.Success ->
                    _uiState.update { it.copy(isLoading = false, recognizedDog = result.data) }
                is ApiResponseStatus.Error ->
                    _uiState.update { it.copy(isLoading = false, error = result.messageId) }
                is ApiResponseStatus.Loading ->
                    _uiState.update { it.copy(isLoading = true) }
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }


    fun onDogDetailNavigated() {
        _uiState.update { it.copy(recognizedDog = null) }
    }
}
