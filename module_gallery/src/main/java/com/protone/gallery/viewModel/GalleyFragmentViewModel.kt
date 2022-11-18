package com.protone.gallery.viewModel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.protone.common.R
import com.protone.common.baseType.bufferCollect
import com.protone.common.baseType.getString
import com.protone.common.baseType.toast
import com.protone.common.database.MediaAction
import com.protone.common.database.dao.DatabaseBridge
import com.protone.common.entity.GalleryBucket
import com.protone.common.entity.GalleryMedia
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GalleryFragmentViewModel : ViewModel() {

    private val _fragFlow = MutableSharedFlow<FragEvent>()
    val fragEvent get() = _fragFlow.asSharedFlow()

    sealed class FragEvent {
        object SelectAll : FragEvent()
        object OnActionBtn : FragEvent()
        object IntoBox : FragEvent()

        data class AddBucket(val name: String, val list: MutableList<GalleryMedia>) : FragEvent()
        data class OnNewBucket(val pairs: Pair<Uri, Array<String>>) : FragEvent()

        data class OnSelect(val galleryMedia: MutableList<GalleryMedia>) : FragEvent()

        data class OnMediaDeleted(val galleryMedia: GalleryMedia) : FragEvent()
        data class OnMediaInserted(val galleryMedia: GalleryMedia) : FragEvent()
        data class OnMediaUpdated(val galleryMedia: GalleryMedia) : FragEvent()
    }

    var rightGallery: String = ""

    var isVideo: Boolean = false

    var isLock: Boolean = false
    var isBucketShowUp = true
    private var isDataSorted = false

    private val galleryMap = mutableMapOf<String?, MutableList<GalleryMedia>>()

    fun getGallery(gallery: String) = galleryMap[gallery]

    fun getGalleryName() = if (rightGallery == "") {
        R.string.all_gallery.getString()
    } else rightGallery

    fun getBucket(bucket: String) = Pair(
        if ((getGallery(bucket)?.size ?: 0) > 0) {
            getGallery(bucket)?.get(0)?.uri ?: Uri.EMPTY
        } else Uri.EMPTY,
        arrayOf(bucket, (getGallery(bucket)?.size ?: 0).toString())
    )

    fun onTargetGallery(bucket: String): Boolean {
        return bucket == rightGallery || rightGallery == R.string.all_gallery.getString()
    }

    fun sortData(combine: Boolean) = viewModelScope.launch(Dispatchers.Default) {
        galleryMap[R.string.all_gallery.getString()] = mutableListOf()
        DatabaseBridge.instance
            .galleryDAOBridge
            .run {
                observeGallery()
                val signedMedias =
                    (if (combine) getAllSignedMedia() else getAllMediaByType(isVideo)) as MutableList<GalleryMedia>?
                if (signedMedias == null) {
                    R.string.none.getString().toast()
                    return@launch
                }
                signedMedias.let {
                    galleryMap[R.string.all_gallery.getString()] = it
                    sendEvent(
                        FragEvent.OnNewBucket(
                            Pair(
                                if (signedMedias.size > 0) signedMedias[0].uri else Uri.EMPTY,
                                arrayOf(
                                    R.string.all_gallery.getString(),
                                    signedMedias.size.toString()
                                )
                            )
                        )
                    )
                }
                (if (combine) getAllGallery() else getAllGallery(isVideo))?.forEach {
                    galleryMap[it] =
                        ((if (combine) getAllMediaByGallery(it)
                        else getAllMediaByGallery(
                            it,
                            isVideo
                        )) as MutableList<GalleryMedia>).also { list ->
                            sendEvent(
                                FragEvent.OnNewBucket(
                                    Pair(
                                        if (list.size > 0) list[0].uri else Uri.EMPTY,
                                        arrayOf(it, list.size.toString())
                                    )
                                )
                            )
                        }
                }
                if (!isLock) sortPrivateData(signedMedias) else isDataSorted = true
            }
    }

    fun addBucket(name: String) {
        DatabaseBridge
            .instance
            .galleryDAOBridge
            .insertGalleryBucketCB(GalleryBucket(name, isVideo)) { re, reName ->
                if (re) {
                    if (!isLock) {
                        sendEvent(
                            FragEvent.OnNewBucket((Pair(Uri.EMPTY, arrayOf(reName, "PRIVATE"))))
                        )
                        galleryMap[reName] = mutableListOf()
                    } else {
                        R.string.locked.getString().toast()
                    }
                } else {
                    R.string.failed_msg.getString().toast()
                }
            }
    }

    fun deleteGalleryBucket(bucket: String) {
        viewModelScope.launch(Dispatchers.IO) {
            DatabaseBridge.instance.galleryDAOBridge.run {
                getGalleryBucket(bucket)?.let { deleteGalleryBucketAsync(it) }
            }
        }
    }

    fun attachFragEvent(onAttach: (MutableSharedFlow<FragEvent>) -> Unit) {
        onAttach.invoke(_fragFlow)
    }

    private fun sortPrivateData(signedMedias: MutableList<GalleryMedia>) {
        viewModelScope.launch(Dispatchers.Default) {
            signedMedias.forEach {
                it.type?.forEach { type ->
                    if (galleryMap[type] == null) {
                        galleryMap[type] = mutableListOf()
                    }
                    galleryMap[type]?.add(it)
                }
            }
            (DatabaseBridge.instance
                .galleryDAOBridge
                .getAllGalleryBucket(isVideo) as MutableList<GalleryBucket>?)
                ?.forEach {
                    sendEvent(FragEvent.OnNewBucket(Pair(Uri.EMPTY, arrayOf(it.type, "PRIVATE"))))
                    galleryMap[it.type] = mutableListOf()
                }
            isDataSorted = true
        }
    }

    private fun observeGallery() {
        viewModelScope.launch(Dispatchers.Default) {
            val allGallery = R.string.all_gallery.getString()
            fun sortDeleteMedia(
                media: GalleryMedia,
                map: MutableMap<String?, MutableList<GalleryMedia>>
            ) {
                if (map[media.bucket]?.remove(media) == true
                    && (map[media.bucket]?.size ?: 0) <= 0
                ) {
                    map.remove(media.bucket)
                }
                map[allGallery]?.remove(media)
            }

            suspend fun insertNewMedia(
                map: MutableMap<String?, MutableList<GalleryMedia>>,
                media: GalleryMedia
            ) {
                if (map[media.bucket] == null) {
                    map[media.bucket] = mutableListOf<GalleryMedia>().also { it.add(media) }
                    FragEvent.OnNewBucket(
                        Pair(
                            media.uri,
                            arrayOf(media.bucket, map[media.bucket]?.size.toString())
                        )
                    ).let { sendEvent(it) }
                } else map[media.bucket]?.add(media)
            }

            DatabaseBridge.instance.galleryMessenger.bufferCollect {
                while (!isDataSorted) delay(200)
                when (it) {
                    is MediaAction.GalleryDataAction.OnGalleryMediaDeleted -> {
                        if (it.media.isVideo != isVideo) return@bufferCollect
                        sortDeleteMedia(it.media, galleryMap)
                        sendEvent(FragEvent.OnMediaDeleted(it.media))
                    }
                    is MediaAction.GalleryDataAction.OnGalleryMediaInserted -> {
                        if (it.media.isVideo != isVideo) return@bufferCollect
                        insertNewMedia(galleryMap, it.media)
                        galleryMap[allGallery]?.add(it.media)
                        sendEvent(FragEvent.OnMediaInserted(it.media))
                    }
                    is MediaAction.GalleryDataAction.OnGalleryMediaUpdated -> {
                        if (it.media.isVideo != isVideo) return@bufferCollect
                        galleryMap[allGallery]?.first { sortMedia -> it.media.uri == sortMedia.uri }
                            ?.let { sortedMedia ->
                                val allIndex = galleryMap[allGallery]?.indexOf(sortedMedia)
                                if (allIndex != null && allIndex != -1) {
                                    galleryMap[allGallery]?.set(allIndex, it.media)
                                    val index = galleryMap[sortedMedia.bucket]?.indexOf(sortedMedia)
                                    if (sortedMedia.bucket != it.media.bucket) {
                                        galleryMap[sortedMedia.bucket]?.remove(sortedMedia)
                                        insertNewMedia(galleryMap, it.media)
                                        sendEvent(FragEvent.OnMediaInserted(it.media))
                                        return@let
                                    } else if (index != null && index != -1) {
                                        galleryMap[sortedMedia.bucket]?.set(index, sortedMedia)
                                    }
                                    sendEvent(FragEvent.OnMediaUpdated(it.media))
                                }
                            }
                    }
                    else -> Unit
                }
            }
        }
    }

    suspend fun sendEvent(fragEvent: FragEvent) {
        _fragFlow.emit(fragEvent)
    }

    suspend fun insertNewMedias(gallery: String, list: MutableList<GalleryMedia>) =
        withContext(Dispatchers.Default) {
            if (!isLock) {
                if (galleryMap[gallery] == null) {
                    galleryMap[gallery] = mutableListOf()
                }
                galleryMap[gallery]?.addAll(list)
            }
        }

}