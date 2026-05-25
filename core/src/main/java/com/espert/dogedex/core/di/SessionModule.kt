package com.espert.dogedex.core.di

import com.espert.dogedex.core.session.SessionManager
import com.espert.dogedex.core.session.SessionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SessionModule {
    @Binds
    @Singleton
    abstract fun bindSessionManager(impl: SessionRepository): SessionManager
}
