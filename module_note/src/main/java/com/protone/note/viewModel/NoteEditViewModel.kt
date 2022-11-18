package com.protone.worker.viewModel

import android.net.Uri
import com.protone.api.baseType.getString
import com.protone.api.baseType.imageSaveToDisk
import com.protone.api.baseType.toast
import com.protone.api.entity.GalleriesWithNotes
import com.protone.api.entity.GalleryMedia
import com.protone.api.entity.Note
import com.protone.api.entity.NoteDirWithNotes
import com.protone.api.json.toUri
import com.protone.api.json.toUriJson
import com.protone.api.onResult
import com.protone.worker.R
import com.protone.worker.database.DatabaseHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class NoteEditViewModel : BaseViewModel() {

    sealed class NoteEvent : ViewEvent {
        object Confirm : NoteEvent()
        object PickImage : NoteEvent()
        object PickVideo : NoteEvent()
        object PickMusic : NoteEvent()
        object PickIcon : NoteEvent()
    }

    companion object {
        const val NOTE_DIR = "NoteType"
        const val NOTE = "Note"
        const val CONTENT_TITLE = "NoteContentTitle"
    }

    var iconUri: Uri? = null
    var noteByName: Note? = null
    var allNote: MutableList<String>? = null
    var onEdit = false
    var medias = arrayListOf<GalleryMedia>()

    suspend fun saveIcon(name: String) = withContext(Dispatchers.IO) {
        iconUri?.imageSaveToDisk(name, null).let {
            if (!it.isNullOrEmpty()) {
                it
            } else {
                if (null != iconUri) {
                    R.string.failed_upload_image.getString().toast()
                }
                iconUri?.toUriJson()
            }
        }
    }

    suspend fun getAllNote() = withContext(Dispatchers.Default) {
        (DatabaseHelper.instance.noteDAOBridge.getAllNote() ?: mutableListOf())
            .map { note -> note.title }.toList() as MutableList<String>
    }

    suspend fun getMusicTitle(uri: Uri) = withContext(Dispatchers.IO) {
        val musicByUri = DatabaseHelper.instance.musicDAOBridge.getMusicByUri(uri)
        musicByUri?.title ?: "^ ^"
    }

    suspend fun copyNote(inNote: Note, note: Note) = withContext(Dispatchers.Default) {
        inNote.title = note.title
        inNote.text = note.text
        inNote.richCode = note.richCode
        inNote.time = note.time
        inNote.imagePath = if (iconUri != null) saveIcon(note.title) else inNote.imagePath
    }

    suspend fun updateNote(note: Note) =
        DatabaseHelper.instance.noteDAOBridge.updateNote(note)

    suspend fun insertNote(note: Note, dir: String?) = withContext(Dispatchers.Default) {
        DatabaseHelper.instance.noteDAOBridge.insertNoteRs(note).let { result ->
            if (result.first) {
                dir?.let {
                    val noteDir = DatabaseHelper.instance.noteDirDAOBridge.getNoteDir(it)
                    if (noteDir != null) {
                        DatabaseHelper
                            .instance
                            .noteDirWithNoteDAOBridge
                            .insertNoteDirWithNote(
                                NoteDirWithNotes(noteDir.noteDirId, result.second)
                            )
                    }
                }
                medias.forEach {
                    DatabaseHelper
                        .instance
                        .galleriesWithNotesDAOBridge
                        .insertGalleriesWithNotes(GalleriesWithNotes(it.uri, result.second))
                }
                true
            } else {
                R.string.failed_msg.getString().toast()
                false
            }
        }
    }

    suspend fun checkNoteTitle(noteTitle: String): String = withContext(Dispatchers.Default) {
        var count = 0
        var tempNoteTitle = noteTitle
        val names = mutableMapOf<String, Int>()
        DatabaseHelper.instance.noteDAOBridge.getAllNote()?.forEach {
            names[it.title] = 1
            if (it.title == tempNoteTitle) {
                tempNoteTitle = "${noteTitle}(${++count})"
            }
        }
        while (names[tempNoteTitle] != null) {
            tempNoteTitle = "${noteTitle}(${++count})"
        }
        if (onEdit) noteTitle else tempNoteTitle
    }

    suspend fun getNoteByName(name: String) =
        DatabaseHelper.instance.noteDAOBridge.getNoteByName(name)


    suspend fun checkNoteCover(imagePath: String) = onResult { co ->
        onEdit = true
        if (imagePath.isEmpty()) return@onResult
        val file = File(imagePath)
        if (file.isFile) {
            co.resumeWith(Result.success(true))
        } else {
            iconUri = imagePath.toUri()
            co.resumeWith(Result.success(false))
        }
    }

}