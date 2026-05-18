package com.fruse.dogedex.dogDetail

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.fruse.dogedex.api.responses.ApiResponseStatus
import com.fruse.dogedex.core.model.Dog
import com.fruse.dogedex.core.navigation.DogDetailKey
import com.fruse.dogedex.dogList.DogTasks
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DogDetailViewModel @Inject constructor(
    private val dogRepository: DogTasks,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val dogDetailKey: DogDetailKey? = runCatching {
        savedStateHandle.toRoute<DogDetailKey>()
    }.getOrNull()

    var dog = mutableStateOf(
        dogDetailKey?.dog
            ?: savedStateHandle.get<Dog>("dog")
    )
        private set

    private var probableDogIds = mutableStateOf(
        dogDetailKey?.probableDogIds
            ?: savedStateHandle.get<List<String>>("most_probable_dog_ids")
            ?: listOf()
    )

    var isRecognition = mutableStateOf(
        dogDetailKey?.isRecognition
            ?: savedStateHandle.get<Boolean>("is_recognition")
    )
        private set

    var status = mutableStateOf<ApiResponseStatus<Any>?>(null)
        private set

    private var _probableDogList =
        MutableStateFlow<MutableList<Dog>>(mutableListOf())
    val probableDogList: StateFlow<MutableList<Dog>>
        get() = _probableDogList

    fun getProbableDogs() {
        _probableDogList.value.clear()
        viewModelScope.launch {
            dogRepository
                .getProbableDogs(probableDogIds.value)
                .collect {
                    if (it is ApiResponseStatus.Success) {
                        val probableDogMutableList = _probableDogList.value.toMutableList()
                        probableDogMutableList.add(it.data)
                        _probableDogList.value = probableDogMutableList
                    }
                }
        }
    }

    fun updateDog(newDog: Dog) {
        dog.value = newDog
    }

    fun addDogToUser() {
        viewModelScope.launch {
            status.value = ApiResponseStatus.Loading()
            handleAddDogToUserResponseStatus(dogRepository.addDogToUser(dog.value?.id ?: 0))
        }
    }

    private fun handleAddDogToUserResponseStatus(apiResponseStatus: ApiResponseStatus<Any>) {
        status.value = apiResponseStatus
    }

    fun resetApiResponseStatus() {
        status.value = null
    }
}
