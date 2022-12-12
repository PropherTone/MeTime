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
import java.util.stream.Collectors

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

        sealed class MediaEvent(val galleryMedia: GalleryMedia) : FragEvent()
        data class OnMediaDeleted(val media: GalleryMedia) : MediaEvent(media)
        data class OnMediaInserted(val media: GalleryMedia) : MediaEvent(media)
        data class OnMediaUpdated(val media: GalleryMedia) : MediaEvent(media)

        data class OnMediasDeleted(val galleryMedia: List<GalleryMedia>) : FragEvent()
        data class OnMediasInserted(val galleryMedia: List<GalleryMedia>) : FragEvent()
        data class OnMediasUpdated(val galleryMedia: List<GalleryMedia>) : FragEvent()
    }

    private val pool = EventCachePool.get<FragEvent>(duration = 500L).apply {

        suspend fun buildEvent(data: List<FragEvent>) = data.first().also { fragEvent ->
            if (fragEvent !is FragEvent.MediaEvent) return@also
            data.associate { (it as FragEvent.MediaEvent).galleryMedia.bucket to listOf(it.galleryMedia) }
                .run {
                    keys.forEach { galleryName ->
                        galleryData.find { it.name == galleryName }.also { gallery ->
                            if (gallery != null) {
                                gallery.size = getGallerySize(galleryName)
                                gallery.uri = getNewestMedia(galleryName)
                                sendEvent(FragEvent.OnGalleryUpdated(gallery))
                            } else {
                                Gallery(
                                    galleryName,
                                    getGallerySize(galleryName),
                                    getNewestMedia(galleryName)
                                ).also { target ->
                                    target.cacheAndNotice()
                                }
                            }
                        }
                        this[galleryName]?.let {
                            sendEvent(FragEvent.OnMediasInserted(it))
                        }
                    }
                }
        }

        suspend fun updateGallery(gallery: Gallery) {
            val all = getGallerySize(gallery.name)
                .takeIf { size ->
                    size != gallery.size
                }?.let { size ->
                    gallery.size = size
                    gallery.itemState = Gallery.ItemState.SIZE_CHANGED
                    true
                } ?: false
                    && getNewestMedia(gallery.name)
                .takeIf { uri ->
                    uri != gallery.uri
                }?.let { uri ->
                    gallery.uri = uri
                    gallery.itemState = Gallery.ItemState.URI_CHANGED
                    true
                } ?: false
            if (all) gallery.itemState = Gallery.ItemState.ALL_CHANGED
            sendEvent(FragEvent.OnGalleryUpdated(gallery))
        }

        handleEvent {
            if (it.isNotEmpty()) when (it.first()) {
                is FragEvent.OnMediaDeleted, is FragEvent.OnMediaInserted -> {
                    buildEvent(it)
                }
                is FragEvent.OnGalleryUpdated -> {
                    it.distinct().forEach { event ->
                        (event as FragEvent.OnGalleryUpdated).gallery.also { gallery ->
                            updateGallery(gallery)
                        }
                    }
                    galleryData.find { gallery -> gallery.name == ALL_GALLERY }?.let { gallery ->
                        updateGallery(gallery)
                    }
                }
                else -> Unit
            }
        }
    }

    fun attachEvent() {
        onAttach?.invoke(_fragFlow)
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
        galleryDAO.insertGalleryBucketCB(GalleryBucket(name, isVideo)) { re, _ ->
            if (!re) R.string.failed_msg.getString().toast()
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
            suspend fun sortDeleteMedia(media: GalleryMedia) {
                if (media.isVideo != isVideo) return
                galleryData.find { it.name == media.bucket }?.let {
                    sendEvent(FragEvent.OnGalleryUpdated(it), false)
                }
                if (media.bucket != rightGallery) return
                sendEvent(FragEvent.OnMediaDeleted(media), false)
            }

            suspend fun onGalleryMediaInserted(media: GalleryMedia) {
                if (media.isVideo != isVideo) return
                galleryData.find { it.name == media.bucket }?.let {
                    sendEvent(FragEvent.OnGalleryUpdated(it), false)
                }
                if (media.bucket != rightGallery) return
                sendEvent(FragEvent.OnMediaInserted(media), false)
            }

            suspend fun onGalleryMediaUpdated(media: GalleryMedia) {
                if (media.isVideo != isVideo && media.bucket != rightGallery) return
                sendEvent(FragEvent.OnMediaUpdated(media))
            }

            suspend fun sendGalleryRemoved(gallery: Gallery) {
                galleryData.find { it.name == ALL_GALLERY }.let {
                    if (it != null) {
                        it.size = it.size - gallery.size
                        sendEvent(FragEvent.OnGalleryUpdated(it))
                    }
                    galleryData.remove(gallery)
                    sendEvent(FragEvent.OnGalleryRemoved(gallery))
                }
            }

            observeGalleryData {
                while (!isDataSorted) delay(200)
                when (it) {
                    is MediaAction.GalleryDataAction.OnGalleryMediaDeleted -> {
                        sortDeleteMedia(it.media)
                    }
                    is MediaAction.GalleryDataAction.OnGalleryMediasDeleted -> it.medias.forEach { media ->
                        sortDeleteMedia(media)
                    }
                    is MediaAction.GalleryDataAction.OnGalleryMediasInserted -> it.medias.forEach { media ->
                        onGalleryMediaInserted(media)
                    }
                    is MediaAction.GalleryDataAction.OnGalleryMediaInserted -> {
                        onGalleryMediaInserted(it.media)
                    }
                    is MediaAction.GalleryDataAction.OnGalleryMediasUpdated -> it.medias.forEach { media ->
                        onGalleryMediaUpdated(media)
                    }
                    is MediaAction.GalleryDataAction.OnGalleryMediaUpdated -> {
                        onGalleryMediaUpdated(it.media)
                    }
                    is MediaAction.GalleryDataAction.OnGalleryDeleted -> {
                        galleryData.find { gallery -> gallery.name == it.gallery }?.let { gallery ->
                            sendGalleryRemoved(gallery)
                        }
                    }
                    is MediaAction.GalleryDataAction.OnGalleryBucketInserted -> {
                        if (!isLock) {
                            Gallery(it.galleryBucket.type, 0, null).cacheAndNotice()
                        } else {
                            R.string.locked.getString().toast()
                        }
                    }
                    is MediaAction.GalleryDataAction.OnGalleryBucketDeleted -> {
                        galleryData.find { gallery ->
                            gallery.name == it.galleryBucket.type
                        }?.let { gallery ->
                            sendGalleryRemoved(gallery)
                        }
                    }
                    else -> Unit
                }
            }
        }
    }

    suspend fun sendEvent(fragEvent: FragEvent, immediate: Boolean = true) {
        if (immediate) _fragFlow.emit(fragEvent)
        else pool.holdEvent(fragEvent)
    }

    suspend fun insertNewMedias(gallery: String, list: MutableList<GalleryMedia>) {
        withDefaultContext {
            TODO("insertNewMedias")
        }
    }


}