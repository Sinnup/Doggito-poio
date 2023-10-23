package com.fruse.dogedex.camera.di

import com.fruse.dogedex.camera.machinelearning.ClassifierRepository
import com.fruse.dogedex.camera.machinelearning.ClassifierTasks
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