package com.protone.worker.viewModel

import android.net.Uri
import com.protone.api.entity.GalleryMedia
import com.protone.worker.database.DatabaseHelper
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
        DatabaseHelper.instance.noteDAOBridge.getNoteByName(name)
    }

    suspend fun getMusicByUri(uri: Uri) = withContext(Dispatchers.IO) {
        DatabaseHelper.instance.musicDAOBridge.getMusicByUri(uri)
    }

    suspend fun filterMedia(uri: Uri, isVideo: Boolean): MutableList<GalleryMedia>? =
        withContext(Dispatchers.IO) {
            DatabaseHelper.instance.signedGalleryDAOBridge.getAllMediaByType(isVideo)?.stream()
                ?.filter { media -> media.uri == uri }
                ?.collect(Collectors.toList())
        }

}