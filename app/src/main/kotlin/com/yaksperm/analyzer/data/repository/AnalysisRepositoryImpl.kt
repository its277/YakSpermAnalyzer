package com.yaksperm.analyzer.data.repository

import com.yaksperm.analyzer.data.database.AnalysisDao
import com.yaksperm.analyzer.data.database.entities.AnalysisEntity
import com.yaksperm.analyzer.domain.model.AnalysisSession
import com.yaksperm.analyzer.domain.model.CasaResult
import com.yaksperm.analyzer.domain.model.MotilityGrade
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalysisRepositoryImpl @Inject constructor(
    private val dao: AnalysisDao
) : AnalysisRepository {

    override suspend fun saveResult(result: CasaResult): Long {
        val entity = result.toEntity()
        return dao.insert(entity)
    }

    override fun getAllSessions(): Flow<List<AnalysisSession>> {
        return dao.getAll().map { entities ->
            entities.map { it.toSession() }
        }
    }

    override suspend fun getResultById(id: Long): CasaResult? {
        return dao.getById(id)?.toCasaResult()
    }

    override suspend fun getEntityById(id: Long): AnalysisEntity? = dao.getById(id)

    override suspend fun deleteById(id: Long) = dao.deleteById(id)

    override suspend fun updatePdfPath(id: Long, pdfPath: String) = dao.updatePdfPath(id, pdfPath)

    override suspend fun getTotalCount() = dao.count()

    override suspend fun getCountSince(timestamp: Long) = dao.countSince(timestamp)

    override suspend fun getAverageMotility() = dao.averageMotility() ?: 0f

    override suspend fun getLatestResult(): CasaResult? = dao.getLatest()?.toCasaResult()

    // ─────────────────────────────────────────────────────────────────────────
    // Mappers
    // ─────────────────────────────────────────────────────────────────────────

    private fun CasaResult.toEntity() = AnalysisEntity(
        sessionId = sessionId, sampleId = sampleId, technicianName = technicianName,
        timestamp = timestamp, totalDetected = totalDetected, totalTracked = totalTracked,
        progressiveMotile = progressiveMotile, nonProgressiveMotile = nonProgressiveMotile,
        immotile = immotile, totalMotilityPct = totalMotilityPct,
        progressiveMotilityPct = progressiveMotilityPct,
        nonProgressiveMotilityPct = nonProgressiveMotilityPct,
        vclMean = vclMean, vapMean = vapMean, vslMean = vslMean,
        linMean = linMean, strMean = strMean, wobMean = wobMean,
        grade = grade.name, videoPath = videoPath,
        framesProcessed = framesProcessed, durationSeconds = durationSeconds
    )

    private fun AnalysisEntity.toCasaResult() = CasaResult(
        sessionId = sessionId, sampleId = sampleId, technicianName = technicianName,
        timestamp = timestamp, totalDetected = totalDetected, totalTracked = totalTracked,
        progressiveMotile = progressiveMotile, nonProgressiveMotile = nonProgressiveMotile,
        immotile = immotile, totalMotilityPct = totalMotilityPct,
        progressiveMotilityPct = progressiveMotilityPct,
        nonProgressiveMotilityPct = nonProgressiveMotilityPct,
        vclMean = vclMean, vapMean = vapMean, vslMean = vslMean,
        linMean = linMean, strMean = strMean, wobMean = wobMean,
        grade = MotilityGrade.valueOf(grade), videoPath = videoPath,
        framesProcessed = framesProcessed, durationSeconds = durationSeconds
    )

    private fun AnalysisEntity.toSession() = AnalysisSession(
        id = id, sampleId = sampleId, technicianName = technicianName,
        timestamp = timestamp, grade = MotilityGrade.valueOf(grade),
        totalMotilityPct = totalMotilityPct,
        progressiveMotilityPct = progressiveMotilityPct,
        totalTracked = totalTracked, pdfPath = pdfPath
    )
}
