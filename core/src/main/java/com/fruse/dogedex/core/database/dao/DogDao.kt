package com.fruse.dogedex.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fruse.dogedex.core.database.entities.DogEntity

@Dao
interface DogDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDogs(dogs: List<DogEntity>)

    @Query("SELECT * FROM dog ORDER BY `index`")
    suspend fun getAllDogs(): List<DogEntity>

    @Query("UPDATE dog SET inCollection = 1 WHERE id = :dogId")
    suspend fun addDogToUser(dogId: Long)

    @Query("SELECT * FROM dog WHERE inCollection = 1")
    suspend fun getUserDogs(): List<DogEntity>

    @Query("SELECT * FROM dog WHERE imageUrl = :mlDogId")
    suspend fun getDogByMLId(mlDogId: String): DogEntity?
}