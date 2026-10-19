package com.example.data.repository

import com.example.data.local.NoteDao
import com.example.data.model.ChecklistItem
import com.example.data.model.NoteEntity
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class NotesRepository(private val noteDao: NoteDao) {

    fun getAllNotes(): Flow<List<NoteEntity>> = noteDao.getAllNotes()

    fun searchNotes(query: String): Flow<List<NoteEntity>> = noteDao.searchNotes(query)

    suspend fun getNoteById(id: Long): NoteEntity? = noteDao.getNoteById(id)

    suspend fun saveNote(
        id: Long = 0,
        title: String,
        content: String,
        isPinned: Boolean = false,
        isFavorite: Boolean = false,
        isChecklist: Boolean = false,
        checklist: List<ChecklistItem> = emptyList()
    ): Long {
        val json = serializeChecklist(checklist)
        val existingNote = if (id != 0L) noteDao.getNoteById(id) else null
        val createdTime = existingNote?.createdAt ?: System.currentTimeMillis()

        val entity = NoteEntity(
            id = id,
            title = title.trim(),
            content = content.trim(),
            isPinned = isPinned,
            isFavorite = isFavorite,
            isChecklist = isChecklist,
            checklistJson = json,
            createdAt = createdTime, // Preserved when editing an existing note
            updatedAt = System.currentTimeMillis()
        )
        return if (id == 0L) {
            noteDao.insertNote(entity)
        } else {
            noteDao.updateNote(entity)
            id
        }
    }

    suspend fun deleteNote(id: Long) {
        noteDao.deleteNoteById(id)
    }

    suspend fun togglePin(note: NoteEntity) {
        noteDao.updateNote(note.copy(isPinned = !note.isPinned, updatedAt = System.currentTimeMillis()))
    }

    fun parseChecklist(json: String): List<ChecklistItem> {
        if (json.isBlank()) return emptyList()
        return try {
            val array = JSONArray(json)
            val items = mutableListOf<ChecklistItem>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                items.add(
                    ChecklistItem(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        text = obj.optString("text", ""),
                        isCompleted = obj.optBoolean("isCompleted", false)
                    )
                )
            }
            items
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun serializeChecklist(items: List<ChecklistItem>): String {
        val array = JSONArray()
        for (item in items) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("text", item.text)
                put("isCompleted", item.isCompleted)
            }
            array.put(obj)
        }
        return array.toString()
    }
}
