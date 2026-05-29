package com.espert.dogedex.dogList

import com.espert.dogedex.R
import com.espert.dogedex.core.database.DogedexDatabase
import com.espert.dogedex.core.database.dao.DogEntityMapper
import com.espert.dogedex.core.model.Dog
import com.espert.dogedex.core.model.ResponseStatus
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface DogTasks {
    suspend fun getDogCollection(): ResponseStatus<List<Dog>>
    suspend fun addDogToUser(dogId: Long): ResponseStatus<Any>
    suspend fun getDogByMlId(mlDogId: String): ResponseStatus<Dog>
    suspend fun getProbableDogs(probableDogsIds: List<String>): Flow<ResponseStatus<Dog>>
    suspend fun insertAllDogs(dogs: List<Dog>)
}

class DogRepository @Inject constructor(
    private val dispatcher: CoroutineDispatcher,
    private val database: DogedexDatabase
) : DogTasks {

    override suspend fun getDogCollection(): ResponseStatus<List<Dog>> {
        return downloadDogs()
    }

    private suspend fun downloadDogs(): ResponseStatus<List<Dog>> {
        val dogEntityList = database.dogDao().getAllDogs()
        val dogEntityMapper = DogEntityMapper()
        return ResponseStatus.Success(
            dogEntityMapper.fromDogEntityListTODogDomainList(dogEntityList)
        )
    }

    override suspend fun addDogToUser(dogId: Long): ResponseStatus<Any> {
        return withContext(dispatcher) {
            try {
                database.dogDao().addDogToUser(dogId)
                ResponseStatus.Success(true)
            } catch (e: Exception) {
                ResponseStatus.Error(R.string.unknown_error)
            }
        }
    }

    override suspend fun getDogByMlId(mlDogId: String): ResponseStatus<Dog> {
        return withContext(dispatcher) {
            val dogEntity = database.dogDao().getDogByMLId(mlDogId)
                ?: return@withContext ResponseStatus.Error(R.string.unknown_error)
            ResponseStatus.Success(DogEntityMapper().fromDogEntityToDogDomain(dogEntity).copy(inCollection = false))
        }
    }

    override suspend fun getProbableDogs(probableDogsIds: List<String>): Flow<ResponseStatus<Dog>> =
        flow {
            for (mlDogId in probableDogsIds) {
                val dog = getDogByMlId(mlDogId)
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
