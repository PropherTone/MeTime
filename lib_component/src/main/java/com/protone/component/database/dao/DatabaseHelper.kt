package com.protone.component.database.dao

import android.content.Context
import com.protone.common.R
import com.protone.common.baseType.getString
import com.protone.common.baseType.toast
import com.protone.component.database.MediaAction
import com.protone.database.room.showRoomDB
import com.protone.database.room.shutdownDataBase
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.emitAll

abstract class DatabaseHelper {

    private val executorService by lazy { CoroutineScope(Dispatchers.IO) }
    fun getScope() = executorService

    private val _musicMessenger = MutableSharedFlow<MediaAction.MusicDataAction>()
    val musicMessenger = _musicMessenger.asSharedFlow()

    private val _noteMessenger = MutableSharedFlow<MediaAction.NoteDataAction>()
    val noteMessenger = _noteMessenger.asSharedFlow()

    private val _galleryMessenger = MutableSharedFlow<MediaAction.GalleryDataAction>()
    val galleryMessenger = _galleryMessenger.asSharedFlow()

    protected suspend fun sendMusicAction(musicDataAction: MediaAction.MusicDataAction) {
        _musicMessenger.emit(musicDataAction)
    }

    protected suspend fun sendNoteAction(noteDataAction: MediaAction.NoteDataAction) {
        _noteMessenger.emit(noteDataAction)
    }

    protected suspend fun sendGalleryAction(galleryDataAction: MediaAction.GalleryDataAction) {
        _galleryMessenger.emit(galleryDataAction)
    }

    protected suspend fun sendGalleryAction(galleryDataAction: List<MediaAction.GalleryDataAction>) {
        _galleryMessenger.emitAll(galleryDataAction.asFlow())
    }

    protected suspend fun sendMusicAction(musicDataAction: List<MediaAction.MusicDataAction>) {
        _musicMessenger.emitAll(musicDataAction.asFlow())
    }

    protected suspend fun sendNoteAction(noteDataAction: List<MediaAction.NoteDataAction>) {
        _noteMessenger.emitAll(noteDataAction.asFlow())
    }

    fun showDataBase(context: Context) {
        showRoomDB(context)
    }

    fun shutdownNow() {
        if (executorService.isActive) {
            executorService.cancel()
        }
        shutdownDataBase()
    }

    inline fun execute(
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        crossinline runnable: suspend () -> Unit
    ): Job = getScope().launch(dispatcher) {
        try {
            runnable.invoke()
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            e.printStackTrace()
            R.string.unknown_error.getString().toast()
        } finally {
            cancel()
        }
    }

}