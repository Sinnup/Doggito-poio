package com.fruse.dogedex.dogList

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fruse.dogedex.api.responses.ApiResponseStatus
import com.fruse.dogedex.core.model.Dog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class DogListViewModel @Inject constructor(
    private val dogRepository: DogTasks
) : ViewModel() {

    var dogList = mutableStateOf<List<Dog>>(emptyList())
        private set

    var status = mutableStateOf<ApiResponseStatus<Any>?>(null)
        private set

    init {
        getDogCollection()
    }

//    private fun downloadUserDogs() {
//        viewModelScope.launch {
//            _status.value = ApiResponseStatus.Loading()
//            handleResponseStatus(dogRepository.getUserDogs())
//        }
//    }

    private fun getDogCollection() {
        viewModelScope.launch {
            status.value = ApiResponseStatus.Loading()
            handleResponseStatus(dogRepository.getDogCollection())
        }
    }

//    private fun downloadDogs() {
//        viewModelScope.launch {
//            _status.value = ApiResponseStatus.Loading()
//            handleResponseStatus(dogRepository.downloadDogs())
//        }
//    }

    @Suppress("UNCHECKED_CAST")
    private fun handleResponseStatus(apiResponseStatus: ApiResponseStatus<List<Dog>>) {
        if (apiResponseStatus is ApiResponseStatus.Success) {
            dogList.value = apiResponseStatus.data
        }
        status.value = apiResponseStatus as ApiResponseStatus<Any>
    }

    fun resetApiResponseStatus() {
        status.value = null
    }
}