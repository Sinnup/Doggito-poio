package com.fruse.dogedex.dogList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fruse.dogedex.api.responses.ApiResponseStatus
import com.fruse.dogedex.core.di.StringResolver
import com.fruse.dogedex.core.model.Dog
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

data class DogListUiState(
    val dogs: List<Dog> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class DogListUiAction {
    data object LoadDogs : DogListUiAction()
    data class OnDogClicked(val dog: Dog) : DogListUiAction()
    data object DismissError : DogListUiAction()
}

sealed class DogListUiEffect {
    data class NavigateToDogDetail(val dog: Dog) : DogListUiEffect()
}

@HiltViewModel
class DogListViewModel @Inject constructor(
    private val dogRepository: DogTasks,
    private val strings: StringResolver
) : ViewModel() {

    private val _uiState = MutableStateFlow(DogListUiState())
    val uiState: StateFlow<DogListUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<DogListUiEffect>(Channel.BUFFERED)
    val uiEffect: Flow<DogListUiEffect> = _uiEffect.receiveAsFlow()

    init {
        handleAction(DogListUiAction.LoadDogs)
    }

    fun handleAction(action: DogListUiAction) {
        when (action) {
            is DogListUiAction.LoadDogs -> getDogCollection()
            is DogListUiAction.OnDogClicked -> viewModelScope.launch {
                _uiEffect.send(DogListUiEffect.NavigateToDogDetail(action.dog))
            }
            is DogListUiAction.DismissError -> _uiState.update { it.copy(error = null) }
        }
    }

    private fun getDogCollection() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = dogRepository.getDogCollection()) {
                is ApiResponseStatus.Success ->
                    _uiState.update { it.copy(isLoading = false, dogs = result.data) }
                is ApiResponseStatus.Error ->
                    _uiState.update { it.copy(isLoading = false, error = strings.resolve(result.messageId)) }
                is ApiResponseStatus.Loading ->
                    _uiState.update { it.copy(isLoading = true) }
            }
        }
    }
}
