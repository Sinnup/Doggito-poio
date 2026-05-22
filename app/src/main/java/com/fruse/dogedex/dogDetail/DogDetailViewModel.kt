package com.fruse.dogedex.dogDetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.fruse.dogedex.core.api.responses.ResponseStatus
import com.fruse.dogedex.core.di.StringResolver
import com.fruse.dogedex.core.model.Dog
import com.fruse.dogedex.core.navigation.DogDetailKey
import com.fruse.dogedex.core.navigation.DogType
import com.fruse.dogedex.dogList.DogTasks
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DogDetailUiState(
    val dog: Dog? = null,
    val isRecognition: Boolean = false,
    val isLoading: Boolean = false,
    val hasDogBeenAdded: Boolean = false,
    val probableDogs: List<Dog> = emptyList(),
    val error: String? = null
)

sealed class DogDetailUiAction {
    data object AddDogToUser : DogDetailUiAction()
    data class UpdateDog(val dog: Dog) : DogDetailUiAction()
    data object LoadProbableDogs : DogDetailUiAction()
    data object DismissError : DogDetailUiAction()
    data object NavigateBack : DogDetailUiAction()
}

sealed class DogDetailUiEffect {
    data object DogAdded : DogDetailUiEffect()
    data object NavigateBack : DogDetailUiEffect()
}

@HiltViewModel
class DogDetailViewModel @Inject constructor(
    private val dogRepository: DogTasks,
    private val strings: StringResolver,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val dogDetailKey: DogDetailKey = savedStateHandle.toRoute(
        typeMap = mapOf(kotlin.reflect.typeOf<Dog>() to DogType)
    )

    private val initialDog = dogDetailKey.dog
    private val probableDogIds = dogDetailKey.probableDogIds
    private val initialIsRecognition = dogDetailKey.isRecognition

    private val _uiState = MutableStateFlow(
        DogDetailUiState(
            dog = initialDog,
            isRecognition = initialIsRecognition
        )
    )
    val uiState: StateFlow<DogDetailUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<DogDetailUiEffect>(Channel.BUFFERED)
    val uiEffect: Flow<DogDetailUiEffect> = _uiEffect.receiveAsFlow()

    fun handleAction(action: DogDetailUiAction) {
        when (action) {
            is DogDetailUiAction.AddDogToUser -> addDogToUser()
            is DogDetailUiAction.UpdateDog -> _uiState.update { it.copy(dog = action.dog) }
            is DogDetailUiAction.LoadProbableDogs -> loadProbableDogs()
            is DogDetailUiAction.DismissError -> _uiState.update { it.copy(error = null) }
            is DogDetailUiAction.NavigateBack -> viewModelScope.launch {
                _uiEffect.send(DogDetailUiEffect.NavigateBack)
            }
        }
    }

    private fun addDogToUser() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
//            when (val result = dogRepository.addDogToUser(_uiState.value.dog?.id ?: 0)) {
            when (val result = dogRepository.addDogToUserDB(_uiState.value.dog?.id ?: 0)) {
                is ResponseStatus.Success ->
                    _uiState.update { it.copy(isLoading = false, hasDogBeenAdded = true) }

                is ResponseStatus.Error ->
                    _uiState.update {
                        it.copy(isLoading = false, error = strings.resolve(result.messageId))
                    }

                is ResponseStatus.Loading ->
                    _uiState.update { it.copy(isLoading = true) }
            }
        }
    }

    private fun loadProbableDogs() {
        viewModelScope.launch {
            _uiState.update { it.copy(probableDogs = emptyList()) }
//            dogRepository.getProbableDogs(probableDogIds).collect { status ->
            dogRepository.getProbableDogsDB(probableDogIds).collect { status ->
                if (status is ResponseStatus.Success) {
                    _uiState.update { it.copy(probableDogs = it.probableDogs + status.data) }
                }
            }
        }
    }
}
