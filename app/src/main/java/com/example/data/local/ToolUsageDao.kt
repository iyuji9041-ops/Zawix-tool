package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.ToolUsageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ToolUsageDao {
    @Query("SELECT * FROM tool_usage")
    fun getAllToolUsages(): Flow<List<ToolUsageEntity>>

    @Query("SELECT * FROM tool_usage WHERE isFavorite = 1")
    fun getFavoriteToolIds(): Flow<List<ToolUsageEntity>>

    @Query("SELECT * FROM tool_usage WHERE lastUsedTimestamp > 0 ORDER BY lastUsedTimestamp DESC LIMIT :limit")
    fun getRecentTools(limit: Int = 10): Flow<List<ToolUsageEntity>>

    @Query("SELECT * FROM tool_usage WHERE toolId = :toolId")
    suspend fun getUsage(toolId: String): ToolUsageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUsage(usage: ToolUsageEntity)

    @Query("UPDATE tool_usage SET isFavorite = :isFavorite WHERE toolId = :toolId")
    suspend fun setFavorite(toolId: String, isFavorite: Boolean)

    @Query("UPDATE tool_usage SET lastUsedTimestamp = 0")
    suspend fun clearRecents()
}
