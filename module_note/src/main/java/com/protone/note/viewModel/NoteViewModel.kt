package com.protone.note.viewModel

import androidx.lifecycle.viewModelScope
import com.protone.common.R
import com.protone.common.baseType.deleteFile
import com.protone.common.baseType.getString
import com.protone.common.baseType.launchDefault
import com.protone.common.baseType.launchIO
import com.protone.common.context.MApplication
import com.protone.common.database.MediaAction
import com.protone.common.entity.Note
import com.protone.common.entity.NoteDir
import com.protone.common.entity.NoteDirWithNotes
import com.protone.component.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NoteViewModel : BaseViewModel() {

    sealed class NoteViewEvent : ViewEvent {
        object RefreshList : NoteViewEvent()
        object AddBucket : NoteViewEvent()
        object Refresh : NoteViewEvent()
        object HandleBucketEvent : NoteViewEvent()
    }

    private val noteList = mutableMapOf<String, MutableList<Note>>()

    private var selected: String? = null

    private var noteDirWatcherJob: Job? = null

    private var dirEmitter: ((List<NoteDir>) -> Unit)? = null
    private var notesEmitter: ((List<Note>) -> Unit)? = null

    private fun createNoteDirJob(dir: NoteDir) {
        noteDirWatcherJob?.cancel()
        noteDirWatcherJob = if (dir.name == R.string.all.getString()) {
            viewModelScope.launchDefault {
                noteDAO.getAllNoteFlow().collect { notes ->
                    notes.let { nonNullNotes -> notesEmitter?.invoke(nonNullNotes) }
                }
            }
        } else viewModelScope.launchDefault {
            noteDAO.getNotesWithNoteDirFlow(dir.noteDirId)
                .collect { notes ->
                    notes?.let { nonNullNotes -> notesEmitter?.invoke(nonNullNotes) }
                }
        }.also { it.start() }
    }

    private suspend fun getNoteDir(note: Note): List<NoteDir> =
        noteDAO.getNoteDirWithNote(note.noteId)

    fun collectNoteEvent(callBack: suspend (MediaAction.NoteDataAction) -> Unit) {
        viewModelScope.launchDefault { observeNoteDate { callBack(it) } }
    }

    fun watchNoteDirs(func: (List<NoteDir>) -> Unit) {
        this.dirEmitter = func
    }

    fun watchNotes(func: (List<Note>) -> Unit) {
        this.notesEmitter = func
    }

    suspend fun getNoteList(type: NoteDir) =
        if (type.name == R.string.all.getString()) {
            createNoteDirJob(type)
            noteDAO.getAllNote() ?: listOf()
        } else noteDAO.getNotesWithNoteDir(type.noteDirId).also {
            createNoteDirJob(type)
        }


    fun insertNewNoteToNoteDir(noteDirWithNotes: NoteDirWithNotes) {
        viewModelScope.launchIO {
            noteDAO.getNoteById(noteDirWithNotes.noteId)?.let { note ->
                noteList[R.string.all.getString()]?.add(note)
                val noteDir = getNoteDir(note)
                noteDir.forEach { noteList[it.name]?.add(note) }
            }
        }
    }

    fun removeNote(note: Note) {
        viewModelScope.launchDefault {
            noteList[R.string.all.getString()]?.remove(note)
            getNoteDir(note).forEach {
                noteList[it.name]?.remove(note)
            }
        }
    }

    fun addNote(note: Note) {
        viewModelScope.launchDefault {
            val all = R.string.all.getString()
            getNoteDir(note).forEach {
                noteList[all]?.add(note)
                noteList[it.name]?.add(note)
            }
        }
    }

    fun updateNote(note: Note) {
        viewModelScope.launchDefault {
            val all = R.string.all.getString()
            getNoteDir(note).forEach {
                noteList[all]?.replaceAll { note ->
                    if (note.noteId == note.noteId) {
                        note
                    } else note
                }
                noteList[it.name]?.replaceAll { note ->
                    if (note.noteId == note.noteId) {
                        note
                    } else note
                }
            }
        }
    }

    fun deleteNoteCache(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            note.imagePath?.deleteFile()
            "${MApplication.app.filesDir.absolutePath}/${note.title}/".deleteFile()
        }
    }

    fun deleteNote(note: Note) {
        noteDAO.deleteNoteAsync(note)
    }

    fun deleteNoteDir(noteType: NoteDir) {
        viewModelScope.launch(Dispatchers.IO) {
            noteDAO.doDeleteNoteDirRs(noteType)
        }
    }

    suspend fun getNote(title: String) = withContext(Dispatchers.IO) {
        noteDAO.getNoteByName(title)
    }

    suspend fun queryAllNote() = withContext(Dispatchers.IO) {
        noteDAO.getALLNoteDir()?.let {
            noteList[R.string.all.getString().also { s -> selected = s }] =
                mutableListOf<Note>().apply { addAll(noteDAO.getAllNote() ?: mutableListOf()) }
            it.forEach { noteDir ->
                noteList[noteDir.name] =
                    noteDAO.getNotesWithNoteDir(noteDir.noteDirId) as MutableList<Note>
            }
            noteList[selected]
        } ?: mutableListOf()
    }

    suspend fun queryAllNoteType() = withContext(Dispatchers.IO) {
        (noteDAO.getALLNoteDir() ?: mutableListOf()) as MutableList<NoteDir>
    }

    suspend fun insertNoteDir(type: String?, image: String?) =
        noteDAO.insertNoteDirRs(NoteDir(type, image))

}