package com.yaksperm.analyzer.di

import android.content.Context
import com.yaksperm.analyzer.domain.casa.CasaCalculator
import com.yaksperm.analyzer.domain.inference.YoloDetector
import com.yaksperm.analyzer.domain.tracker.UKFTracker
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object InferenceModule {

    @Provides
    @Singleton
    fun provideYoloDetector(@ApplicationContext context: Context): YoloDetector =
        YoloDetector(context)

    @Provides
    @Singleton
    fun provideUkfTracker(): UKFTracker = UKFTracker()

    // CasaCalculator is an object — no injection needed but we expose it for testability
    @Provides
    @Singleton
    fun provideCasaCalculator(): CasaCalculator = CasaCalculator
}
