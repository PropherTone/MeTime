package com.protone.note.viewModel

import android.net.Uri
import com.protone.component.R
import com.protone.common.baseType.getString
import com.protone.common.baseType.imageSaveToDisk
import com.protone.common.baseType.toast
import com.protone.common.entity.GalleriesWithNotes
import com.protone.common.entity.GalleryMedia
import com.protone.common.entity.Note
import com.protone.common.entity.NoteDirWithNotes
import com.protone.common.utils.json.toUri
import com.protone.common.utils.json.toUriJson
import com.protone.common.utils.onResult
import com.protone.component.BaseViewModel
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

    var iconUri: Uri? = null
    var noteByName: Note? = null
    var allNote: MutableList<String>? = null
    var onEdit = false
    var medias = arrayListOf<GalleryMedia>()

    suspend fun saveIcon(name: String, w: Int, h: Int) = withContext(Dispatchers.IO) {
        iconUri?.imageSaveToDisk(name, null, w, h).let {
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
        (noteDAO.getAllNote() ?: mutableListOf())
            .map { note -> note.title }.toList() as MutableList<String>
    }

    suspend fun getMusicTitle(uri: Uri) = withContext(Dispatchers.IO) {
        val musicByUri = musicDAO.getMusicByUri(uri)
        musicByUri?.title ?: "^ ^"
    }

    suspend fun copyNote(inNote: Note, note: Note, w: Int, h: Int) =
        withContext(Dispatchers.Default) {
            inNote.title = note.title
            inNote.text = note.text
            inNote.richCode = note.richCode
            inNote.time = note.time
            inNote.imagePath = if (iconUri != null) saveIcon(note.title, w, h) else inNote.imagePath
        }

    suspend fun updateNote(note: Note) = noteDAO.updateNote(note)

    suspend fun insertNote(note: Note, dir: String?) = withContext(Dispatchers.Default) {
        noteDAO.insertNoteRs(note).let { result ->
            if (result.first) {
                dir?.let {
                    val noteDir = noteDAO.getNoteDir(it)
                    if (noteDir != null) {
                        noteDAO.insertNoteDirWithNote(
                            NoteDirWithNotes(noteDir.noteDirId, result.second)
                        )
                    }
                }
                medias.forEach {
                    galleryDAO.insertGalleriesWithNotes(GalleriesWithNotes(it.mediaId, result.second))
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
        noteDAO.getAllNote()?.forEach {
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
        noteDAO.getNoteByName(name)

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