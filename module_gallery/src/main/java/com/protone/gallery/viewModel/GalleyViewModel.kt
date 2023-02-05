package com.protone.gallery.viewModel

import androidx.lifecycle.viewModelScope
import com.protone.component.R
import com.protone.common.baseType.*
import com.protone.common.entity.Gallery
import com.protone.common.entity.Gallery.ItemState
import com.protone.common.entity.GalleryBucket
import com.protone.common.entity.GalleryMedia
import com.protone.common.utils.ALL_GALLERY
import com.protone.common.utils.EventCachePool
import com.protone.component.BaseViewModel
import com.protone.component.database.MediaAction
import com.protone.component.database.userConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.concurrent.LinkedBlockingDeque

class GalleryViewModel : BaseViewModel() {

    sealed class GalleryEvent {
        object OnSelectedMode : GalleryEvent()
        object ExitSelectedMode : GalleryEvent()
        data class OnNewGallery(val gallery: Gallery) : GalleryEvent()
        data class OnNewGalleries(val galleries: List<Gallery>) : GalleryEvent()
        data class OnGalleryRemoved(val gallery: Gallery) : GalleryEvent()
        data class OnGalleryUpdated(
            val gallery: Gallery,
            val itemState: ItemState = ItemState.ALL_CHANGED
        ) : GalleryEvent()
    }

    sealed class GalleryListEvent : GalleryEvent() {
        data class OnDrawerEvent(val isOpen: Boolean) : GalleryListEvent()
        object SelectAll : GalleryListEvent()
        object ExitSelect : GalleryListEvent()
        data class OnGallerySelected(
            val gallery: Gallery,
            val isVideo: Boolean,
            val combine: Boolean,
            val isDrawerOpen: Boolean
        ) : GalleryListEvent()

        sealed class MediaEvent(val galleryMedia: GalleryMedia) : GalleryListEvent()
        data class OnMediaDeleted(val media: GalleryMedia) : MediaEvent(media)
        data class OnMediaInserted(val media: GalleryMedia) : MediaEvent(media)
        data class OnMediaUpdated(val media: GalleryMedia) : MediaEvent(media)

        data class OnMediasInserted(val medias: List<GalleryMedia>) : GalleryListEvent()
    }

    private val _galleryFlow = MutableSharedFlow<GalleryEvent>()
    val galleryFlow = _galleryFlow.asSharedFlow()

    private val mailers = arrayOfNulls<MutableSharedFlow<GalleryListEvent>>(2)
    private fun getCurrentMailer() = mailers[rightMailer]

    private val galleryData = mutableListOf<Gallery>()
    private val galleryVideoData by lazy { mutableListOf<Gallery>() }
    private fun getGalleryData(isVideo: Boolean) =
        if (isVideo && !combine) galleryVideoData else galleryData

    fun getGalleryData() = if (isVideoGallery && !combine) galleryVideoData else galleryData

    val selectedMedias by lazy {
        MediaSelectedList().apply list@{
            var onSelectMode = false
            dataListener = object : MediaSelectedList.DataListener {
                override fun onAdded() {
                    if (!onSelectMode) {
                        viewModelScope.launchDefault {
                            sendBucketEvent(GalleryEvent.OnSelectedMode)
                        }
                        onSelectMode = true
                    }
                }

                override fun onCleared() {
                    if (onSelectMode) {
                        viewModelScope.launchDefault {
                            sendBucketEvent(GalleryEvent.ExitSelectedMode)
                        }
                        onSelectMode = false
                    }
                }
            }
        }
    }

    private var rightMediaGallery = ALL_GALLERY
    private var rightVideoGallery = ALL_GALLERY
    var rightGallery: String = ALL_GALLERY
        private set(value) {
            if (isVideoGallery) rightVideoGallery = value
            else rightMediaGallery = value
            field = value
        }
        get() {
            return if (isVideoGallery) rightVideoGallery else rightMediaGallery
        }

    private var rightMailer = 0
    private var isDataSorted = false
    private val combine = userConfig.combineGallery
    private val isVideoGallery: Boolean get() = rightMailer == 1
    private val isLock = userConfig.lockGallery.isNotEmpty()

    private val pool = EventCachePool.get<GalleryEvent>(duration = 400L).handleEvent { data ->
        if (data.isEmpty()) return@handleEvent
        when (data.first()) {
            is GalleryListEvent.OnMediaInserted -> data.first().also { event ->
                if (event !is GalleryListEvent.MediaEvent) return@also
                data.associate {
                    (it as GalleryListEvent.MediaEvent) to listOf(it.galleryMedia)
                }.run {
                    keys.forEach { event ->
                        event.galleryMedia.bucket.let { galleryName ->
                            galleryData.find { it.name == galleryName }.also { gallery ->
                                if (gallery != null)
                                    gallery.updateGallery(event.galleryMedia.isVideo)
                                else Gallery(
                                    galleryName,
                                    getGallerySize(galleryName, event.galleryMedia.isVideo),
                                    getNewestMedia(galleryName, event.galleryMedia.isVideo)
                                ).also { target -> target.cacheAndNotice(event.galleryMedia.isVideo) }
                            }
                        }
                        this[event]?.let {
                            sendListEvent(GalleryListEvent.OnMediasInserted(it))
                        }
                    }
                }
            }
            is GalleryEvent.OnGalleryUpdated -> data.distinct().forEach { event ->
                val isVideo = isVideoGallery
                (event as GalleryEvent.OnGalleryUpdated).gallery.updateGallery(isVideo)
                galleryData.find { gallery ->
                    gallery.name == ALL_GALLERY
                }?.updateGallery(isVideo)
            }
            else -> Unit
        }
    }

    fun sortData() {
        observeGallery()
        viewModelScope.launchDefault {
            if (combine) {
                sortData(false)
            } else {
                sortData(false)
                sortData(true)
            }
            if (!isLock) sortPrivateData() else isDataSorted = true
        }
    }

    fun deleteGalleryBucket(bucket: String) {
        viewModelScope.launchIO {
            galleryDAO.run {
                getGalleryBucket(bucket)?.let { deleteGalleryBucketAsync(it) }
            }
        }
    }

    fun selectAll() {
        viewModelScope.launchDefault {
            getCurrentMailer()?.emit(GalleryListEvent.SelectAll)
        }
    }

    fun exitSelect() {
        viewModelScope.launchDefault {
            sendListEvent(GalleryListEvent.ExitSelect)
        }
    }

    fun drawerStateChanged(isOpen: Boolean) {
        viewModelScope.launchDefault {
            sendListEvent(GalleryListEvent.OnDrawerEvent(isOpen))
        }
    }

    fun getSelectedMedias() = selectedMedias.toList()

    fun getBucket(bucket: String) = getGalleryData(isVideoGallery).find { it.name == bucket }

    fun getSelectedBucket() = getGalleryData(isVideoGallery).find { it.name == rightGallery }

    fun addBucket(name: String) {
        viewModelScope.launchIO {
            galleryDAO.apply {
                if (name.isEmpty()) {
                    R.string.enter.getString().toast()
                    return@launchIO
                }
                val galleries = getAllCheckedGallery(isVideoGallery)
                if (name == ALL_GALLERY && galleries?.contains(name) == true) {
                    com.protone.gallery.R.string.name_used.getString().toast()
                    return@launchIO
                }
                insertGalleryBucketCB(GalleryBucket(name)) { re, _ ->
                    if (!re) R.string.failed_msg.getString().toast()
                }
            }
        }
    }

    fun removeMediasFromCustomGallery(medias: List<GalleryMedia>): Boolean {
        return getSelectedBucket().takeIf { it != null && it.custom }?.let {
            galleryDAO.deleteMediasWithGalleryBucketAsync(medias, it.name)
            viewModelScope.launchDefault {
                medias.forEach { media ->
                    sendListEvent(GalleryListEvent.OnMediaDeleted(media))
                }
            }
            true
        } ?: false
    }

    fun generateMailer(isVideo: Boolean) = MutableSharedFlow<GalleryListEvent>().also {
        mailers[if (isVideo) 1 else 0] = it
    }.asSharedFlow()

    fun onGallerySelected(gallery: Gallery, isDrawerOpen: Boolean) {
        viewModelScope.launch {
            rightGallery = gallery.name
            sendListEvent(
                GalleryListEvent.OnGallerySelected(
                    gallery,
                    isVideoGallery,
                    combine,
                    isDrawerOpen
                )
            )
        }
    }

    fun onTabChanged(tabText: CharSequence): Boolean {
        val mailer = rightMailer
        when (tabText) {
            com.protone.gallery.R.string.photo.getString() -> rightMailer = 0
            com.protone.gallery.R.string.video.getString() -> rightMailer = 1
        }
        return mailer != rightMailer
    }

    suspend fun getRightGalleryMedias(): List<GalleryMedia>? = galleryDAO.run {
        if (rightGallery == ALL_GALLERY) {
            if (combine) getAllSignedMedia() else getAllMediaByType(isVideoGallery)
        } else getGalleryData().find { it.name == rightGallery }?.let {
            if (it.custom) getGalleryBucket(it.name)?.let { bucket ->
                if (combine) getGalleryMediasByBucket(bucket.galleryBucketId)
                else getGalleryMediasByBucket(bucket.galleryBucketId, isVideoGallery)
            } else null
        }
    } ?: galleryDAO.run {
        if (combine) getAllMediaByType(isVideoGallery)
        else getAllMediaByGallery(rightGallery, isVideoGallery)
    }

    private suspend fun sortData(isVideo: Boolean) {
        galleryDAO.run {
            val galleries = getAllCheckedGallery(isVideo) as MutableList<String>?
            if (galleries == null) {
                R.string.none.getString().toast()
                return
            }

            Gallery(
                ALL_GALLERY,
                getGallerySize(isVideo),
                getNewestMediaChecked(isVideo)
            ).cacheAndNotice(isVideo)

            galleries.forEach {
                Gallery(
                    it,
                    getGallerySize(it, isVideo),
                    getNewestMedia(it, isVideo)
                ).cacheAndNotice(isVideo)
            }
        }
    }

    private suspend fun sortPrivateData() {
        getAllGalleryBucket()?.forEach {
            Gallery(it.type, 0, null, custom = true).cacheAndNotice(true)
        }
        isDataSorted = true
    }

    private fun observeGallery() {
        viewModelScope.launchDefault {
            suspend fun sortDeleteMedia(media: GalleryMedia) {
                getGalleryData(media.isVideo).find { it.name == media.bucket }?.let {
                    sendBucketEvent(GalleryEvent.OnGalleryUpdated(it), false)
                }
                if (media.isVideo != isVideoGallery && media.bucket != rightGallery) return
                selectedMedias.remove(media)
                sendListEvent(GalleryListEvent.OnMediaDeleted(media), true)
            }

            suspend fun onGalleryMediaInserted(media: GalleryMedia) {
                if (media.isVideo != isVideoGallery) return
                getGalleryData(media.isVideo).find { it.name == media.bucket }?.let {
                    sendBucketEvent(GalleryEvent.OnGalleryUpdated(it), false)
                }
                if (media.bucket != rightGallery) return
                sendListEvent(GalleryListEvent.OnMediaInserted(media), false)
            }

            suspend fun onGalleryMediaUpdated(media: GalleryMedia) {
                if (media.isVideo != isVideoGallery && media.bucket != rightGallery) return
                sendListEvent(GalleryListEvent.OnMediaUpdated(media))
            }

            suspend fun sendGalleryRemoved(gallery: Gallery) {
                getGalleryData(isVideoGallery).find { it.name == ALL_GALLERY }.let {
                    it?.updateGallery(isVideoGallery)
                    galleryData.remove(gallery)
                    sendBucketEvent(GalleryEvent.OnGalleryRemoved(gallery))
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
                            Gallery(it.galleryBucket.type, 0, null, custom = true)
                                .cacheAndNotice(true)
                        } else R.string.locked.getString().toast()
                    }
                    is MediaAction.GalleryDataAction.OnGalleryBucketDeleted -> {
                        galleryData.find { gallery ->
                            gallery.name == it.galleryBucket.type
                        }?.let { gallery ->
                            sendGalleryRemoved(gallery)
                        }
                    }
                    is MediaAction.GalleryDataAction.OnMediaWithGalleryBucketMultiInserted ->
                        it.mediaWithGalleryBuckets
                            .map { mwb -> mwb.galleryBucketId }
                            .distinct()
                            .forEach { bucketId ->
                                galleryDAO.getGalleryBucket(bucketId)?.let { galleryBucket ->
                                    if (rightGallery == galleryBucket.type) {
                                        galleryDAO.getGalleryMediasByBucket(bucketId)
                                            ?.let { medias ->
                                                sendListEvent(
                                                    GalleryListEvent.OnMediasInserted(medias)
                                                )
                                            }
                                    }
                                }
                            }
                    else -> Unit
                }
            }
        }
    }

    private suspend fun Gallery.cacheAndNotice(isVideo: Boolean) {
        if (custom) {
            getGalleryData(true).add(this)
            getGalleryData(false).add(this)
        } else {
            getGalleryData(isVideo).add(this)
        }
        if (isVideoGallery == isVideo || custom)
            sendBucketEvent(GalleryEvent.OnNewGallery(this), true)
    }

    private suspend fun Gallery.updateGallery(isVideo: Boolean) {
        var itemState = ItemState.ALL_CHANGED
        val newSize = if (name == ALL_GALLERY) getGallerySize(isVideo)
        else getGallerySize(name, isVideo)
        newSize.takeIf { size ->
            if (size <= 0) {
                sendBucketEvent(GalleryEvent.OnGalleryRemoved(this))
                return
            }
            size != this.size
        }?.let { size ->
            this.size = size
            itemState = ItemState.SIZE_CHANGED
        }

        val media = if (name == ALL_GALLERY) getNewestMediaChecked(isVideo)
        else getNewestMedia(name, isVideo)
        media.takeIf { uri ->
            uri != this.uri
        }?.let { uri ->
            this.uri = uri
            itemState = ItemState.URI_CHANGED
        }
        sendBucketEvent(GalleryEvent.OnGalleryUpdated(this, itemState))
    }

    private suspend fun sendBucketEvent(fragEvent: GalleryEvent, immediate: Boolean = true) {
        if (immediate) _galleryFlow.emit(fragEvent)
        else pool.holdEvent(fragEvent)
    }

    private suspend fun sendListEvent(fragEvent: GalleryListEvent, immediate: Boolean = true) {
        if (immediate) getCurrentMailer()?.emit(fragEvent)
        else pool.holdEvent(fragEvent)
    }

    private suspend fun getAllCheckedGallery(isVideo: Boolean) = galleryDAO.run {
        if (combine) getAllGallery() else getAllGallery(isVideo)
    }

    private suspend fun getGallerySize(name: String, isVideo: Boolean): Int = galleryDAO.run {
        if (combine) getMediaCountByGallery(name) else getMediaCountByGallery(name, isVideo)
    }

    private suspend fun getGallerySize(isVideo: Boolean): Int = galleryDAO.run {
        if (combine) getMediaCount() else getMediaCount(isVideo)
    }

    private suspend fun getAllGalleryBucket() = galleryDAO.getAllGalleryBucket()

    private suspend fun getNewestMedia(gallery: String, isVideo: Boolean) = galleryDAO.run {
        if (combine) getNewestMediaInGallery(gallery)
        else getNewestMediaInGallery(gallery, isVideo)
    }

    private suspend fun getNewestMediaChecked(isVideo: Boolean) = galleryDAO.run {
        if (combine) getNewestMedia()
        else getNewestMedia(isVideo)
    }

}

class MediaSelectedList : LinkedBlockingDeque<GalleryMedia>() {

    var dataListener: DataListener? = null

    interface DataListener {
        fun onAdded() {}
        fun onRemoved() {}
        fun onCleared() {}
    }

    override fun add(element: GalleryMedia): Boolean {
        return super.add(element).also {
            if (it) dataListener?.onAdded()
        }
    }

    override fun addFirst(e: GalleryMedia?) {
        super.addFirst(e)
        dataListener?.onAdded()
    }

    override fun addLast(e: GalleryMedia?) {
        super.addLast(e)
        dataListener?.onAdded()
    }

    override fun addAll(elements: Collection<GalleryMedia>): Boolean {
        return super.addAll(elements).also {
            if (it) dataListener?.onAdded()
        }
    }

    override fun remove(element: GalleryMedia?): Boolean {
        return super.remove(element).also {
            if (it) dataListener?.onRemoved()
        }
    }

    override fun removeAll(elements: Collection<GalleryMedia>): Boolean {
        return super.removeAll(elements.toSet()).apply {
            dataListener?.onRemoved()
        }
    }

    override fun remove(): GalleryMedia {
        return super.remove().apply {
            dataListener?.onRemoved()
        }
    }

    override fun removeFirst(): GalleryMedia {
        return super.removeFirst().apply {
            dataListener?.onRemoved()
        }
    }

    override fun removeLast(): GalleryMedia {
        return super.removeLast().apply {
            dataListener?.onRemoved()
        }
    }

    override fun clear() {
        super.clear()
        dataListener?.onCleared()
    }
}