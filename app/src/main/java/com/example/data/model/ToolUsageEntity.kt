package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tool_usage")
data class ToolUsageEntity(
    @PrimaryKey
    val toolId: String,
    val isFavorite: Boolean = false,
    val lastUsedTimestamp: Long = 0L
)
