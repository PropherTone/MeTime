package com.protone.component

import androidx.lifecycle.ViewModel
import com.protone.common.baseType.bufferCollect
import com.protone.common.baseType.mutableBufferCollect
import com.protone.component.database.MediaAction
import com.protone.component.database.dao.DatabaseBridge
import kotlinx.coroutines.CoroutineScope

abstract class BaseViewModel : ViewModel() {

    interface ViewEvent

    val galleryDAO by lazy { DatabaseBridge.instance.galleryDAOBridge }
    val musicDAO by lazy { DatabaseBridge.instance.musicDAOBridge }
    val noteDAO by lazy { DatabaseBridge.instance.noteDAOBridge }

    fun shutDownDataBase() {
        DatabaseBridge.instance.shutdownNow()
    }

    suspend inline fun observeGalleryData(crossinline callBack: suspend (MediaAction.GalleryDataAction) -> Unit) {
        DatabaseBridge.instance.galleryMessenger.bufferCollect(callBack)
    }

    suspend inline fun observeMusicData(crossinline callBack: suspend (MediaAction.MusicDataAction) -> Unit) {
        DatabaseBridge.instance.musicMessenger.bufferCollect(callBack)
    }

    suspend inline fun observeNoteDate(crossinline callBack: suspend (MediaAction.NoteDataAction) -> Unit) {
        DatabaseBridge.instance.noteMessenger.bufferCollect(callBack)
    }

    suspend inline fun observeGalleryDataMutable(
        coroutineScope: CoroutineScope,
        crossinline callBack: suspend (MediaAction.GalleryDataAction) -> Unit
    ) = DatabaseBridge.instance.galleryMessenger.mutableBufferCollect(coroutineScope, callBack)
    
    suspend inline fun observeMusicDataMutable(
        coroutineScope: CoroutineScope,
        crossinline callBack: suspend (MediaAction.MusicDataAction) -> Unit
    ) = DatabaseBridge.instance.musicMessenger.mutableBufferCollect(coroutineScope, callBack)

    suspend inline fun observeNoteDateMutable(
        coroutineScope: CoroutineScope,
        crossinline callBack: suspend (MediaAction.NoteDataAction) -> Unit
    ) = DatabaseBridge.instance.noteMessenger.mutableBufferCollect(coroutineScope, callBack)

}