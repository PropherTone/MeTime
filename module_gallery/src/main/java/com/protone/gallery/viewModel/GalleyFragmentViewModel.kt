package com.protone.gallery.viewModel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.protone.common.R
import com.protone.common.baseType.*
import com.protone.common.entity.Gallery
import com.protone.common.entity.GalleryBucket
import com.protone.common.entity.GalleryMedia
import com.protone.common.utils.ALL_GALLERY
import com.protone.common.utils.TAG
import com.protone.component.BaseViewModel
import com.protone.component.database.MediaAction
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class GalleryFragmentViewModel : BaseViewModel() {

    private val _fragFlow = MutableSharedFlow<FragEvent>()
    val fragEvent get() = _fragFlow.asSharedFlow()

    var isVideo: Boolean = false
    var isLock: Boolean = false
    var combine: Boolean = false
    var onAttach: ((MutableSharedFlow<FragEvent>) -> Unit)? = null

    var rightGallery: String = ""
    var isBucketShowUp = true
    private var isDataSorted = false

    private val galleryMap = mutableMapOf<String?, MutableList<GalleryMedia>>()
    private val galleryData = mutableListOf<Gallery>()

    sealed class FragEvent {
        object SelectAll : FragEvent()
        object OnActionBtn : FragEvent()
        object IntoBox : FragEvent()

        data class AddGalleryBucket(val name: String, val list: MutableList<GalleryMedia>) :
            FragEvent()

        data class OnNewGalleryBucket(val gallery: Gallery) : FragEvent()
        data class OnGalleryRemoved(val gallery: Gallery) : FragEvent()

        data class OnSelect(val galleryMedia: MutableList<GalleryMedia>) : FragEvent()

        data class OnMediaDeleted(val galleryMedia: GalleryMedia) : FragEvent()
        data class OnMediaInserted(val galleryMedia: GalleryMedia) : FragEvent()
        data class OnMediaUpdated(val galleryMedia: GalleryMedia) : FragEvent()
    }

    suspend fun getGallery(gallery: String) = withIOContext {
        galleryDAO.run {
            when {
                combine && gallery == ALL_GALLERY -> getAllSignedMedia()
                combine -> getAllMediaByGallery(gallery)
                gallery == ALL_GALLERY -> getAllMediaByType(isVideo)
                else -> getAllMediaByGallery(gallery, isVideo)
            }
        }
    }

    private suspend fun getGallerySize(name: String): Int = galleryDAO.run {
        if (combine) getMediaCountByGallery(name) else getMediaCountByGallery(name, isVideo)
    }

    private suspend fun getGallerySize(): Int = galleryDAO.run {
        if (combine) getMediaCount() else getMediaCount(isVideo)
    }

    private suspend fun getAllGalleryBucket() = galleryDAO.run {
        if (combine) getAllGalleryBucket() else getAllGalleryBucket(isVideo)
    }

    fun getGalleryName() = if (rightGallery == "") ALL_GALLERY else rightGallery

    fun getBucket(bucket: String) = galleryData.find { it.name == bucket }

    fun onTargetGallery(bucket: String): Boolean {
        return bucket == rightGallery || rightGallery == ALL_GALLERY
    }

    private suspend fun Gallery.cacheAndNotice() {
        galleryData.add(this)
        sendEvent(FragEvent.OnNewGalleryBucket(this))
    }

    fun sortData() = viewModelScope.launchDefault {
        galleryDAO.run {
            observeGallery()
            val galleries =
                (if (combine) getAllGallery() else getAllGallery(isVideo)) as MutableList<String>?
            if (galleries == null) {
                R.string.none.getString().toast()
                return@launchDefault
            }

            Gallery(
                ALL_GALLERY,
                getGallerySize(),
                if (combine) getNewestMedia() else getNewestMedia(isVideo)
            ).cacheAndNotice()

            galleries.forEach {
                Gallery(
                    it,
                    getGallerySize(it),
                    if (combine) getNewestMediaInGallery(it)
                    else getNewestMediaInGallery(it, isVideo)
                ).cacheAndNotice()
            }
            if (!isLock) sortPrivateData() else isDataSorted = true
        }
    }

    private fun sortPrivateData() {
        viewModelScope.launchDefault {
            getAllGalleryBucket()?.forEach {
                Gallery(it.type, 0, null).cacheAndNotice()
            }
            isDataSorted = true
        }
    }

    fun addBucket(name: String) {
        galleryDAO.insertGalleryBucketCB(GalleryBucket(name, isVideo)) { re, reName ->
            if (re) {
                if (!isLock) {
                    sendEvent(FragEvent.OnNewGalleryBucket(Gallery(reName, 0, null)))
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
            galleryDAO.run {
                getGalleryBucket(bucket)?.let { deleteGalleryBucketAsync(it) }
            }
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
                        Gallery(media.bucket, getGallerySize(media.bucket), media.uri)
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

            observeGalleryData {
                Log.d(TAG, "observeGallery: $it")
                while (!isDataSorted) delay(200)
                Log.d(TAG, "observeGallery2: $it")
                when (it) {
                    is MediaAction.GalleryDataAction.OnGalleryMediaDeleted -> {
                        if (it.media.isVideo != isVideo) return@observeGalleryData
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
                            sendEvent(FragEvent.OnGalleryRemoved(Gallery(it.gallery, 0, null)))
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

    suspend fun insertNewMedias(gallery: String, list: MutableList<GalleryMedia>) {
        withDefaultContext {
            TODO("insertNewMedias")
        }
    }


}