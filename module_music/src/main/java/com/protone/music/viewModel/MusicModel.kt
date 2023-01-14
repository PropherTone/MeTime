package com.protone.music.viewModel

import androidx.lifecycle.viewModelScope
import com.protone.common.baseType.bufferCollect
import com.protone.common.baseType.launchDefault
import com.protone.common.baseType.withIOContext
import com.protone.common.entity.Music
import com.protone.common.entity.MusicBucket
import com.protone.common.utils.ALL_MUSIC
import com.protone.common.utils.EventCachePool
import com.protone.component.BaseViewModel
import com.protone.component.database.MediaAction
import com.protone.component.database.userConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.io.File

class MusicModel : BaseViewModel() {

    sealed class MusicViewEvent : ViewEvent {
        data class PlayMusic(val music: Music) : MusicViewEvent()
        data class AddMusic(val bucket: String) : MusicViewEvent()
        data class Edit(val bucket: String) : MusicViewEvent()
        data class Delete(val bucket: String) : MusicViewEvent()
        data class AddBucket(val bucket: String) : MusicViewEvent()
        data class DeleteBucket(val bucket: String) : MusicViewEvent()
        object AddMusicBucket : MusicViewEvent()
        object Locate : MusicViewEvent()
        object Search : MusicViewEvent()
    }

    sealed class MusicEvent {
        data class OnMusicBucketInsert(val musicBucket: MusicBucket) : MusicEvent()
        data class OnMusicBucketDeleted(val musicBucket: MusicBucket) : MusicEvent()
        data class OnMusicBucketUpdated(val musicBucket: MusicBucket) : MusicEvent()

        data class OnMusicInsert(val music: Music) : MusicEvent()
        data class OnMusicsInsert(val musics: Collection<Music>) : MusicEvent()
        data class OnMusicDeleted(val music: Music) : MusicEvent()
        data class OnMusicsDeleted(val musics: Collection<Music>) : MusicEvent()
        data class OnMusicUpdated(val music: Music) : MusicEvent()

        data class OnMusicInsertToBucket(val musicID: Long, val musicBucketId: Long) : MusicEvent()
        data class OnMusicRemoveFromBucket(
            val musicID: Long,
            val musicBucketId: Long
        ) : MusicEvent()
    }

    var isInit = false

    var playerFitTopH = 0

    private val _musicObserver by lazy { MutableSharedFlow<MusicEvent>() }
    fun CoroutineScope.observeMusicEvent(block: suspend (MusicEvent) -> Unit) {
        launchDefault {
            _musicObserver.asSharedFlow().bufferCollect {
                while (!isInit) delay(24L)
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
    fun getDefaultBucket() = defaultBucket

    private val eventPool by lazy {
        EventCachePool<MusicEvent>(duration = 400L).handleEvent {
            if (it.isEmpty()) return@handleEvent
            when (it.first()) {
                is MusicEvent.OnMusicInsert -> {
                    sendMusicEvent(MusicEvent.OnMusicsInsert(
                        it.map { event -> (event as MusicEvent.OnMusicInsert).music }
                    ))
                    getBucket(ALL_MUSIC)?.let { bucket ->
                        sendMusicEvent(MusicEvent.OnMusicBucketUpdated(bucket))
                    }
                }
                is MusicEvent.OnMusicInsertToBucket -> {
                    TODO("NOT YET IMP")
                }
                is MusicEvent.OnMusicRemoveFromBucket -> {
                    TODO("NOT YET IMP")
                }
                else -> Unit
            }
        }
    }

    init {
        viewModelScope.launchDefault {
            observeMusicData {
                while (!isInit) delay(24L)
                when (it) {
                    is MediaAction.MusicDataAction.OnNewMusicBucket -> {
                        sendMusicEvent(MusicEvent.OnMusicBucketInsert(it.musicBucket))
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
                        sendMusicEvent(MusicEvent.OnMusicsInsert(it.musics))
                    }
                    is MediaAction.MusicDataAction.OnMusicDeleted -> {
                        sendMusicEvent(MusicEvent.OnMusicDeleted(it.music))
                    }
                    is MediaAction.MusicDataAction.OnMusicsDeleted -> {
                        sendMusicEvent(MusicEvent.OnMusicsDeleted(it.musics))
                    }
                    is MediaAction.MusicDataAction.OnMusicUpdate -> {
                        sendMusicEvent(MusicEvent.OnMusicUpdated(it.music))
                    }
                    is MediaAction.MusicDataAction.OnMusicWithMusicBucketInserted -> {
                        eventPool.holdEvent(
                            MusicEvent.OnMusicInsertToBucket(
                                it.musicWithMusicBucket.musicBaseId,
                                it.musicWithMusicBucket.musicBucketId
                            )
                        )
                    }
                    is MediaAction.MusicDataAction.OnMusicWithMusicBucketDeleted -> {
                        eventPool.holdEvent(
                            MusicEvent.OnMusicRemoveFromBucket(it.musicID, it.musicBucketId)
                        )
                    }
                }
            }
        }
        viewModelScope.launchDefault {
            defaultBucket = MusicBucket(
                ALL_MUSIC,
                null,
                0,
                null,
                null
            ).also { it.tempIcon = musicDAO.getNewestMusicUri().toString() }
        }
    }

    suspend fun getCurrentMusicList(bucket: MusicBucket): List<Music> = withIOContext {
        musicDAO.let {
            if (bucket.name == ALL_MUSIC) it.getAllMusic()
            else it.getMusicWithMusicBucket(bucket.musicBucketId)
        } ?: listOf()
    }

    suspend fun getBucket(name: String) =
        if (name == ALL_MUSIC) defaultBucket else musicDAO.getMusicBucketByName(name)

    suspend fun getMusicBuckets(): MutableList<MusicBucket> = withIOContext {
        musicDAO.run {
            ((getAllMusicBucket() as MutableList<MusicBucket>?)
                ?: mutableListOf()).let { list ->
                viewModelScope.launchDefault {
//                    list.forEach {
//                        val newSize = getMusicWithMusicBucket(it.musicBucketId).size
//                        if (it.size != newSize) {
//                            it.size = newSize
//                            updateMusicBucket(it)
//                        }
//                    }
                }
                list.add(0, defaultBucket)
                list
            }
        }
    }

    suspend fun getMusicById(id: Long) = musicDAO.getMusicById(id)

    suspend fun tryDeleteMusicBucket(name: String): MusicBucket? = withIOContext {
        musicDAO.let {
            val musicBucketByName = it.getMusicBucketByName(name)
            if (musicBucketByName != null) {
                if (it.deleteMusicBucketRs(musicBucketByName)) {
                    musicBucketByName.icon?.let { path ->
                        val file = File(path)
                        if (file.isFile && file.exists()) file.delete()
                    }
                }
                musicBucketByName
            } else null
        }
    }

    private suspend fun sendMusicEvent(musicEvent: MusicEvent) {
        _musicObserver.emit(musicEvent)
    }

}