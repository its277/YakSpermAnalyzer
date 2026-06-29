package com.yaksperm.analyzer.di

import android.content.Context
import androidx.room.Room
import com.yaksperm.analyzer.data.database.AnalysisDao
import com.yaksperm.analyzer.data.database.AnalysisDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AnalysisDatabase =
        Room.databaseBuilder(context, AnalysisDatabase::class.java, "yaksperm_db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideDao(db: AnalysisDatabase): AnalysisDao = db.analysisDao()
}
