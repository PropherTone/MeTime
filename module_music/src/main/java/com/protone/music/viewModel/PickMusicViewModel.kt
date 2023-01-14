package com.protone.music.viewModel

import androidx.lifecycle.viewModelScope
import com.protone.common.baseType.launchIO
import com.protone.common.baseType.withIOContext
import com.protone.common.entity.Music
import com.protone.component.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PickMusicViewModel : BaseViewModel() {

    companion object {
        const val BUCKET_NAME = "BUCKET"
        const val MODE = "MODE"

        const val ADD_BUCKET = "ADD"
        const val PICK_MUSIC = "PICK"
        const val SEARCH_MUSIC = "SEARCH"
    }

    val data: MutableList<Music> = mutableListOf()

    suspend fun getMusics() = musicDAO.getAllMusic() as MutableList<Music>

    suspend fun filterData(input: String) = withContext(Dispatchers.Default) {
        data.filter {
            it.displayName?.contains(input, true) == true
                    || it.album?.contains(input, true) == true
        } as MutableList<Music>
    }

    suspend fun getMusicWithMusicBucket(bucket: String): Collection<Music> =
        withIOContext {
            musicDAO.run {
                val musicBucket = getMusicBucketByName(bucket)
                if (musicBucket != null) {
                    getMusicWithMusicBucket(musicBucket.musicBucketId)
                } else listOf()
            }
        }

    fun deleteMusicWithMusicBucket(musicBaseId: Long, musicBucket: String) {
        viewModelScope.launchIO {
            musicDAO.run {
                getMusicBucketByName(musicBucket)?.let { musicBucket ->
                    deleteMusicWithMusicBucketAsync(musicBaseId, musicBucket.musicBucketId)
                }
            }
        }
    }

    suspend fun insertMusicWithMusicBucket(musicBaseId: Long, bucket: String): Long =
        withIOContext {
            musicDAO.insertMusicWithMusicBucket(musicBaseId, bucket)
        }

}