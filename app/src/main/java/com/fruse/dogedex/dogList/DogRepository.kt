package com.fruse.dogedex.dogList

import com.fruse.dogedex.R
import com.fruse.dogedex.core.api.ApiService
import com.fruse.dogedex.core.api.dto.AddDogTOUserDTO
import com.fruse.dogedex.core.api.dto.DogDTOMapper
import com.fruse.dogedex.core.api.makeNetworkCall
import com.fruse.dogedex.api.responses.ApiResponseStatus
import com.fruse.dogedex.core.model.Dog
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface DogTasks {
    suspend fun getDogCollection(): ApiResponseStatus<List<Dog>>
    suspend fun addDogToUser(dogId: Long): ApiResponseStatus<Any>
    suspend fun getDogBYMlId(mlDogId: String): ApiResponseStatus<Dog>
    suspend fun getProbableDogs(probableDogsIds: List<String>): Flow<ApiResponseStatus<Dog>>
}

class DogRepository @Inject constructor(
    private val apiService: ApiService,
    private val dispatcher: CoroutineDispatcher
) : DogTasks {

    override suspend fun getDogCollection(): ApiResponseStatus<List<Dog>> {
        return withContext(dispatcher) {
            // Se crean 2 tareas para que se ejecuten de forma asíncrona, al final una vez
            // completadas todas, se procede a la siguiente línea de código
            val allDogsListResponseDeferred = async { downloadDogs() }
            val userDogsListResponseDeferred = async { getUserDogs() }

            val allDogsListResponse = allDogsListResponseDeferred.await()
            val userDogsListResponse = userDogsListResponseDeferred.await()

            if (allDogsListResponse is ApiResponseStatus.Error) {
                allDogsListResponse
            } else if (userDogsListResponse is ApiResponseStatus.Error) {
                userDogsListResponse
            } else if (allDogsListResponse is ApiResponseStatus.Success &&
                userDogsListResponse is ApiResponseStatus.Success
            ) {
                ApiResponseStatus.Success(
                    getCollectionList(
                        allDogsListResponse.data,
                        userDogsListResponse.data
                    )
                )
            } else {
                ApiResponseStatus.Error(R.string.unknown_error)
            }
        }
    }

    private fun getCollectionList(allDogList: List<Dog>, userDOgList: List<Dog>): List<Dog> {
        return allDogList.map {
            if (userDOgList.contains(it)) {
                it
            } else {
                Dog(
                    it.id, index = it.index, "", "", "", "",
                    "", "", "", "", "", false
                )
            }
        }.sorted()
    }

    private suspend fun downloadDogs(): ApiResponseStatus<List<Dog>> {
        return makeNetworkCall {
            val dogListApiResponse = apiService.getAllDogs()
            val dogDTOList = dogListApiResponse.data.dogs
            val dogDTOMapper = DogDTOMapper()
            dogDTOMapper.fromDogDTOListTODogDomainList(dogDTOList)
        }
    }

    override suspend fun addDogToUser(dogId: Long): ApiResponseStatus<Any> {
        return makeNetworkCall {
            val addDogTOUserDTO = AddDogTOUserDTO(dogId)
            val defaultResponse = apiService.addDogToUser(addDogTOUserDTO)

            if (!defaultResponse.isSuccess) {
                throw Exception(defaultResponse.message)
            }

            defaultResponse
        }
    }

    private suspend fun getUserDogs(): ApiResponseStatus<List<Dog>> {
        return makeNetworkCall {
            val dogListApiResponse = apiService.getUserDogs()
            val dogDTOList = dogListApiResponse.data.dogs
            val dogDTOMapper = DogDTOMapper()
            dogDTOMapper.fromDogDTOListTODogDomainList(dogDTOList)
        }
    }

    override suspend fun getDogBYMlId(mlDogId: String): ApiResponseStatus<Dog> {
        return makeNetworkCall {
            val response = apiService.getDogBYMlId(mlDogId)

            if (!response.isSuccess) {
                throw Exception(response.message)
            }

            val dogDTOMapper = DogDTOMapper()
            dogDTOMapper.fromDogDTOToDogDomain(response.data.dog)
        }
    }

    override suspend fun getProbableDogs(probableDogsIds: List<String>): Flow<ApiResponseStatus<Dog>> =
        flow {

            for (mlDogId in probableDogsIds) {
                val dog = getDogBYMlId(mlDogId)
                emit(dog)
            }
        }.flowOn(dispatcher)
}