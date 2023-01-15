package com.protone.component.database.dao

import android.net.Uri
import com.protone.common.baseType.withIOContext
import com.protone.component.database.MediaAction
import com.protone.common.entity.Music
import com.protone.common.entity.MusicBucket
import com.protone.common.entity.MusicWithMusicBucket
import com.protone.database.room.getMusicBucketDAO
import com.protone.database.room.getMusicDAO
import com.protone.database.room.getMusicWithMusicBucketDAO
import com.protone.database.room.musicsFilterBy

sealed class BaseMusicDAO : MusicDAO()

sealed class MusicDAO : MusicBucketDAO() {
    private val musicDAO = getMusicDAO()

    suspend fun getNewestMusicUri(): Uri? = withIOContext { musicDAO.getNewestMusicUri() }

    suspend fun getAllMusic(): List<Music>? =
        withIOContext { musicDAO.getAllMusic() }

    suspend fun getMusicByUri(uri: Uri): Music? =
        withIOContext { musicDAO.getMusicByUri(uri) }

    suspend fun getMusicById(id: Long): Music? =
        withIOContext { musicDAO.getMusicById(id) }

    suspend fun insertMusic(music: Music) = withIOContext {
        musicDAO.insertMusic(music).also {
            if (it == -1L) return@withIOContext null
            sendEvent(MediaAction.MusicDataAction.OnMusicInserted(music))
        }
    }

    suspend fun insertMusicMulti(musics: List<Music>) = withIOContext {
        if (musics.isEmpty()) return@withIOContext emptyList()
        musicDAO.insertMusicMulti(musics).apply {
            sendEvent(
                MediaAction.MusicDataAction.OnMusicsInserted(musics musicsFilterBy this)
            )
        }
    }

    suspend fun deleteMusicMulti(musics: List<Music>) = withIOContext {
        if (musics.isEmpty()) return@withIOContext emptyList()
        musicDAO.deleteMusicMulti(musics).apply {
            sendEvent(
                MediaAction.MusicDataAction.OnMusicsDeleted(musics musicsFilterBy this)
            )
        }
    }

    suspend fun deleteMusic(music: Music) = withIOContext {
        musicDAO.deleteMusic(music).apply {
            sendEvent(MediaAction.MusicDataAction.OnMusicDeleted(music))
        }
    }

    suspend fun updateMusic(music: Music): Int = withIOContext {
        musicDAO.updateMusic(music).apply {
            sendEvent(MediaAction.MusicDataAction.OnMusicUpdate(music))
        }
    }
}

sealed class MusicBucketDAO : MusicWithMusicBucketDAO() {
    private val musicBucketDAO = getMusicBucketDAO()

    suspend fun getAllMusicBucket(): List<MusicBucket>? = withIOContext {
        musicBucketDAO.getAllMusicBucket()
    }

    suspend fun getMusicBucketByName(name: String): MusicBucket? = withIOContext {
        musicBucketDAO.getMusicBucketByName(name)
    }

    suspend fun getMusicBucketById(id: Long): MusicBucket? = withIOContext {
        musicBucketDAO.getMusicBucketById(id)
    }

    suspend fun addMusicBucket(musicBucket: MusicBucket) = withIOContext {
        sendEvent(MediaAction.MusicDataAction.OnNewMusicBucket(musicBucket))
        musicBucketDAO.addMusicBucket(musicBucket)
    }

    suspend fun updateMusicBucket(musicBucket: MusicBucket): Int = withIOContext {
        sendEvent(MediaAction.MusicDataAction.OnMusicBucketUpdated(musicBucket))
        musicBucketDAO.updateMusicBucket(musicBucket)
    }

    suspend fun deleteMusicBucket(musicBucket: MusicBucket) = withIOContext {
        sendEvent(MediaAction.MusicDataAction.OnMusicBucketDeleted(musicBucket))
        musicBucketDAO.deleteMusicBucket(musicBucket)
    }
}

sealed class MusicWithMusicBucketDAO : BaseDAO<MediaAction.MusicDataAction>() {
    private val musicWithMusicBucketDAO = getMusicWithMusicBucketDAO()

    suspend fun insertMusicWithMusicBucket(musicWithMusicBucket: MusicWithMusicBucket): Long? =
        withIOContext {
            musicWithMusicBucketDAO.insertMusicWithMusicBucket(musicWithMusicBucket)?.also {
                if (it == -1L) return@withIOContext null
                sendEvent(
                    MediaAction.MusicDataAction.OnMusicWithMusicBucketInserted(musicWithMusicBucket)
                )
            }
        }

    suspend fun deleteMusicWithMusicBucket(musicID: Long, musicBucketId: Long) =
        withIOContext {
            musicWithMusicBucketDAO.deleteMusicWithMusicBucket(musicID, musicBucketId)?.also {
                if (it == -1) return@withIOContext null
                sendEvent(
                    MediaAction.MusicDataAction.OnMusicWithMusicBucketDeleted(
                        musicID,
                        musicBucketId
                    )
                )
            }
        }

    suspend fun getMusicWithMusicBucket(musicBucketId: Long): List<Music>? =
        withIOContext {
            musicWithMusicBucketDAO.getMusicWithMusicBucket(musicBucketId)
        }

    suspend fun getMusicBucketWithMusic(musicID: Long): List<MusicBucket>? =
        withIOContext {
            musicWithMusicBucketDAO.getMusicBucketWithMusic(musicID)
        }

    suspend fun getMusicWithBucketSize(musicBucketId: Long): Int = withIOContext {
        musicWithMusicBucketDAO.getMusicWithBucketSize(musicBucketId)
    }

}

