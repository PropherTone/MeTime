package com.protone.gallery.viewModel

import androidx.lifecycle.viewModelScope
import com.protone.common.R
import com.protone.common.baseType.*
import com.protone.common.entity.Gallery
import com.protone.common.entity.GalleryBucket
import com.protone.common.entity.GalleryMedia
import com.protone.common.utils.ALL_GALLERY
import com.protone.common.utils.EventCachePool
import com.protone.component.BaseViewModel
import com.protone.component.database.MediaAction
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flow

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

    private val galleryData = mutableListOf<Gallery>()

    sealed class FragEvent {
        object SelectAll : FragEvent()
        object OnActionBtn : FragEvent()
        object IntoBox : FragEvent()

        data class AddToGalleryBucket(val name: String, val list: MutableList<GalleryMedia>) :
            FragEvent()

        data class OnNewGallery(val gallery: Gallery) : FragEvent()
        data class OnGalleryRemoved(val gallery: Gallery) : FragEvent()
        data class OnGalleryUpdated(val gallery: Gallery) : FragEvent()

        data class OnNewGalleries(val gallery: List<Gallery>) : FragEvent()
        data class OnGalleriesRemoved(val gallery: List<Gallery>) : FragEvent()
        data class OnGalleriesUpdated(val gallery: List<Gallery>) : FragEvent()

        data class OnSelect(val galleryMedia: MutableList<GalleryMedia>) : FragEvent()

        data class OnMediaDeleted(val galleryMedia: GalleryMedia) : FragEvent()
        data class OnMediaInserted(val galleryMedia: GalleryMedia) : FragEvent()
        data class OnMediaUpdated(val galleryMedia: GalleryMedia) : FragEvent()

        data class OnMediasDeleted(val galleryMedia: List<GalleryMedia>) : FragEvent()
        data class OnMediasInserted(val galleryMedia: List<GalleryMedia>) : FragEvent()
        data class OnMediasUpdated(val galleryMedia: List<GalleryMedia>) : FragEvent()
    }

    private val pool = EventCachePool.get<FragEvent>(duration = 500L).apply {
        viewModelScope.launchDefault {
            suspend fun buildEvent(
                increase: Boolean,
                data: List<FragEvent>,
                galleries: List<Gallery>
            ): FragEvent? {
                val bucket =
                    if (increase) (data.first() as FragEvent.OnMediaInserted).galleryMedia.bucket
                    else (data.first() as FragEvent.OnMediaDeleted).galleryMedia.bucket
                return galleries.find { gallery -> gallery.name == bucket }?.let { gallery ->
                    if (increase) gallery.size += data.size else gallery.size -= data.size
                    gallery.uri = getNewestMedia(bucket)
                    FragEvent.OnGalleryUpdated(gallery)
                }
            }
            handlerEvent {
                if (it.isNotEmpty()) when (it.first()) {
                    is FragEvent.OnMediaDeleted -> {
                        buildEvent(false, it, galleryData)
                    }
                    is FragEvent.OnMediaInserted -> {
                        buildEvent(true, it, galleryData)
                    }
                    else -> null
                }?.let { event -> sendEvent(event, true) }
            }
        }
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

    private suspend fun getNewestMedia(gallery: String) = galleryDAO.run {
        if (combine) getNewestMediaInGallery(gallery)
        else getNewestMediaInGallery(gallery, isVideo)
    }

    fun getGalleryName() = if (rightGallery == "") ALL_GALLERY else rightGallery

    fun getBucket(bucket: String) = galleryData.find { it.name == bucket }

    private suspend fun Gallery.cacheAndNotice() {
        galleryData.add(this)
        sendEvent(FragEvent.OnNewGallery(this), true)
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
                Gallery(it, getGallerySize(it), getNewestMedia(it)).cacheAndNotice()
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
                    Gallery(reName, 0, null).cacheAndNotice()
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
            suspend fun sortDeleteMedia(
                media: GalleryMedia,
                rightGallery: String
            ) {
                if (media.isVideo != isVideo && media.bucket != rightGallery) return
                sendEvent(FragEvent.OnMediaDeleted(media), false)
            }

            suspend fun onGalleryMediaInserted(
                isVideo: Boolean,
                media: GalleryMedia,
                rightGallery: String
            ) {
                if (media.isVideo != isVideo && media.bucket != rightGallery) return
                sendEvent(FragEvent.OnMediaInserted(media), false)
            }

            suspend fun onGalleryMediaUpdated(
                isVideo: Boolean,
                media: GalleryMedia,
                rightGallery: String
            ) {
                if (media.isVideo != isVideo && media.bucket != rightGallery) return
                sendEvent(FragEvent.OnMediaUpdated(media), true)
            }

            observeGalleryData {
                while (!isDataSorted) delay(200)
                when (it) {
                    is MediaAction.GalleryDataAction.OnGalleryMediaDeleted -> {
                        sortDeleteMedia(it.media, rightGallery)
                    }
                    is MediaAction.GalleryDataAction.OnGalleryMediasInserted -> it.medias.forEach { media ->
                        onGalleryMediaInserted(isVideo, media, rightGallery)
                    }
                    is MediaAction.GalleryDataAction.OnGalleryMediaInserted -> {
                        onGalleryMediaInserted(isVideo, it.media, rightGallery)
                    }
                    is MediaAction.GalleryDataAction.OnGalleryMediasUpdated -> it.medias.forEach { media ->
                        onGalleryMediaUpdated(isVideo, media, rightGallery)
                    }
                    is MediaAction.GalleryDataAction.OnGalleryMediaUpdated -> {
                        onGalleryMediaUpdated(isVideo, it.media, rightGallery)
                    }
                    is MediaAction.GalleryDataAction.OnGalleryDeleted -> {
                        galleryData.find { gallery -> gallery.name == it.gallery }?.let { gallery ->
                            galleryData.remove(gallery)
                            sendEvent(FragEvent.OnGalleryRemoved(gallery), true)
                        }
                    }
                    is MediaAction.GalleryDataAction.OnGalleryBucketInserted -> {
                        Gallery(it.galleryBucket.type, 0, null, true).cacheAndNotice()
                    }
                    is MediaAction.GalleryDataAction.OnGalleryBucketDeleted -> {
                        galleryData.find { gallery ->
                            gallery.name == it.galleryBucket.type
                        }?.let { gallery ->
                            galleryData.remove(gallery)
                            sendEvent(FragEvent.OnGalleryRemoved(gallery), true)
                        }
                    }
                    else -> Unit
                }
            }
        }
    }

    suspend fun sendEvent(fragEvent: FragEvent, immediate: Boolean) {
        if (immediate) _fragFlow.emit(fragEvent)
        else pool.holdEvent(fragEvent)
    }

    suspend fun insertNewMedias(gallery: String, list: MutableList<GalleryMedia>) {
        withDefaultContext {
            TODO("insertNewMedias")
        }
    }


}