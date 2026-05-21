package com.fruse.dogedex.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.fruse.dogedex.core.database.entities.DogEntity
import com.fruse.dogedex.core.database.dao.DogDao

@Database(entities = [DogEntity::class], version = 1, exportSchema = false)
abstract class DogedexDatabase: RoomDatabase() {

    abstract fun dogDao(): DogDao
}