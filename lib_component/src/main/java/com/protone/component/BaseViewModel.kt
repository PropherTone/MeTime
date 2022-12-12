package com.protone.component

import androidx.lifecycle.ViewModel
import com.protone.common.baseType.bufferCollect
import com.protone.component.database.MediaAction
import com.protone.component.database.dao.DatabaseBridge

abstract class BaseViewModel : ViewModel() {

    interface ViewEvent

    val galleryDAO by lazy { DatabaseBridge.instance.galleryDAOBridge }
    val musicDAO by lazy { DatabaseBridge.instance.musicDAOBridge }
    val noteDAO by lazy { DatabaseBridge.instance.noteDAOBridge }

    fun shutDownDataBase() {
        DatabaseBridge.instance.shutdownNow()
    }

    suspend inline fun observeGalleryData(crossinline callBack: suspend (MediaAction.GalleryDataAction) -> Unit) {
        DatabaseBridge.instance.galleryMessenger.bufferCollect { callBack(it) }
    }

    suspend inline fun observeMusicData(crossinline callBack: suspend (MediaAction.MusicDataAction) -> Unit) {
        DatabaseBridge.instance.musicMessenger.bufferCollect { callBack(it) }
    }

    suspend inline fun observeNoteDate(crossinline callBack: suspend (MediaAction.NoteDataAction) -> Unit) {
        DatabaseBridge.instance.noteMessenger.bufferCollect { callBack(it) }
    }
}