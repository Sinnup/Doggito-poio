package com.fruse.dogedex.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.fruse.dogedex.core.database.entities.DogEntity

@Dao
interface DogDao {

    @Insert
    suspend fun insertDogs(dogs: List<DogEntity>)

    @Query("SELECT * FROM dog")
    suspend fun getAllDogs(): List<DogEntity>

    @Query("UPDATE dog SET inCollection = 1 WHERE id = :dogId")
    suspend fun addDogToUser(dogId: Long)

    @Query("SELECT * FROM dog WHERE inCollection = 1")
    suspend fun getUserDogs(): List<DogEntity>

    @Query("SELECT * FROM dog WHERE id = :mlDogId ")
    suspend fun getDogByMLId(mlDogId: String): DogEntity
}