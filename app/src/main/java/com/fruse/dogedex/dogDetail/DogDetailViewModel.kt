package com.fruse.dogedex.dogDetail

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fruse.dogedex.api.responses.ApiResponseStatus
import com.fruse.dogedex.core.model.Dog
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

    var dog = mutableStateOf(
        savedStateHandle.get<Dog>(DogDetailComposeActivity.DOG_KEY)
    )
        private set

    private var probableDogIds = mutableStateOf(
        savedStateHandle.get<List<String>>(DogDetailComposeActivity.MOST_PROBABLE_DOG_IDS)
            ?: listOf()
    )

    var isRecognition = mutableStateOf(
        savedStateHandle.get<Boolean>(DogDetailComposeActivity.IS_RECOGNITION_KEY)
    )
        private set

    var status = mutableStateOf<ApiResponseStatus<Any>?>(null)
        private set

//    val status: LiveData<ApiResponseStatus<Any>>
//        get() = _status

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
//        status.value = ApiResponseStatus.Error(R.string.password_must_not_be_emtpy)

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