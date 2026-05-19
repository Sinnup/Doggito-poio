package com.fruse.dogedex.core.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

typealias StringResolver = (Int) -> String

@Module
@InstallIn(SingletonComponent::class)
object StringResolverModule {
    @Provides
    fun provideStringResolver(@ApplicationContext ctx: Context): StringResolver = ctx::getString
}
