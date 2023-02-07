package com.protone.note.viewModel

import android.net.Uri
import com.protone.common.entity.GalleryMedia
import com.protone.component.BaseViewModel
import com.protone.component.ViewEventHandle
import com.protone.component.ViewEventHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import java.util.stream.Collectors

class NoteViewViewModel : BaseViewModel(),
    ViewEventHandle<NoteViewViewModel.NoteViewEvent> by ViewEventHandler() {

    sealed class NoteViewEvent : ViewEvent {
        object Next : NoteViewEvent()
        object Edit : NoteViewEvent()
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