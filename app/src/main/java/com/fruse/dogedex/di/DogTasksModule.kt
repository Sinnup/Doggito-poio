package com.fruse.dogedex.di

import com.fruse.dogedex.dogList.DogRepository
import com.fruse.dogedex.dogList.DogTasks
import com.fruse.dogedex.dogList.ImageRepository
import com.fruse.dogedex.dogList.ImageRepositoryImpl
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