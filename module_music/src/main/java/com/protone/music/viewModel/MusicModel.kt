package com.protone.music.viewModel

import androidx.lifecycle.viewModelScope
import com.protone.common.baseType.*
import com.protone.component.database.MediaAction
import com.protone.component.database.userConfig
import com.protone.common.entity.Music
import com.protone.common.entity.MusicBucket
import com.protone.common.utils.MUSIC_BUCKET
import com.protone.component.BaseViewModel
import com.protone.music.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MusicModel : BaseViewModel() {

    sealed class MusicEvent : ViewEvent {
        data class PlayMusic(val music: Music) : MusicEvent()
        data class SetBucketCover(val name: String) : MusicEvent()
        data class AddMusic(val bucket: String) : MusicEvent()
        data class Edit(val bucket: String) : MusicEvent()
        data class Delete(val bucket: String) : MusicEvent()
        data class RefreshBucket(val newName: String) : MusicEvent()
        data class AddBucket(val bucket: String) : MusicEvent()
        data class DeleteBucket(val bucket: String) : MusicEvent()
        object AddMusicBucket : MusicEvent()
        object Locate : MusicEvent()
        object Search : MusicEvent()
    }

    var lastBucket: String = userConfig.lastMusicBucket
        set(value) {
            userConfig.lastMusicBucket = value
            field = value
        }
        get() = userConfig.lastMusicBucket

    var onMusicDataEvent: OnMusicDataEvent? = null

    init {
        viewModelScope.launchDefault {
            observeMusicData {
                when (it) {
                    is MediaAction.MusicDataAction.OnNewMusicBucket ->
                        onMusicDataEvent?.onNewMusicBucket(it.musicBucket)
                    is MediaAction.MusicDataAction.OnMusicBucketUpdated ->
                        onMusicDataEvent?.onMusicBucketUpdated(it.musicBucket)
                    is MediaAction.MusicDataAction.OnMusicBucketDeleted ->
                        onMusicDataEvent?.onMusicBucketDeleted(it.musicBucket)
                    else -> Unit
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

    suspend fun getMusicBuckets(): MutableList<MusicBucket> = withContext(Dispatchers.IO) {
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

    interface OnMusicDataEvent {
        suspend fun onNewMusicBucket(musicBucket: MusicBucket)
        suspend fun onMusicBucketUpdated(musicBucket: MusicBucket)
        suspend fun onMusicBucketDeleted(musicBucket: MusicBucket)
    }
}