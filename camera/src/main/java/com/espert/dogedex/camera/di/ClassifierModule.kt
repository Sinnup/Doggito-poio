package com.espert.dogedex.camera.di

import com.espert.dogedex.camera.machinelearning.ClassifierRepository
import com.espert.dogedex.camera.machinelearning.ClassifierTasks
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ClassifierModule {

    @Binds
    abstract fun bindClassifierTasks(classifierRepository: ClassifierRepository): ClassifierTasks
}