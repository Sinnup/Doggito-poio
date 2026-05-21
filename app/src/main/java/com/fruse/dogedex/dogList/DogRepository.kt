package com.fruse.dogedex.dogList

import android.content.Context
import com.fruse.dogedex.R
import com.fruse.dogedex.core.api.ApiService
import com.fruse.dogedex.core.api.dto.AddDogTOUserDTO
import com.fruse.dogedex.core.api.dto.DogDTOMapper
import com.fruse.dogedex.core.api.makeNetworkCall
import com.fruse.dogedex.core.api.responses.ResponseStatus
import com.fruse.dogedex.core.database.DogedexDatabase
import com.fruse.dogedex.core.database.dao.DogEntityMapper
import com.fruse.dogedex.core.model.Dog
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface DogTasks {
    suspend fun getDogCollection(): ResponseStatus<List<Dog>>
    suspend fun addDogToUser(dogId: Long): ResponseStatus<Any>
    suspend fun getDogBYMlId(mlDogId: String): ResponseStatus<Dog>
    suspend fun getProbableDogs(probableDogsIds: List<String>): Flow<ResponseStatus<Dog>>

    suspend fun insertAllDogs(dogs: List<Dog>)
    suspend fun getDogCollectionDB(): ResponseStatus<List<Dog>>
    suspend fun addDogToUserDB(dogId: Long): ResponseStatus<Any>
    suspend fun getDogBYMlIdDB(mlDogId: String): ResponseStatus<Dog>
    suspend fun getProbableDogsDB(probableDogsIds: List<String>): Flow<ResponseStatus<Dog>>
}

class DogRepository @Inject constructor(
    private val apiService: ApiService,
    private val dispatcher: CoroutineDispatcher,
    private val database: DogedexDatabase
) : DogTasks {

    override suspend fun getDogCollection(): ResponseStatus<List<Dog>> {
        return withContext(dispatcher) {
            // Se crean 2 tareas para que se ejecuten de forma asíncrona, al final una vez
            // completadas todas, se procede a la siguiente línea de código
            val allDogsListResponseDeferred = async { downloadDogs() }
            val userDogsListResponseDeferred = async { getUserDogs() }

            val allDogsListResponse = allDogsListResponseDeferred.await()
            val userDogsListResponse = userDogsListResponseDeferred.await()

            if (allDogsListResponse is ResponseStatus.Error) {
                allDogsListResponse
            } else if (userDogsListResponse is ResponseStatus.Error) {
                userDogsListResponse
            } else if (allDogsListResponse is ResponseStatus.Success &&
                userDogsListResponse is ResponseStatus.Success
            ) {
                ResponseStatus.Success(
                    getCollectionList(
                        allDogsListResponse.data,
                        userDogsListResponse.data
                    )
                )
            } else {
                ResponseStatus.Error(R.string.unknown_error)
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

    private suspend fun downloadDogs(): ResponseStatus<List<Dog>> {
        return makeNetworkCall {
            val dogListApiResponse = apiService.getAllDogs()
            val dogDTOList = dogListApiResponse.data.dogs
            val dogDTOMapper = DogDTOMapper()
            dogDTOMapper.fromDogDTOListTODogDomainList(dogDTOList)
        }
    }

    private suspend fun downloadDogsDB(): ResponseStatus<List<Dog>> {
        val dogEntityList = database.dogDao().getAllDogs()
        val dogEntityMapper = DogEntityMapper()
        return ResponseStatus.Success(
            dogEntityMapper.fromDogEntityListTODogDomainList(dogEntityList)
        )
    }

    override suspend fun addDogToUser(dogId: Long): ResponseStatus<Any> {
        return makeNetworkCall {
            val addDogTOUserDTO = AddDogTOUserDTO(dogId)
            val defaultResponse = apiService.addDogToUser(addDogTOUserDTO)

            if (!defaultResponse.isSuccess) {
                throw Exception(defaultResponse.message)
            }

            defaultResponse
        }
    }

    private suspend fun getUserDogs(): ResponseStatus<List<Dog>> {
        return makeNetworkCall {
            val dogListApiResponse = apiService.getUserDogs()
            val dogDTOList = dogListApiResponse.data.dogs
            val dogDTOMapper = DogDTOMapper()
            dogDTOMapper.fromDogDTOListTODogDomainList(dogDTOList)
        }
    }

    private suspend fun getUserDogsDB(): ResponseStatus<List<Dog>> {
        val dogEntityList = database.dogDao().getUserDogs()
        val dogEntityMapper = DogEntityMapper()

        return ResponseStatus.Success(
            dogEntityMapper.fromDogEntityListTODogDomainList(dogEntityList)
        )
    }

    override suspend fun getDogBYMlId(mlDogId: String): ResponseStatus<Dog> {
        return makeNetworkCall {
            val response = apiService.getDogByMlId(mlDogId)

            if (!response.isSuccess) {
                throw Exception(response.message)
            }

            val dogDTOMapper = DogDTOMapper()
            dogDTOMapper.fromDogDTOToDogDomain(response.data.dog)
        }
    }

    override suspend fun getProbableDogs(probableDogsIds: List<String>): Flow<ResponseStatus<Dog>> =
        flow {

            for (mlDogId in probableDogsIds) {
                val dog = getDogBYMlId(mlDogId)
                emit(dog)
            }
        }.flowOn(dispatcher)

    override suspend fun getDogCollectionDB(): ResponseStatus<List<Dog>> {
        return withContext(dispatcher) {
            // Se crean 2 tareas para que se ejecuten de forma asíncrona, al final una vez
            // completadas todas, se procede a la siguiente línea de código
            val allDogsListResponseDeferred = async { downloadDogsDB() }
            val userDogsListResponseDeferred = async { getUserDogsDB() }

            val allDogsListResponse = allDogsListResponseDeferred.await()
            val userDogsListResponse = userDogsListResponseDeferred.await()

            if (allDogsListResponse is ResponseStatus.Error) {
                allDogsListResponse
            } else if (userDogsListResponse is ResponseStatus.Error) {
                userDogsListResponse
            } else if (allDogsListResponse is ResponseStatus.Success &&
                userDogsListResponse is ResponseStatus.Success
            ) {
                ResponseStatus.Success(
                    getCollectionList(
                        allDogsListResponse.data,
                        userDogsListResponse.data
                    )
                )
            } else {
                ResponseStatus.Error(R.string.unknown_error)
            }
        }
    }

    override suspend fun addDogToUserDB(dogId: Long): ResponseStatus<Any> {
        return withContext(dispatcher) {
            try {
                database.dogDao().addDogToUser(dogId)
                ResponseStatus.Success(true)
            } catch (e: Exception) {
                throw Exception(e.message)
            }
        }
    }

    override suspend fun getDogBYMlIdDB(mlDogId: String): ResponseStatus<Dog> {
        return withContext(dispatcher) {
            val dogEntity = database.dogDao().getDogByMLId(mlDogId)

            val dogEntityMapper = DogEntityMapper()
            ResponseStatus.Success(dogEntityMapper.fromDogEntityToDogDomain(dogEntity))
        }
    }

    override suspend fun getProbableDogsDB(probableDogsIds: List<String>): Flow<ResponseStatus<Dog>> =
        flow {

            for (mlDogId in probableDogsIds) {
                val dog = getDogBYMlIdDB(mlDogId)
                emit(dog)
            }
        }.flowOn(dispatcher)

    override suspend fun insertAllDogs(dogs: List<Dog>) {
        withContext(dispatcher) {
            val dogsList = DogEntityMapper().fromDogDomainListTODogEntityList(dogs)
            database.dogDao().insertDogs(dogsList)
        }
    }
}