package com.example.data.repository

import com.example.data.dao.NoteDao
import com.example.data.model.Note
import kotlinx.coroutines.flow.Flow

class NoteRepository(private val noteDao: NoteDao) {
    val activeNotes: Flow<List<Note>> = noteDao.getActiveNotes()
    val archivedNotes: Flow<List<Note>> = noteDao.getArchivedNotes()
    val favoriteNotes: Flow<List<Note>> = noteDao.getFavoriteNotes()
    val noteCount: Flow<Int> = noteDao.getNoteCount()

    fun getNoteById(id: Long): Flow<Note?> = noteDao.getNoteById(id)

    suspend fun getNoteByIdDirect(id: Long): Note? = noteDao.getNoteByIdDirect(id)

    fun searchNotes(query: String): Flow<List<Note>> = noteDao.searchNotes(query)

    suspend fun insert(note: Note): Long = noteDao.insertNote(note)

    suspend fun update(note: Note) = noteDao.updateNote(note)

    suspend fun delete(note: Note) = noteDao.deleteNote(note)

    suspend fun deleteById(id: Long) = noteDao.deleteNoteById(id)
}
