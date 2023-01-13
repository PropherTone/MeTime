package com.protone.music.viewModel

import androidx.lifecycle.viewModelScope
import com.protone.common.baseType.*
import com.protone.common.entity.Music
import com.protone.common.entity.MusicBucket
import com.protone.common.utils.ALL_MUSIC
import com.protone.common.utils.EventCachePool
import com.protone.common.utils.MUSIC_BUCKET
import com.protone.component.BaseViewModel
import com.protone.component.database.MediaAction
import com.protone.component.database.userConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext
import java.io.File

class MusicModel : BaseViewModel() {

    sealed class MusicViewEvent : ViewEvent {
        data class PlayMusic(val music: Music) : MusicViewEvent()
        data class SetBucketCover(val name: String) : MusicViewEvent()
        data class AddMusic(val bucket: String) : MusicViewEvent()
        data class Edit(val bucket: String) : MusicViewEvent()
        data class Delete(val bucket: String) : MusicViewEvent()
        data class RefreshBucket(val newName: String) : MusicViewEvent()
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

        data class OnMusicInsert(val music: Music, val bucketName: String) : MusicEvent()
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

    private var isInit = false

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

    private val eventPool by lazy {
        EventCachePool<MusicEvent>(duration = 400L).handleEvent {
            if (it.isEmpty()) return@handleEvent
            when (it.first()) {
                is MusicEvent.OnMusicInsert -> {
                    sendMusicEvent(MusicEvent.OnMusicsInsert(
                        it.map { event -> (event as MusicEvent.OnMusicInsert).music }
                    ))
//                    sendMusicEvent(MusicEvent.OnMusicBucketUpdated(getBucket(ALL)))
                }
                is MusicEvent.OnMusicInsertToBucket -> {}
                is MusicEvent.OnMusicRemoveFromBucket -> {}
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
                        eventPool.holdEvent(MusicEvent.OnMusicInsert(it.music, ALL_MUSIC))
                    }
                    is MediaAction.MusicDataAction.OnMusicsInserted -> {
                        it.musics.forEach { music ->
                            eventPool.holdEvent(MusicEvent.OnMusicInsert(music, ALL_MUSIC))
                        }
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
    }

    suspend fun getCurrentMusicList(bucket: MusicBucket): MutableList<Music> =
        musicDAO.run {
            (getMusicWithMusicBucket(bucket.musicBucketId) as MutableList<Music>).let {
                if (it.isEmpty())
                    getMusicBucketByName(bucket.name)
                        ?.musicBucketId
                        ?.let { id -> getMusicWithMusicBucket(id) as MutableList<Music> }
                        ?: mutableListOf()
                else it
            }
        }

    suspend fun getBucketRefreshed(name: String) = withIOContext {
        getBucket(name)?.let {
            it.size
            val newBucket = musicDAO.getMusicWithMusicBucket(it.musicBucketId)
            if (it.size == 0 && newBucket.isNotEmpty()) {
                it.icon = newBucket[0].uri.toBitmap()?.saveToFile(name, MUSIC_BUCKET)
                musicDAO.updateMusicBucket(it)
            }
            it.size = newBucket.size
            it
        }
    }

    suspend fun getBucket(name: String) =
        musicDAO.getMusicBucketByName(name)

    suspend fun getLastMusicBucket(list: MutableList<MusicBucket>): MusicBucket =
        withContext(Dispatchers.Default) { list.find { it.name == lastBucket } ?: MusicBucket() }

    suspend fun getMusicBuckets(): MutableList<MusicBucket> = withIOContext {
        musicDAO.run {
            ((getAllMusicBucket() as MutableList<MusicBucket>?)
                ?: mutableListOf()).let { list ->
                list.forEach {
                    val newSize = getMusicWithMusicBucket(it.musicBucketId).size
                    if (it.size != newSize) {
                        it.size = newSize
                        updateMusicBucket(it)
                    }
                }
                list
            }
        }
    }

    suspend fun tryDeleteMusicBucket(name: String): MusicBucket? {
        return musicDAO.let {
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