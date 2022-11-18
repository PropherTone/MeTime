package com.protone.common.database.dao

import android.net.Uri
import com.protone.common.baseType.withIOContext
import com.protone.common.database.MediaAction
import com.protone.common.entity.Music
import com.protone.common.entity.MusicBucket
import com.protone.common.entity.MusicWithMusicBucket
import com.protone.database.room.getMusicBucketDAO
import com.protone.database.room.getMusicDAO
import com.protone.database.room.getMusicWithMusicBucketDAO

abstract class BaseMusicDAO : BaseDAO<MediaAction.MusicDataAction>() {

    private val musicDAO = getMusicDAO()

    suspend fun getAllMusic(): List<Music>? =
        withIOContext { musicDAO.getAllMusic() }

    suspend fun getMusicByUri(uri: Uri): Music? =
        withIOContext { musicDAO.getMusicByUri(uri) }

    suspend fun insertMusic(music: Music) = withIOContext {
        sendEvent(MediaAction.MusicDataAction.OnMusicInserted(music))
        musicDAO.insertMusic(music)
    }

    suspend fun deleteMusic(music: Music) = withIOContext {
        sendEvent(MediaAction.MusicDataAction.OnMusicDeleted(music))
        musicDAO.deleteMusic(music)
    }

    suspend fun updateMusic(music: Music): Int = withIOContext {
        sendEvent(MediaAction.MusicDataAction.OnMusicUpdate(music))
        musicDAO.updateMusic(music)
    }

    /*MusicBucket*****************************************************/
    private val musicBucketDAO = getMusicBucketDAO()

    suspend fun getAllMusicBucket(): List<MusicBucket>? = withIOContext {
        musicBucketDAO.getAllMusicBucket()
    }

    suspend fun getMusicBucketByName(name: String): MusicBucket? = withIOContext {
        musicBucketDAO.getMusicBucketByName(name)
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

    /*MusicWithMusicBucket*****************************************************/
    private val musicWithMusicBucketDAO = getMusicWithMusicBucketDAO()

    suspend fun insertMusicWithMusicBucket(musicWithMusicBucket: MusicWithMusicBucket): Long? =
        withIOContext {
            sendEvent(MediaAction.MusicDataAction.OnMusicWithMusicBucketInserted(musicWithMusicBucket))
            musicWithMusicBucketDAO.insertMusicWithMusicBucket(musicWithMusicBucket)
        }

    suspend fun deleteMusicWithMusicBucket(musicID: Long, musicBucketId: Long) =
        withIOContext {
            sendEvent(MediaAction.MusicDataAction.OnMusicWithMusicBucketDeleted(musicID))
            musicWithMusicBucketDAO.deleteMusicWithMusicBucket(musicID, musicBucketId)
        }

    suspend fun getMusicWithMusicBucket(musicBucketId: Long): List<Music> =
        withIOContext {
            musicWithMusicBucketDAO.getMusicWithMusicBucket(musicBucketId) ?: mutableListOf()
        }

    suspend fun getMusicBucketWithMusic(musicID: Long): List<MusicBucket> =
        withIOContext {
            musicWithMusicBucketDAO.getMusicBucketWithMusic(musicID) ?: mutableListOf()
        }

}
