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
import com.protone.component.database.userConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import com.protone.gallery.viewModel.GalleryViewModel.GalleryEvent.OnGalleryUpdated.ItemState

class GalleryViewModel : BaseViewModel() {

    sealed class GalleryEvent {
        data class OnNewGallery(val gallery: Gallery) : GalleryEvent()
        data class OnGalleryRemoved(val gallery: Gallery) : GalleryEvent()
        data class OnGalleryUpdated(
            val gallery: Gallery,
            val itemState: ItemState = ItemState.ALL_CHANGED
        ) : GalleryEvent() {
            enum class ItemState {
                SIZE_CHANGED,
                URI_CHANGED,
                ALL_CHANGED
            }
        }

        data class OnNewGalleries(val galleries: List<Gallery>) : GalleryEvent()
    }

    sealed class GalleryListEvent : GalleryEvent() {
        data class OnDrawerEvent(val isOpen: Boolean) : GalleryListEvent()
        object SelectAll : GalleryListEvent()
        object QuiteSelect : GalleryListEvent()
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
    private fun getGalleryData(isVideo: Boolean) = if (isVideo) galleryVideoData else galleryData
    fun getGalleryData() = if (isVideoGallery) galleryVideoData else galleryData

    val dataFlow by lazy { MutableSharedFlow<GalleryListFragmentViewModel.GallerySelectData?>() }
    val chooseData by lazy { mutableListOf<GalleryMedia>() }

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

    var rightMailer = 0
        private set
    private var isDataSorted = false
    private val combine = userConfig.combineGallery
    private val isVideoGallery: Boolean get() = rightMailer == 1
    private val isLock = userConfig.lockGallery.isNotEmpty()

    private val pool =
        EventCachePool.get<GalleryEvent>(duration = 500L).apply {
            handleEvent { data ->
                if (data.isNotEmpty()) when (data.first()) {
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
        }

    fun sortData() {
        observeGallery()
        if (combine) {
            sortData(false)
        } else {
            sortData(false)
            sortData(true)
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
        viewModelScope.launch {
            getCurrentMailer()?.emit(GalleryListEvent.SelectAll)
        }
    }

    fun quiteSelect() {
        viewModelScope.launch {
            chooseData.clear()
            dataFlow.emit(null)
            sendListEvent(GalleryListEvent.QuiteSelect)
        }
    }

    fun drawerStateChanged(isOpen: Boolean) {
        viewModelScope.launchDefault {
            sendListEvent(GalleryListEvent.OnDrawerEvent(isOpen))
        }
    }

    fun getBucket(bucket: String) = getGalleryData(isVideoGallery).find { it.name == bucket }

    fun getSelectedBucket() = getGalleryData(isVideoGallery).find { it.name == rightGallery }

    fun addBucket(name: String) {
        galleryDAO.insertGalleryBucketCB(GalleryBucket(name, isVideoGallery)) { re, reName ->
            if (re) {
                if (!isLock) {
                    galleryData.add(Gallery(reName, 0, null, custom = true))
                } else {
                    R.string.locked.getString().toast()
                }
            } else {
                R.string.failed_msg.getString().toast()
            }
        }
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
            R.string.photo.getString() -> rightMailer = 0
            R.string.video.getString() -> rightMailer = 1
        }
        return mailer != rightMailer
    }

    inline fun observeSelectData(crossinline onPost: (Boolean) -> Unit) {
        viewModelScope.launchDefault {
            dataFlow.bufferCollect {
                when (it) {
                    is GalleryListFragmentViewModel.GallerySelectData.OnGalleryMediaSelect -> {
                        chooseData.add(it.media)
                        onPost(true)
                    }
                    is GalleryListFragmentViewModel.GallerySelectData.OnGalleryMediasSelect -> {
                        chooseData.addAll(it.medias)
                        onPost(true)
                    }
                    null -> {
                        chooseData.clear()
                        onPost(false)
                    }
                }
            }
        }
    }

    private fun sortData(isVideo: Boolean) = viewModelScope.launchDefault {
        galleryDAO.run {
            val galleries =
                (if (combine) getAllGallery() else getAllGallery(isVideo)) as MutableList<String>?
            if (galleries == null) {
                R.string.none.getString().toast()
                return@launchDefault
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
            if (!isLock) sortPrivateData(isVideo) else isDataSorted = true
        }
    }

    private fun sortPrivateData(isVideo: Boolean) {
        viewModelScope.launchDefault {
            getAllGalleryBucket(isVideo)?.forEach {
                Gallery(it.type, 0, null, custom = true).cacheAndNotice(isVideo)
            }
            isDataSorted = true
        }
    }

    private fun observeGallery() {
        viewModelScope.launchDefault {
            suspend fun sortDeleteMedia(media: GalleryMedia) {
                getGalleryData(media.isVideo).find { it.name == media.bucket }?.let {
                    sendBucketEvent(GalleryEvent.OnGalleryUpdated(it), false)
                }
                if (media.isVideo != isVideoGallery && media.bucket != rightGallery) return
                chooseData.remove(media)
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
                                .cacheAndNotice(isVideoGallery)
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
                                                    GalleryListEvent.OnMediasInserted(
                                                        medias
                                                    )
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
        getGalleryData(isVideo).add(this)
        if (isVideoGallery == isVideo)
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

    suspend fun sendListEvent(fragEvent: GalleryListEvent, immediate: Boolean = true) {
        if (immediate) getCurrentMailer()?.emit(fragEvent)
        else pool.holdEvent(fragEvent)
    }

    private suspend fun getGallerySize(name: String, isVideo: Boolean): Int = galleryDAO.run {
        if (combine) getMediaCountByGallery(name) else getMediaCountByGallery(name, isVideo)
    }

    private suspend fun getGallerySize(isVideo: Boolean): Int = galleryDAO.run {
        if (combine) getMediaCount() else getMediaCount(isVideo)
    }

    private suspend fun getAllGalleryBucket(isVideo: Boolean) = galleryDAO.run {
        if (combine) getAllGalleryBucket() else getAllGalleryBucket(isVideo)
    }

    private suspend fun getNewestMedia(gallery: String, isVideo: Boolean) = galleryDAO.run {
        if (combine) getNewestMediaInGallery(gallery)
        else getNewestMediaInGallery(gallery, isVideo)
    }

    private suspend fun getNewestMediaChecked(isVideo: Boolean) = galleryDAO.run {
        if (combine) getNewestMedia()
        else getNewestMedia(isVideo)
    }

}