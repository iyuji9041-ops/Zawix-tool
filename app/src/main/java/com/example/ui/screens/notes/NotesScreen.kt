package com.example.ui.screens.notes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChecklistItem
import com.example.data.model.NoteEntity
import com.example.data.repository.NotesRepository
import com.example.ui.components.ConfirmDialog
import com.example.ui.components.ZaswixTopAppBar
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@Composable
fun NotesScreen(
    notesRepository: NotesRepository,
    onBackClick: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }

    val notesFlow = remember(searchQuery) {
        if (searchQuery.isBlank()) notesRepository.getAllNotes() else notesRepository.searchNotes(searchQuery)
    }
    val notesList by notesFlow.collectAsState(initial = emptyList())

    var activeNoteToEdit by remember { mutableStateOf<NoteEntity?>(null) }
    var isCreatingNew by remember { mutableStateOf(false) }
    var noteToDelete by remember { mutableStateOf<NoteEntity?>(null) }

    if (noteToDelete != null) {
        ConfirmDialog(
            title = "Delete Note",
            message = "Are you sure you want to delete '${noteToDelete?.title}'? This action cannot be undone.",
            confirmText = "Delete",
            isDestructive = true,
            onConfirm = {
                val toDelete = noteToDelete
                noteToDelete = null
                if (toDelete != null) {
                    coroutineScope.launch { notesRepository.deleteNote(toDelete.id) }
                }
            },
            onDismiss = { noteToDelete = null }
        )
    }

    if (isCreatingNew || activeNoteToEdit != null) {
        NoteEditorDialog(
            note = activeNoteToEdit,
            notesRepository = notesRepository,
            onDismiss = {
                isCreatingNew = false
                activeNoteToEdit = null
            },
            onSave = { title, content, isChecklist, checklist ->
                coroutineScope.launch {
                    val id = activeNoteToEdit?.id ?: 0L
                    notesRepository.saveNote(
                        id = id,
                        title = title,
                        content = content,
                        isPinned = activeNoteToEdit?.isPinned ?: false,
                        isChecklist = isChecklist,
                        checklist = checklist
                    )
                    isCreatingNew = false
                    activeNoteToEdit = null
                }
            }
        )
    }

    Scaffold(
        topBar = {
            ZaswixTopAppBar(
                title = "Notes & Checklists",
                onBackClick = onBackClick
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isCreatingNew = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_note_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add note or checklist")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search saved notes & tasks...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
                    .testTag("notes_search_input")
            )

            if (notesList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Checklist,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = if (searchQuery.isBlank()) "No notes yet" else "No matching notes found",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (searchQuery.isBlank()) "Tap '+' to create your first note or checklist. Everything is stored locally on your device." else "Try searching with different keywords.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(notesList, key = { it.id }) { note ->
                        NoteItemCard(
                            note = note,
                            notesRepository = notesRepository,
                            onClick = { activeNoteToEdit = note },
                            onTogglePin = {
                                coroutineScope.launch { notesRepository.togglePin(note) }
                            },
                            onDelete = { noteToDelete = note }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NoteItemCard(
    note: NoteEntity,
    notesRepository: NotesRepository,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val checklist = remember(note.checklistJson) {
        notesRepository.parseChecklist(note.checklistJson)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            1.dp,
            if (note.isPinned) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("note_item_${note.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = if (note.isChecklist) Icons.Default.Checklist else Icons.Default.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (note.title.isNotBlank()) note.title else "Untitled Note",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row {
                    IconButton(onClick = onTogglePin, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = if (note.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = if (note.isPinned) "Unpin" else "Pin",
                            tint = if (note.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (note.isChecklist && checklist.isNotEmpty()) {
                val completedCount = checklist.count { it.isCompleted }
                Text(
                    text = "$completedCount of ${checklist.size} completed",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                checklist.take(3).forEach { item ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = item.isCompleted,
                            onCheckedChange = { isChecked ->
                                val updated = checklist.map {
                                    if (it.id == item.id) it.copy(isCompleted = isChecked) else it
                                }
                                coroutineScope.launch {
                                    notesRepository.saveNote(
                                        id = note.id,
                                        title = note.title,
                                        content = note.content,
                                        isPinned = note.isPinned,
                                        isChecklist = true,
                                        checklist = updated
                                    )
                                }
                            },
                            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = item.text,
                            style = MaterialTheme.typography.bodySmall.copy(
                                textDecoration = if (item.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                            ),
                            color = if (item.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                if (checklist.size > 3) {
                    Text(
                        text = "+ ${checklist.size - 3} more items",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else if (note.content.isNotBlank()) {
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            val dateStr = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(note.updatedAt))
            Text(
                text = "Updated $dateStr",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun NoteEditorDialog(
    note: NoteEntity?,
    notesRepository: NotesRepository,
    onDismiss: () -> Unit,
    onSave: (title: String, content: String, isChecklist: Boolean, checklist: List<ChecklistItem>) -> Unit
) {
    var title by remember { mutableStateOf(note?.title ?: "") }
    var content by remember { mutableStateOf(note?.content ?: "") }
    var isChecklistMode by remember { mutableStateOf(note?.isChecklist ?: false) }

    val initialChecklist = remember(note) {
        if (note != null && note.isChecklist) notesRepository.parseChecklist(note.checklistJson) else emptyList()
    }
    val checklistItems = remember { mutableStateListOf<ChecklistItem>().apply { addAll(initialChecklist) } }
    var newChecklistText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (note == null) "Create Note" else "Edit Note",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                TabRow(
                    selectedTabIndex = if (isChecklistMode) 1 else 0,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Tab(
                        selected = !isChecklistMode,
                        onClick = { isChecklistMode = false },
                        text = { Text("Plain Note") }
                    )
                    Tab(
                        selected = isChecklistMode,
                        onClick = { isChecklistMode = true },
                        text = { Text("Checklist") }
                    )
                }

                if (!isChecklistMode) {
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("Content") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        shape = RoundedCornerShape(10.dp)
                    )
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newChecklistText,
                                onValueChange = { newChecklistText = it },
                                placeholder = { Text("Add checklist item...") },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Button(
                                onClick = {
                                    if (newChecklistText.isNotBlank()) {
                                        checklistItems.add(
                                            ChecklistItem(
                                                id = UUID.randomUUID().toString(),
                                                text = newChecklistText.trim(),
                                                isCompleted = false
                                            )
                                        )
                                        newChecklistText = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Add")
                            }
                        }

                        checklistItems.forEachIndexed { index, item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Checkbox(
                                        checked = item.isCompleted,
                                        onCheckedChange = { isChecked ->
                                            checklistItems[index] = item.copy(isCompleted = isChecked)
                                        },
                                        colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                                    )
                                    Text(
                                        text = item.text,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            textDecoration = if (item.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                                        ),
                                        color = if (item.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                IconButton(onClick = { checklistItems.removeAt(index) }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Delete item", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(title, content, isChecklistMode, checklistItems.toList())
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.testTag("save_note_confirm_button")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}
