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
import kotlinx.coroutines.flow.asSharedFlow

abstract class DatabaseHelper {

    private val executorService by lazy { CoroutineScope(Dispatchers.IO) }
    fun getScope() = executorService

    private val _musicMessenger = MutableSharedFlow<MediaAction.MusicDataAction>()
    val musicMessenger = _musicMessenger.asSharedFlow()

    private val _noteMessenger = MutableSharedFlow<MediaAction.NoteDataAction>()
    val noteMessenger = _noteMessenger.asSharedFlow()

    private val _galleryMessenger = MutableSharedFlow<MediaAction.GalleryDataAction>()
    val galleryMessenger = _galleryMessenger.asSharedFlow()

    protected fun sendMusicAction(musicDataAction: MediaAction.MusicDataAction) {
        execute {
            _musicMessenger.emit(musicDataAction)
        }
    }

    protected fun sendNoteAction(musicDataAction: MediaAction.NoteDataAction) {
        execute {
            _noteMessenger.emit(musicDataAction)
        }
    }

    protected fun sendGalleryAction(musicDataAction: MediaAction.GalleryDataAction) {
        execute {
            _galleryMessenger.emit(musicDataAction)
        }
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