package com.protone.music.viewModel

import androidx.databinding.ObservableField
import androidx.lifecycle.viewModelScope
import com.protone.common.baseType.*
import com.protone.common.entity.Music
import com.protone.common.entity.MusicBucket
import com.protone.common.utils.ALL_MUSIC
import com.protone.common.utils.EventCachePool
import com.protone.common.utils.todayDate
import com.protone.component.BaseViewModel
import com.protone.component.R
import com.protone.component.ViewEventHandle
import com.protone.component.ViewEventHandler
import com.protone.component.database.MediaAction
import com.protone.component.database.userConfig
import com.protone.music.adapter.MusicBucketAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

class MusicModel : BaseViewModel(),
    ViewEventHandle<MusicModel.MusicViewEvent> by ViewEventHandler() {

    sealed class MusicViewEvent : ViewEvent {
        data class InitList(val list: List<MusicBucket>) : MusicViewEvent()

        data class DoEdit(val musicBucket: MusicBucket) : MusicViewEvent()
        data class DoAddMusic(val musicBucket: MusicBucket) : MusicViewEvent()

        data class PlayMusic(val music: Music) : MusicViewEvent()
        data class OnBucketSelect(val musicBucket: MusicBucket, val list: List<Music>) :
            MusicViewEvent()

        data class OnBucketRefresh(val musicBucket: MusicBucket, val state: Int) : MusicViewEvent()
        object AddMusicBucket : MusicViewEvent()
    }

    sealed class MusicEvent {
        data class OnMusicBucketInsert(val musicBucket: MusicBucket) : MusicEvent()
        data class OnMusicBucketDeleted(val musicBucket: MusicBucket) : MusicEvent()
        data class OnMusicBucketUpdated(val musicBucket: MusicBucket) : MusicEvent()

        data class OnMusicInsert(val music: Music) : MusicEvent()
        data class OnMusicDeleted(val music: Music) : MusicEvent()
        data class OnMusicUpdated(val music: Music) : MusicEvent()

        data class OnMusicBucketDataChanged(val musics: List<Music>) : MusicEvent()

        data class OnMusicsInsert(
            val musics: Collection<Music>,
            val bucketName: String = ALL_MUSIC
        ) : MusicEvent()

        data class OnMusicsDeleted(
            val musics: Collection<Music>,
            val bucketName: String = ALL_MUSIC
        ) : MusicEvent()

        sealed class MusicWithBucketEvent(
            val musicID: Long,
            val musicBucketID: Long
        ) : MusicEvent()

        data class OnMusicInsertToBucket(
            val musicId: Long,
            val musicBucketId: Long
        ) : MusicWithBucketEvent(musicId, musicBucketId)

        data class OnMusicRemoveFromBucket(
            val musicId: Long,
            val musicBucketId: Long
        ) : MusicWithBucketEvent(musicId, musicBucketId)
    }

    val isContainerOpen = ObservableField(true)

    private var isViewEventInit: AtomicBoolean? = null
    private var isEventInit: AtomicBoolean? = null
    var isInit
        set(value) {
            isViewEventInit?.set(value)
            isEventInit?.set(value)
        }
        get() = isViewEventInit?.get() == true && isEventInit?.get() == true

    var currentBucket = ""
    var playerFitTopH = 0

    private val _musicObserver by lazy { MutableSharedFlow<MusicEvent>() }
    fun CoroutineScope.observeMusicEvent(block: suspend (MusicEvent) -> Unit) {
        launchDefault {
            isViewEventInit = _musicObserver.asSharedFlow().mutableBufferCollect(this) {
                block(it)
            }
        }
    }

    var lastBucket: String = userConfig.lastMusicBucket
        set(value) {
            userConfig.lastMusicBucket = value
            field = value
        }
        get() = userConfig.lastMusicBucket

    private lateinit var defaultBucket: MusicBucket
    private fun getDefaultBucket() = defaultBucket

    private val eventPool by lazy {
        EventCachePool<MusicEvent>(duration = 400L).handleEvent {
            if (it.isEmpty()) return@handleEvent
            when (it.first()) {
                is MusicEvent.OnMusicInsert -> {
                    sendMusicsInserted(MusicEvent.OnMusicsInsert(
                        it.map { event -> (event as MusicEvent.OnMusicInsert).music }
                    ))
                }
//                is MusicEvent.OnMusicInsertToBucket -> {
//                    checkMusicWithBucket(it.map { event ->
//                        event as MusicEvent.MusicWithBucketEvent
//                    }) { musics, name ->
//                        sendMusicsInserted(MusicEvent.OnMusicsInsert(musics, name))
//                    }
//                }
//                is MusicEvent.OnMusicRemoveFromBucket -> {
//                    checkMusicWithBucket(it.map { event ->
//                        event as MusicEvent.MusicWithBucketEvent
//                    }) { musics, name ->
//                        sendMusicEvent(MusicEvent.OnMusicsDeleted(musics, name))
//                    }
//                }
                else -> Unit
            }
        }
    }

//    private suspend inline fun checkMusicWithBucket(
//        events: List<MusicEvent.MusicWithBucketEvent>,
//        callBack: (List<Music>, String) -> Unit
//    ) {
//        events.associateToMapList({
//            musicDAO.getMusicBucketById(it.musicBucketID) ?: getDefaultBucket()
//        }, {
//            musicDAO.getMusicById(it.musicID)
//        }).onEach { pair ->
//            callBack(pair.value.filterNotNull(), pair.key.name)
//        }
//    }

    init {
        viewModelScope.launchDefault {
            isEventInit = observeMusicDataMutable(viewModelScope) {
                when (it) {
                    is MediaAction.MusicDataAction.OnNewMusicBucket ->
                        musicDAO.getMusicBucketByName(it.musicBucket.name)?.let { mb ->
                            sendMusicEvent(MusicEvent.OnMusicBucketInsert(mb))
                        }
                    is MediaAction.MusicDataAction.OnMusicBucketUpdated -> {
                        sendMusicEvent(MusicEvent.OnMusicBucketUpdated(it.musicBucket))
                    }
                    is MediaAction.MusicDataAction.OnMusicBucketDeleted -> {
                        sendMusicEvent(MusicEvent.OnMusicBucketDeleted(it.musicBucket))
                    }
                    is MediaAction.MusicDataAction.OnMusicInserted -> {
                        eventPool.holdEvent(MusicEvent.OnMusicInsert(it.music))
                    }
                    is MediaAction.MusicDataAction.OnMusicsInserted -> {
                        sendMusicsInserted(MusicEvent.OnMusicsInsert(it.musics))
                    }
                    is MediaAction.MusicDataAction.OnMusicDeleted -> {
                        sendMusicDeleted(MusicEvent.OnMusicDeleted(it.music))
                    }
                    is MediaAction.MusicDataAction.OnMusicsDeleted -> {
                        sendMusicEvent(MusicEvent.OnMusicsDeleted(it.musics))
                    }
                    is MediaAction.MusicDataAction.OnMusicUpdate -> {
                        sendMusicEvent(MusicEvent.OnMusicUpdated(it.music))
                    }
//                    is MediaAction.MusicDataAction.OnMusicWithMusicBucketInserted -> {
//                        eventPool.holdEvent(
//                            MusicEvent.OnMusicInsertToBucket(
//                                it.musicWithMusicBucket.musicBaseId,
//                                it.musicWithMusicBucket.musicBucketId
//                            )
//                        )
//                    }
//                    is MediaAction.MusicDataAction.OnMusicWithMusicBucketDeleted -> {
//                        eventPool.holdEvent(
//                            MusicEvent.OnMusicRemoveFromBucket(it.musicID, it.musicBucketId)
//                        )
//                    }
                    else -> Unit
                }
            }
        }
        viewModelScope.launchDefault {
            defaultBucket = MusicBucket(
                ALL_MUSIC,
                null,
                musicDAO.getAllMusicSize(),
                null,
                "最后更新：${todayDate("yyyy/MM/dd hh:mm:ss")}"
            ).also { it.tempIcon = musicDAO.getNewestMusicUri().toString() }
        }
    }

    fun addMusicBucket() {
        viewModelScope.launch {
            sendViewEvent(MusicViewEvent.AddMusicBucket)
        }
    }

    fun initList() {
        viewModelScope.launch {
            sendViewEvent(MusicViewEvent.InitList(getMusicBuckets()))
        }
    }

    fun getBucketEventListener() = object : MusicBucketAdapter.MusicBucketEvent {
        override fun onBucketClicked(musicBucket: MusicBucket) {
            onBucketSelect(musicBucket)
        }

        override fun addMusic(musicBucket: MusicBucket, position: Int) {
            sendViewEvent(MusicViewEvent.DoAddMusic(musicBucket))
        }

        override fun delete(musicBucket: MusicBucket, position: Int) {
            tryDeleteMusicBucket(musicBucket.name)
        }

        override fun edit(musicBucket: MusicBucket, position: Int) {
            sendViewEvent(MusicViewEvent.DoEdit(musicBucket))
        }

        override fun onSelectedBucketRefresh(bucket: MusicBucket, state: Int) {
            sendViewEvent(MusicViewEvent.OnBucketRefresh(bucket, state))
        }
    }

    private var bucketObserveJob: Job? = null

    private fun onBucketSelect(musicBucket: MusicBucket) {
        bucketObserveJob?.cancel()
        bucketObserveJob = viewModelScope.launch {
            sendViewEvent(
                MusicViewEvent.OnBucketSelect(
                    musicBucket,
                    getCurrentMusicList(musicBucket)
                )
            )
            launchDefault {
                if (musicBucket.name == ALL_MUSIC) {
                    musicDAO.getAllMusicFlow().bufferCollect {
                        if (it == null) return@bufferCollect
                        sendMusicEvent(MusicEvent.OnMusicBucketDataChanged(it))
                    }
                } else {
                    musicDAO.getMusicWithMusicBucketFlow(musicBucket.musicBucketId).bufferCollect {
                        if (it == null) return@bufferCollect
                        sendMusicEvent(MusicEvent.OnMusicBucketDataChanged(it))
                    }
                }
            }
        }
        bucketObserveJob?.start()
    }

    private suspend fun sendMusicsInserted(onMusicsInsert: MusicEvent.OnMusicsInsert) {
        defaultBucket.size += onMusicsInsert.musics.size
        sendMusicEvent(MusicEvent.OnMusicBucketUpdated(defaultBucket))
        sendMusicEvent(onMusicsInsert)
    }

    private suspend fun sendMusicDeleted(onMusicDeleted: MusicEvent.OnMusicDeleted) {
        if (currentBucket != ALL_MUSIC) return
        getBucket(ALL_MUSIC)?.let { mb ->
            mb.size -= 1
            sendMusicEvent(onMusicDeleted)
        }
    }

    private suspend fun getCurrentMusicList(bucket: MusicBucket): List<Music> = withIOContext {
        musicDAO.let {
            if (bucket.name == ALL_MUSIC) it.getAllMusic()
            else it.getMusicWithMusicBucket(bucket.musicBucketId)
        } ?: listOf()
    }

    private suspend fun getBucket(name: String) =
        if (name == ALL_MUSIC) defaultBucket else musicDAO.getMusicBucketByName(name)

    private suspend fun getMusicBuckets(): List<MusicBucket> = withIOContext {
        musicDAO.run {
            ((getAllMusicBucket() as MutableList<MusicBucket>?)
                ?: mutableListOf()).let { list ->
                list.add(0, defaultBucket)
                list
            }
        }
    }

    fun tryDeleteMusicBucket(name: String) {
        viewModelScope.launchIO {
            if (name == ALL_MUSIC) {
                R.string.bruh.getString().toast()
                return@launchIO
            }
            musicDAO.let {
                it.getMusicBucketByName(name)?.also { musicBucket ->
                    if (it.deleteMusicBucketRs(musicBucket)) musicBucket.icon?.let { path ->
                        val file = File(path)
                        if (file.isFile && file.exists()) file.delete()
                    }
                }
            } ?: R.string.failed_msg.getString().toast()
        }
    }

    private suspend fun sendMusicEvent(musicEvent: MusicEvent) {
        _musicObserver.emit(musicEvent)
    }

}