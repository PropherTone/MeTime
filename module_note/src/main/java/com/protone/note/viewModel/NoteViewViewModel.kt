package com.protone.note.viewModel

import android.net.Uri
import com.protone.common.entity.GalleryMedia
import com.protone.component.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import java.util.stream.Collectors

class NoteViewViewModel : BaseViewModel() {

    sealed class NoteViewEvent : ViewEvent {
        object Next : NoteViewEvent()
        object Edit : NoteViewEvent()
    }

    companion object {
        const val NOTE_NAME = "NOTE_NAME"
    }

    val noteQueue = ArrayDeque<String>()

    suspend fun getNoteByName(name: String) = withContext(Dispatchers.IO) {
        noteDAO.getNoteByName(name)
    }

    suspend fun getMusicByUri(uri: Uri) = withContext(Dispatchers.IO) {
        musicDAO.getMusicByUri(uri)
    }

    suspend fun filterMedia(uri: Uri, isVideo: Boolean): MutableList<GalleryMedia>? =
        withContext(Dispatchers.IO) {
            galleryDAO.getAllMediaByType(isVideo)?.stream()
                ?.filter { media -> media.uri == uri }
                ?.collect(Collectors.toList())
        }

}