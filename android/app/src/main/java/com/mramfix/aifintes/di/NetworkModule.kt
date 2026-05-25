package com.mramfix.aifintes.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    // Retrofit + OkHttp будет добавлен в AN-3
}
