package com.espert.dogedex.core.di

import android.content.Context
import com.espert.dogedex.core.utils.AssetCopyHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AssetModule {

    @Provides
    @Singleton
    fun provideAssetCopyHelper(
        @ApplicationContext context: Context
    ): AssetCopyHelper {
        return AssetCopyHelper(context)
    }
}