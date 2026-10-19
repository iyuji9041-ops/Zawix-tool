package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val isPinned: Boolean = false,
    val isFavorite: Boolean = false,
    val isChecklist: Boolean = false,
    val checklistJson: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class ChecklistItem(
    val id: String,
    val text: String,
    val isCompleted: Boolean = false
)
