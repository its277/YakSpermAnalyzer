package com.yaksperm.analyzer.di

import com.yaksperm.analyzer.data.repository.AnalysisRepository
import com.yaksperm.analyzer.data.repository.AnalysisRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindAnalysisRepository(impl: AnalysisRepositoryImpl): AnalysisRepository
}
