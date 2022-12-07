package com.protone.gallery.viewModel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.protone.common.R
import com.protone.common.baseType.*
import com.protone.component.database.MediaAction
import com.protone.component.database.dao.DatabaseBridge
import com.protone.common.entity.GalleryBucket
import com.protone.common.entity.GalleryMedia
import com.protone.common.utils.ALL_GALLERY
import com.protone.common.utils.TAG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext

class GalleryFragmentViewModel : ViewModel() {

    private val _fragFlow = MutableSharedFlow<FragEvent>()
    val fragEvent get() = _fragFlow.asSharedFlow()

    sealed class FragEvent {
        object SelectAll : FragEvent()
        object OnActionBtn : FragEvent()
        object IntoBox : FragEvent()

        data class AddGalleryBucket(val name: String, val list: MutableList<GalleryMedia>) :
            FragEvent()

        data class OnNewGalleryBucket(val pairs: Pair<Uri, Array<String>>) : FragEvent()
        data class OnGalleryRemoved(val pairs: Pair<Uri, Array<String>>) : FragEvent()

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
        ALL_GALLERY
    } else rightGallery

    fun getBucket(bucket: String) = Pair(
        if ((getGallery(bucket)?.size ?: 0) > 0) {
            getGallery(bucket)?.get(0)?.uri ?: Uri.EMPTY
        } else Uri.EMPTY,
        arrayOf(bucket, (getGallery(bucket)?.size ?: 0).toString())
    )

    fun onTargetGallery(bucket: String): Boolean {
        return bucket == rightGallery || rightGallery == ALL_GALLERY
    }

    fun sortData(combine: Boolean) = viewModelScope.launchDefault {
        galleryMap[ALL_GALLERY] = mutableListOf()
        DatabaseBridge.instance.galleryDAOBridge.run {
            observeGallery()
            val signedMedias =
                (if (combine) getAllSignedMedia() else getAllMediaByType(isVideo))
                        as MutableList<GalleryMedia>?
            if (signedMedias == null) {
                R.string.none.getString().toast()
                return@launchDefault
            }
            galleryMap[ALL_GALLERY] = signedMedias
            sendEvent(
                FragEvent.OnNewGalleryBucket(
                    Pair(
                        if (signedMedias.size > 0) signedMedias[0].uri else Uri.EMPTY,
                        arrayOf(ALL_GALLERY, signedMedias.size.toString())
                    )
                )
            )
            (if (combine) getAllGallery() else getAllGallery(isVideo))?.forEach {
                galleryMap[it] =
                    ((if (combine) getAllMediaByGallery(it) else getAllMediaByGallery(it, isVideo))
                            as MutableList<GalleryMedia>)
                        .also { list ->
                            sendEvent(
                                FragEvent.OnNewGalleryBucket(
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
        DatabaseBridge.instance
            .galleryDAOBridge
            .insertGalleryBucketCB(GalleryBucket(name, isVideo)) { re, reName ->
                if (re) {
                    if (!isLock) {
                        sendEvent(
                            FragEvent.OnNewGalleryBucket(
                                (Pair(
                                    Uri.EMPTY,
                                    arrayOf(reName, "PRIVATE")
                                ))
                            )
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
        viewModelScope.launchIO {
            DatabaseBridge.instance.galleryDAOBridge.run {
                getGalleryBucket(bucket)?.let { deleteGalleryBucketAsync(it) }
            }
        }
    }

    fun attachFragEvent(onAttach: (MutableSharedFlow<FragEvent>) -> Unit) {
        onAttach.invoke(_fragFlow)
    }

    private fun sortPrivateData(signedMedias: MutableList<GalleryMedia>) {
        viewModelScope.launchDefault {
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
                    sendEvent(
                        FragEvent.OnNewGalleryBucket(
                            Pair(
                                Uri.EMPTY,
                                arrayOf(it.type, "PRIVATE")
                            )
                        )
                    )
                    galleryMap[it.type] = mutableListOf()
                }
            isDataSorted = true
        }
    }

    private fun observeGallery() {
        viewModelScope.launchDefault {
            Log.d(TAG, "observeGallery: launch")
            fun sortDeleteMedia(
                media: GalleryMedia,
                map: MutableMap<String?, MutableList<GalleryMedia>>
            ) {
                if (map[media.bucket]?.remove(media) == true
                    && (map[media.bucket]?.size ?: 0) <= 0
                ) {
                    map.remove(media.bucket)
                }
                map[ALL_GALLERY]?.remove(media)
            }

            suspend fun insertNewMedia(
                map: MutableMap<String?, MutableList<GalleryMedia>>,
                media: GalleryMedia
            ) {
                if (map[media.bucket] == null) {
                    map[media.bucket] = mutableListOf<GalleryMedia>().also { it.add(media) }
                    FragEvent.OnNewGalleryBucket(
                        Pair(
                            media.uri,
                            arrayOf(media.bucket, map[media.bucket]?.size.toString())
                        )
                    ).let { sendEvent(it) }
                } else map[media.bucket]?.add(media)
            }

            suspend fun onGalleryMediaInserted(
                isVideo: Boolean,
                galleryMedia: GalleryMedia,
                map: MutableMap<String?, MutableList<GalleryMedia>>,
                gallery: String
            ) {
                if (galleryMedia.isVideo != isVideo) return
                insertNewMedia(map, galleryMedia)
                map[gallery]?.add(galleryMedia)
                sendEvent(FragEvent.OnMediaInserted(galleryMedia))
            }

            suspend fun onGalleryMediaUpdated(
                isVideo: Boolean,
                galleryMedia: GalleryMedia,
                map: MutableMap<String?, MutableList<GalleryMedia>>,
                gallery: String
            ) {
                if (galleryMedia.isVideo != isVideo) return
                map[gallery]?.first { sortMedia -> galleryMedia.uri == sortMedia.uri }
                    ?.let { sortedMedia ->
                        val allIndex = map[gallery]?.indexOf(sortedMedia)
                        if (allIndex != null && allIndex != -1) {
                            map[gallery]?.set(allIndex, galleryMedia)
                            val index = map[sortedMedia.bucket]?.indexOf(sortedMedia)
                            if (sortedMedia.bucket != galleryMedia.bucket) {
                                map[sortedMedia.bucket]?.remove(sortedMedia)
                                insertNewMedia(map, galleryMedia)
                                sendEvent(FragEvent.OnMediaInserted(galleryMedia))
                                return@let
                            } else if (index != null && index != -1) {
                                map[sortedMedia.bucket]?.set(index, sortedMedia)
                            }
                            sendEvent(FragEvent.OnMediaUpdated(galleryMedia))
                        }
                    }
            }

            DatabaseBridge.instance.galleryMessenger.bufferCollect {
                Log.d(TAG, "observeGallery: $it")
                while (!isDataSorted) delay(200)
                Log.d(TAG, "observeGallery2: $it")
                when (it) {
                    is MediaAction.GalleryDataAction.OnGalleryMediaDeleted -> {
                        if (it.media.isVideo != isVideo) return@bufferCollect
                        sortDeleteMedia(it.media, galleryMap)
                        sendEvent(FragEvent.OnMediaDeleted(it.media))
                    }
                    is MediaAction.GalleryDataAction.OnGalleryMediasInserted -> it.medias.forEach { media ->
                        onGalleryMediaInserted(isVideo, media, galleryMap, ALL_GALLERY)
                    }
                    is MediaAction.GalleryDataAction.OnGalleryMediaInserted -> {
                        onGalleryMediaInserted(isVideo, it.media, galleryMap, ALL_GALLERY)
                    }
                    is MediaAction.GalleryDataAction.OnGalleryMediasUpdated -> it.medias.forEach { media ->
                        onGalleryMediaUpdated(isVideo, media, galleryMap, ALL_GALLERY)
                    }
                    is MediaAction.GalleryDataAction.OnGalleryMediaUpdated -> {
                        onGalleryMediaUpdated(isVideo, it.media, galleryMap, ALL_GALLERY)
                    }
                    is MediaAction.GalleryDataAction.OnGalleryDeleted -> {
                        galleryMap.remove(it.gallery)?.let { _ ->
                            sendEvent(
                                FragEvent.OnGalleryRemoved(
                                    Pair(Uri.EMPTY, arrayOf(it.gallery))
                                )
                            )
                        }
                    }
                    is MediaAction.GalleryDataAction.OnGalleryBucketInserted -> {
                        TODO("OnGalleryBucketInserted")
                    }
                    is MediaAction.GalleryDataAction.OnGalleryBucketDeleted -> {
                        TODO("OnGalleryBucketDeleted")
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