package com.protone.gallery.viewModel

import androidx.lifecycle.viewModelScope
import com.google.android.material.tabs.TabLayout
import com.protone.common.R
import com.protone.common.baseType.bufferCollect
import com.protone.common.baseType.getString
import com.protone.common.baseType.launchDefault
import com.protone.common.baseType.toast
import com.protone.common.entity.Gallery
import com.protone.common.entity.GalleryMedia
import com.protone.common.utils.ALL_GALLERY
import com.protone.common.utils.EventCachePool
import com.protone.component.BaseViewModel
import com.protone.component.database.userConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class GalleryViewModel : BaseViewModel(), TabLayout.OnTabSelectedListener {

    companion object {
        const val CHOOSE_MODE = "ChooseData"
        const val URI = "Uri"
        const val Gallery_DATA = "GalleryData"
        const val CHOOSE_PHOTO = "PHOTO"
        const val CHOOSE_VIDEO = "VIDEO"
        const val CHOOSE_MEDIA = "MEDIA"
    }

    sealed class GalleryEvent {
        object SelectAll : GalleryEvent()
        object OnActionBtn : GalleryEvent()
        object IntoBox : GalleryEvent()

        data class OnNewGallery(val gallery: Gallery) : GalleryEvent()
        data class OnGalleryRemoved(val gallery: Gallery) : GalleryEvent()
        data class OnGalleryUpdated(val gallery: Gallery) : GalleryEvent()

        data class OnNewGalleries(val galleries: List<Gallery>) : GalleryEvent()

        data class OnSelect(val galleryMedia: MutableList<GalleryMedia>) : GalleryEvent()

        sealed class MediaEvent(val galleryMedia: GalleryMedia) : GalleryEvent()
        data class OnMediaDeleted(val media: GalleryMedia) : MediaEvent(media)
        data class OnMediaInserted(val media: GalleryMedia) : MediaEvent(media)
        data class OnMediaUpdated(val media: GalleryMedia) : MediaEvent(media)

        data class OnMediasInserted(val medias: List<GalleryMedia>) : GalleryEvent()
    }

    private val _galleryFlow = MutableSharedFlow<GalleryEvent>()
    val galleryFlow = _galleryFlow.asSharedFlow()

    private val mailers = arrayOfNulls<MutableSharedFlow<GalleryEvent>>(2)

    private val galleryData = mutableListOf<Gallery>()
    private val galleryVideoData by lazy { mutableListOf<Gallery>() }
    private fun getGalleryData(isVideo: Boolean) = if (isVideo) galleryVideoData else galleryData

    var chooseData: MutableList<GalleryMedia>? = null
        private set
    private var rightMailer = 0
    private val combine = userConfig.combineGallery
    private var isDataSorted = false
    private val isLock = userConfig.lockGallery.isNotEmpty()

    private val pool =
        EventCachePool.get<GalleryEvent>(duration = 500L).apply {
            handleEvent { data ->
                if (data.isNotEmpty()) when (data.first()) {
                    is GalleryEvent.OnMediaInserted -> data.first().also { event ->
                        if (event !is GalleryEvent.MediaEvent) return@also
                        data.associate {
                            (it as GalleryEvent.MediaEvent) to listOf(it.galleryMedia)
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
                                    sendEvent(GalleryEvent.OnMediasInserted(it))
                                }
                            }
                        }
                    }
                    is GalleryEvent.OnGalleryUpdated -> data.distinct().forEach { event ->
                        val isVideo = rightMailer == 1
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
        if (combine) {
            sortData(false)
        } else {
            sortData(false)
            sortData(true)
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
                if (combine) getNewestMedia() else getNewestMedia(isVideo)
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

    private suspend fun Gallery.cacheAndNotice(isVideo: Boolean) {
        getGalleryData(isVideo).add(this)
        sendEvent(GalleryEvent.OnNewGallery(this), true)
    }

    private suspend fun Gallery.updateGallery(isVideo: Boolean) {
        val all = getGallerySize(name, isVideo)
            .takeIf { size ->
                if (size <= 0) {
                    sendEvent(GalleryEvent.OnGalleryRemoved(this))
                    return
                }
                size != this.size
            }?.let { size ->
                this.size = size
                itemState = Gallery.ItemState.SIZE_CHANGED
                true
            } ?: false
                && getNewestMedia(name, isVideo)
            .takeIf { uri ->
                uri != this.uri
            }?.let { uri ->
                this.uri = uri
                itemState = Gallery.ItemState.URI_CHANGED
                true
            } ?: false
        if (all) itemState = Gallery.ItemState.ALL_CHANGED
        sendEvent(GalleryEvent.OnGalleryUpdated(this))
    }

    fun intoBox() {
        viewModelScope.launch {
            getCurrentMailer()?.emit(GalleryEvent.IntoBox)
        }
    }

    fun selectAll() {
        viewModelScope.launch {
            getCurrentMailer()?.emit(GalleryEvent.SelectAll)
        }
    }

    private fun getCurrentMailer() = mailers[rightMailer]

    suspend fun sendEvent(fragEvent: GalleryEvent, immediate: Boolean = true) {
        if (immediate) _galleryFlow.emit(fragEvent)
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

    override fun onTabSelected(tab: TabLayout.Tab?) {
        when (tab?.text) {
            R.string.photo.getString() -> rightMailer = 0
            R.string.video.getString() -> rightMailer = 1
        }
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) = Unit
    override fun onTabReselected(tab: TabLayout.Tab?) = Unit
}