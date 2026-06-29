package com.yaksperm.analyzer.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.yaksperm.analyzer.data.database.entities.AnalysisEntity

@Database(
    entities = [AnalysisEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AnalysisDatabase : RoomDatabase() {
    abstract fun analysisDao(): AnalysisDao
}
