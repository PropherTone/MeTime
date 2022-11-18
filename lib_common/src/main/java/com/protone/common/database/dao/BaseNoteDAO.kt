package com.protone.common.database.dao

import com.protone.common.baseType.withIOContext
import com.protone.common.database.MediaAction
import com.protone.common.entity.Note
import com.protone.common.entity.NoteDir
import com.protone.common.entity.NoteDirWithNotes
import com.protone.database.room.getNoteDAO
import com.protone.database.room.getNoteDirWithNoteDAO
import com.protone.database.room.getNoteTypeDAO
import kotlinx.coroutines.flow.Flow

sealed class BaseNoteDAO : BaseDAO<MediaAction.NoteDataAction>() {

    private val noteDAO = getNoteDAO()

    suspend fun getAllNote(): List<Note>? = withIOContext {
        noteDAO.getAllNote()
    }

    fun getAllNoteFlow(): Flow<List<Note>> = noteDAO.getAllNoteFlow()

    suspend fun getNoteByName(name: String): Note? = withIOContext {
        noteDAO.getNoteByName(name)
    }

    suspend fun getNoteById(id: Long): Note? = withIOContext {
        noteDAO.getNoteById(id)
    }

    suspend fun updateNote(note: Note): Int = withIOContext {
        noteDAO.updateNote(note).run {
            sendEvent(MediaAction.NoteDataAction.OnNoteUpdated(note))
            this
        }
    }

    suspend fun deleteNote(note: Note) = withIOContext {
        noteDAO.deleteNote(note).run {
            sendEvent(MediaAction.NoteDataAction.OnNoteDeleted(note))
        }
    }

    suspend fun insertNote(note: Note): Long = withIOContext {
        noteDAO.insertNote(note).run {
            note.noteId = this
            sendEvent(MediaAction.NoteDataAction.OnNoteInserted(note))
            this
        }
    }

    /*NoteDirWithNote***********************************************************/
    private val noteDirWithNoteDAO = getNoteDirWithNoteDAO()

    suspend fun insertNoteDirWithNote(noteDirWithNotes: NoteDirWithNotes) =
        withIOContext {
            sendEvent(MediaAction.NoteDataAction.OnNoteDirWithNoteInserted(noteDirWithNotes))
            noteDirWithNoteDAO.insertNoteDirWithNote(noteDirWithNotes)
        }

    suspend fun getNotesWithNoteDir(noteDirId: Long): List<Note> = withIOContext {
        noteDirWithNoteDAO.getNotesWithNoteDir(noteDirId) ?: mutableListOf()
    }

    suspend fun getNoteDirWithNote(noteId: Long): List<NoteDir> = withIOContext {
        noteDirWithNoteDAO.getNoteDirWithNote(noteId) ?: mutableListOf()
    }

    fun getNotesWithNoteDirFlow(noteDirId: Long): Flow<List<Note>?> =
        noteDirWithNoteDAO.getNotesWithNoteDirFlow(noteDirId)

    /*NoteType***********************************************************/
    private val noteTypeDAO = getNoteTypeDAO()

    suspend fun getNoteDir(name: String): NoteDir? =
        withIOContext { noteTypeDAO.getNoteDir(name) }

    suspend fun getALLNoteDir(): List<NoteDir>? =
        withIOContext { noteTypeDAO.getALLNoteDir() }

    suspend fun insertNoteDir(noteDir: NoteDir) = withIOContext {
        sendEvent(MediaAction.NoteDataAction.OnNoteDirInserted(noteDir))
        noteTypeDAO.insertNoteDir(noteDir)
    }

    suspend fun deleteNoteDir(noteDir: NoteDir) = withIOContext {
        sendEvent(MediaAction.NoteDataAction.OnNoteDirDeleted(noteDir))
        noteTypeDAO.deleteNoteDir(noteDir)
    }

    fun getALLNoteDirFlow(): Flow<List<NoteDir>?> = noteTypeDAO.getALLNoteDirFlow()

}
