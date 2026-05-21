package com.fruse.dogedex.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.fruse.dogedex.core.database.dao.Dog
import com.fruse.dogedex.core.database.dao.DogDao

@Database(entities = [Dog::class], version = 1)
abstract class DogedexDatabase: RoomDatabase() {

    abstract fun dogDao(): DogDao
}