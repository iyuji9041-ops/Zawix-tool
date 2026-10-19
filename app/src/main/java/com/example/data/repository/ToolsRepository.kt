package com.example.data.repository

import com.example.data.local.ToolUsageDao
import com.example.data.model.Tool
import com.example.data.model.ToolUsageEntity
import com.example.data.model.ToolsRegistry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ToolsRepository(private val toolUsageDao: ToolUsageDao) {

    val allToolsFlow: Flow<List<Tool>> = toolUsageDao.getAllToolUsages().map { usages ->
        val favoriteMap = usages.associate { it.toolId to it.isFavorite }
        ToolsRegistry.allTools.map { tool ->
            tool.copy(isFavorite = favoriteMap[tool.id] ?: false)
        }
    }

    val favoriteToolsFlow: Flow<List<Tool>> = toolUsageDao.getFavoriteToolIds().map { usages ->
        val favIds = usages.filter { it.isFavorite }.map { it.toolId }.toSet()
        ToolsRegistry.allTools.filter { it.id in favIds }.map { it.copy(isFavorite = true) }
    }

    val recentToolsFlow: Flow<List<Tool>> = toolUsageDao.getRecentTools(10).map { usages ->
        val toolMap = ToolsRegistry.allTools.associateBy { it.id }
        usages.mapNotNull { usage ->
            toolMap[usage.toolId]?.copy(isFavorite = usage.isFavorite)
        }
    }

    suspend fun toggleFavorite(toolId: String) {
        val existing = toolUsageDao.getUsage(toolId)
        if (existing == null) {
            toolUsageDao.upsertUsage(ToolUsageEntity(toolId = toolId, isFavorite = true, lastUsedTimestamp = 0L))
        } else {
            toolUsageDao.setFavorite(toolId, !existing.isFavorite)
        }
    }

    suspend fun recordToolUsage(toolId: String) {
        val existing = toolUsageDao.getUsage(toolId)
        val isFav = existing?.isFavorite ?: false
        toolUsageDao.upsertUsage(
            ToolUsageEntity(
                toolId = toolId,
                isFavorite = isFav,
                lastUsedTimestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun clearRecents() {
        toolUsageDao.clearRecents()
    }
}
