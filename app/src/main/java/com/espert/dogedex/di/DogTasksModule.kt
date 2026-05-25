package com.espert.dogedex.di

import com.espert.dogedex.dogList.DogRepository
import com.espert.dogedex.dogList.DogTasks
import com.espert.dogedex.dogList.ImageRepository
import com.espert.dogedex.dogList.ImageRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
abstract class DogTasksModule {

    @Binds
    abstract fun bindDogTasks(
        dogRepository: DogRepository
    ): DogTasks

    @Binds
    @Singleton
    abstract fun bindImageRepository(
        imageRepositoryImpl: ImageRepositoryImpl
    ): ImageRepository
}