package com.yaksperm.analyzer.data.repository

import com.yaksperm.analyzer.data.database.entities.AnalysisEntity
import com.yaksperm.analyzer.domain.model.AnalysisSession
import com.yaksperm.analyzer.domain.model.CasaResult
import kotlinx.coroutines.flow.Flow

interface AnalysisRepository {
    suspend fun saveResult(result: CasaResult): Long
    fun getAllSessions(): Flow<List<AnalysisSession>>
    suspend fun getResultById(id: Long): CasaResult?
    suspend fun getEntityById(id: Long): AnalysisEntity?
    suspend fun deleteById(id: Long)
    suspend fun updatePdfPath(id: Long, pdfPath: String)
    suspend fun getTotalCount(): Int
    suspend fun getCountSince(timestamp: Long): Int
    suspend fun getAverageMotility(): Float
    suspend fun getLatestResult(): CasaResult?
}
