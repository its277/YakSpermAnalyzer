package com.yaksperm.analyzer.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.yaksperm.analyzer.data.database.entities.AnalysisEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AnalysisDao {

    @Insert
    suspend fun insert(entity: AnalysisEntity): Long

    @Update
    suspend fun update(entity: AnalysisEntity)

    @Query("SELECT * FROM analyses ORDER BY timestamp DESC")
    fun getAll(): Flow<List<AnalysisEntity>>

    @Query("SELECT * FROM analyses WHERE id = :id")
    suspend fun getById(id: Long): AnalysisEntity?

    @Query("SELECT * FROM analyses ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatest(): AnalysisEntity?

    @Query("SELECT COUNT(*) FROM analyses")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM analyses WHERE timestamp >= :startOfMonth")
    suspend fun countSince(startOfMonth: Long): Int

    @Query("SELECT AVG(totalMotilityPct) FROM analyses")
    suspend fun averageMotility(): Float?

    @Query("DELETE FROM analyses WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE analyses SET pdfPath = :pdfPath WHERE id = :id")
    suspend fun updatePdfPath(id: Long, pdfPath: String)
}
