package com.fruse.dogedex.auth.di

import com.fruse.dogedex.auth.auth.AuthRepository
import com.fruse.dogedex.auth.auth.AuthTasks
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