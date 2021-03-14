package com.smartpocket.cuantoteroban.di

import com.smartpocket.cuantoteroban.CurrencyManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppSingletonModule {

    @Singleton
    @Provides
    fun provideCurrencyManager() = CurrencyManager()

}