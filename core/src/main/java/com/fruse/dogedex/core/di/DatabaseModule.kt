package com.fruse.dogedex.core.di

import android.content.Context
import androidx.room.Room
import com.fruse.dogedex.core.database.DogedexDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    fun provideDogedexDatabase(@ApplicationContext context: Context): DogedexDatabase {
        return Room.databaseBuilder(
            context,
            DogedexDatabase::class.java,
            "dogedex-database"
        ).build()
    }
}