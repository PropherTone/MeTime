package com.protone.note.viewModel

import androidx.lifecycle.viewModelScope
import com.protone.component.R
import com.protone.common.baseType.*
import com.protone.common.context.MApplication
import com.protone.common.entity.Note
import com.protone.common.entity.NoteDir
import com.protone.common.entity.NoteDirWithNotes
import com.protone.component.BaseViewModel
import com.protone.component.database.MediaAction
import com.protone.component.tools.ViewEventHandle
import com.protone.component.tools.ViewEventHandler
import kotlinx.coroutines.Job

class NoteViewModel : BaseViewModel(),
    ViewEventHandle<NoteViewModel.NoteViewEvent> by ViewEventHandler() {

    sealed class NoteViewEvent : ViewEvent {
        data class RefreshList(val notes: List<Note>, val buckets: List<NoteDir>) : NoteViewEvent()
        data class OnTypeSelected(val notes: List<Note>) : NoteViewEvent()
        data class OnNotesUpdated(val notes: List<Note>) : NoteViewEvent()
        data class OnBucketInserted(val bucket: NoteDir) : NoteViewEvent()
    }

    private var noteDirWatcherJob: Job? = null

    private fun createNoteDirJob(dir: NoteDir) {
        noteDirWatcherJob?.cancel()
        noteDirWatcherJob = (if (dir.name == R.string.all.getString()) {
            viewModelScope.launchDefault {
                noteDAO.getAllNoteFlow().collect { notes ->
                    sendViewEvent(NoteViewEvent.OnNotesUpdated(notes))
                }
            }
        } else viewModelScope.launchDefault {
            noteDAO.getNotesWithNoteDirFlow(dir.noteDirId)
                .collect { notes ->
                    notes?.let { nonNullNotes ->
                        sendViewEvent(NoteViewEvent.OnNotesUpdated(nonNullNotes))
                    }
                }
        }).also { it.start() }
    }

    fun collectNoteEvent() {
        viewModelScope.launchDefault {
            observeNoteDate {
                when (it) {
                    is MediaAction.NoteDataAction.OnNoteDeleted -> {
                        it.note.imagePath?.deleteFile()
                        "${MApplication.app.filesDir.absolutePath}/${it.note.title}/".deleteFile()
                    }
                    else -> Unit
                }
            }
        }
    }

    fun refreshListData() {
        viewModelScope.launchIO {
            sendViewEvent(
                NoteViewEvent.RefreshList(
                    getNoteList(NoteDir(R.string.all.getString(), null)),
                    queryAllNoteType()
                )
            )
        }
    }

    fun addBucket(name: String) {
        viewModelScope.launchIO {
            if (name.isNotEmpty()) insertNoteDir(name, "").let { pair ->
                if (pair.first) {
                    sendViewEvent(NoteViewEvent.OnBucketInserted(pair.second))
                } else {
                    R.string.failed_msg.getString().toast()
                }
            } else R.string.enter.getString().toast()
        }
    }

    fun onTypeSelected(dir: NoteDir) {
        viewModelScope.launchIO {
            sendViewEvent(NoteViewEvent.OnTypeSelected(getNoteList(dir)))
        }
    }

    fun deleteNote(note: Note) {
        noteDAO.deleteNoteAsync(note)
    }

    fun deleteNoteDir(noteType: NoteDir) {
        viewModelScope.launchIO {
            noteDAO.doDeleteNoteDirRs(noteType)
        }
    }

    private suspend fun getNoteList(type: NoteDir) =
        if (type.name == R.string.all.getString()) {
            createNoteDirJob(type)
            noteDAO.getAllNote() ?: listOf()
        } else noteDAO.getNotesWithNoteDir(type.noteDirId).also {
            createNoteDirJob(type)
        }

    private suspend fun queryAllNoteType() =
        (noteDAO.getALLNoteDir() ?: mutableListOf()) as MutableList<NoteDir>

    private suspend fun insertNoteDir(type: String?, image: String?) =
        noteDAO.insertNoteDirRs(NoteDir(type, image))

}