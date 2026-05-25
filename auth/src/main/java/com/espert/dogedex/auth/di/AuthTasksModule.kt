package com.espert.dogedex.auth.di

import com.espert.dogedex.auth.auth.AuthRepository
import com.espert.dogedex.auth.auth.AuthTasks
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthTasksModule {

    @Binds
    abstract fun binAuthTasks(authRepository: AuthRepository): AuthTasks
}