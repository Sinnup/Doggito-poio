package com.fruse.dogedex.dogList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fruse.dogedex.api.responses.ApiResponseStatus
import com.fruse.dogedex.core.model.Dog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DogListUiState(
    val dogs: List<Dog> = emptyList(),
    val isLoading: Boolean = false,
    val error: Int? = null
)

@HiltViewModel
class DogListViewModel @Inject constructor(
    private val dogRepository: DogTasks
) : ViewModel() {

    private val _uiState = MutableStateFlow(DogListUiState())
    val uiState: StateFlow<DogListUiState> = _uiState.asStateFlow()

    init {
        getDogCollection()
    }

    private fun getDogCollection() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = dogRepository.getDogCollection()) {
                is ApiResponseStatus.Success ->
                    _uiState.update { it.copy(isLoading = false, dogs = result.data) }
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
}
