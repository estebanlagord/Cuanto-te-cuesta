package com.smartpocket.cuantoteroban.preferences.di

import com.smartpocket.cuantoteroban.CurrencyManager
import com.smartpocket.cuantoteroban.preferences.MyFragmentFactory
import com.smartpocket.cuantoteroban.preferences.PreferencesManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PreferencesModule {

    @Singleton
    @Provides
    fun providePreferencesManager(currencyManager: CurrencyManager) =
            PreferencesManager(currencyManager)

    @Singleton
    @Provides
    fun provideFragmentFactory(preferencesManager: PreferencesManager, currencyManager: CurrencyManager) =
            MyFragmentFactory(preferencesManager, currencyManager)
}