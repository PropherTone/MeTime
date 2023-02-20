package com.protone.component.database.dao

import android.util.Log
import com.protone.common.baseType.getString
import com.protone.common.baseType.imageSaveToDisk
import com.protone.common.entity.*
import com.protone.common.utils.MUSIC_BUCKET
import com.protone.common.utils.TAG
import com.protone.component.database.MediaAction

class DatabaseBridge : DatabaseHelper() {

    companion object {
        @JvmStatic
        val instance: DatabaseBridge
            @Synchronized get() {
                if (helperImpl == null) {
                    synchronized(this::class) {
                        helperImpl = DatabaseBridge()
                    }
                }
                return helperImpl!!
            }

        @Volatile
        private var helperImpl: DatabaseBridge? = null
    }

    val musicDAOBridge by lazy { MusicDAOBridge() }
    val noteDAOBridge by lazy { NoteDAOBridge() }
    val galleryDAOBridge by lazy { GalleryDAOBridge() }

    inner class MusicDAOBridge : BaseMusicDAO() {

        override suspend fun sendEvent(mediaAction: MediaAction.MusicDataAction) {
            sendMusicAction(mediaAction)
        }

        fun insertMusicMultiAsync(musics: List<Music>) {
            execute {
                insertMusicMulti(musics)
            }
        }

        fun deleteMusicMultiAsync(musics: List<Music>) {
            execute {
                deleteMusicMulti(musics)
            }
        }

        fun insertMusicCheck(music: Music) = execute {
            getMusicByUri(music.uri).let {
                if (it == null) {
                    insertMusic(music)
                } else {
                    updateMusic(music)
                }
            }
        }

        fun deleteMusicAsync(music: Music) = execute {
            deleteMusic(music)
        }

        fun updateMusicBucketAsync(bucket: MusicBucket) {
            execute {
                updateMusicBucket(bucket)
            }
        }

        fun addMusicBucketAsync(musicBucket: MusicBucket) {
            execute {
                addMusicBucket(musicBucket)
            }
        }

        fun deleteMusicBucketAsync(bucket: MusicBucket) {
            execute {
                deleteMusicBucket(bucket)
            }
        }

        suspend fun deleteMusicBucketRs(bucket: MusicBucket): Boolean {
            deleteMusicBucket(bucket)
            return getMusicBucketByName(bucket.name) == null
        }

        inline fun addMusicBucketWithCallBack(
            musicBucket: MusicBucket,
            crossinline callBack: suspend (result: Boolean, name: String) -> Unit
        ) {
            execute {
                var count = 0
                val tempName = musicBucket.name
                val names = mutableMapOf<String, Int>()
                getAllMusicBucket()?.forEach {
                    names[it.name] = 1
                    if (it.name == musicBucket.name) {
                        musicBucket.name = "${tempName}(${++count})"
                    }
                }
                while (names[musicBucket.name] != null) {
                    musicBucket.name = "${tempName}(${++count})"
                }
                addMusicBucket(musicBucket)
                callBack(getMusicBucketByName(musicBucket.name) != null, musicBucket.name)
            }
        }


        fun deleteMusicWithMusicBucketAsync(musicID: Long, musicBucketId: Long) {
            execute {
                deleteMusicWithMusicBucket(musicID, musicBucketId)?.let {
                    execute {
                        musicDAOBridge.getMusicBucketById(musicBucketId)?.let { bucket ->
                            bucket.size = getMusicWithBucketSize(musicBucketId)
                            updateMusicBucket(bucket)
                        }
                    }
                }
            }
        }

        fun insertMusicMultiWithBucketAsync(musicBucket: String, musics: List<Music>) {
            execute {
                musics.forEach {
                    insertMusicWithMusicBucket(it.musicBaseId, musicBucket)
                }
            }
        }

        suspend fun insertMusicWithMusicBucket(musicID: Long, bucket: String): Long {
            val musicBucket = musicDAOBridge.getMusicBucketByName(bucket) ?: return -1L
            return insertMusicWithMusicBucket(
                MusicWithMusicBucket(musicBucket.musicBucketId, musicID)
            )?.let {
                execute {
                    if (musicBucket.icon == null) {
                        musicBucket.icon = getNewestMusicInBucket(musicBucket.musicBucketId)
                            ?.imageSaveToDisk(musicBucket.name, MUSIC_BUCKET)
                    }
                    musicBucket.size = getMusicWithBucketSize(musicBucket.musicBucketId)
                    updateMusicBucket(musicBucket)
                }
                it
            } ?: -1
        }

    }

    inner class GalleryDAOBridge : BaseGalleryDAO() {

        override suspend fun sendEvent(mediaAction: MediaAction.GalleryDataAction) {
            sendGalleryAction(mediaAction)
        }

        fun insertMediaWithGalleryBucketAsync(bucketId: Long, media: GalleryMedia) {
            execute {
                insertMediaWithGalleryBucket(
                    MediaWithGalleryBucket(bucketId, media.mediaId),
                    media
                )
            }
        }

        fun insertMediaWithGalleryBucketMultiAsync(bucketName: String, medias: List<GalleryMedia>) {
            if (medias.isEmpty()) return
            execute {
                getGalleryBucket(bucketName)?.let { bucket ->
                    insertMediaWithGalleryBucketMulti(medias.map {
                        MediaWithGalleryBucket(it.mediaId, bucket.galleryBucketId)
                    }, medias)
                }
            }
        }

        fun insertMediaWithGalleryBucketMultiAsync(bucketId: Long, medias: List<GalleryMedia>) {
            if (medias.isEmpty()) return
            execute {
                insertMediaWithGalleryBucketMulti(medias.map {
                    MediaWithGalleryBucket(it.mediaId, bucketId)
                }, medias)
            }
        }

        fun deleteMediasWithGalleryBucketAsync(medias: List<GalleryMedia>, bucket: String) {
            execute {
                getGalleryBucket(bucket)?.let {
                    medias.forEach { media ->
                        deleteMediaWithGalleryBucket(
                            MediaWithGalleryBucket(media.mediaId, it.galleryBucketId)
                        )
                    }
                }
            }
        }

        fun deleteSignedMediaMultiAsync(list: List<GalleryMedia>) {
            execute {
                deleteSignedMediaMulti(list)
            }
        }

        fun updateSignedMediaAsync(media: GalleryMedia) = execute {
            updateSignedMedia(media)
        }

        fun deleteSignedMediaAsync(media: GalleryMedia) {
            execute {
                deleteSignedMedia(media)
            }
        }

        fun updateMediaMultiAsync(list: List<GalleryMedia>) {
            execute { updateSignedMediaMulti(list) }
        }

        fun insertMediaAsync(media: GalleryMedia) {
            execute {
                insertSignedMedia(media)
            }
        }

        fun deleteSignedMediasByGalleryAsync(gallery: String) {
            execute {
                deleteSignedMediasByGallery(gallery)
            }
        }

        fun deleteGalleryBucketAsync(galleryBucket: GalleryBucket) {
            execute {
                deleteGalleryBucket(galleryBucket)
            }
        }

        suspend fun insertSignedMediaChecked(media: GalleryMedia): GalleryMedia? {
            val it = getSignedMedia(media.uri)
            return if (it != null) {
                if (it == media) return null
                updateSignedMedia(it)
                it
            } else {
                insertSignedMedia(media)
                media
            }
        }

        inline fun insertGalleryBucketCB(
            galleryBucket: GalleryBucket,
            crossinline callBack: suspend (Boolean, String) -> Unit
        ) {
            execute {
                var count = 0
                val tempName = galleryBucket.type
                val names = mutableMapOf<String, Int>()
                getAllGalleryBucket()?.forEach {
                    names[it.type] = 1
                    if (it.type == galleryBucket.type) {
                        galleryBucket.type = "${tempName}(${++count})"
                    }
                }
                while (names[galleryBucket.type] != null) {
                    galleryBucket.type = "${tempName}(${++count})"
                }
                insertGalleryBucket(galleryBucket)
                callBack.invoke(getGalleryBucket(galleryBucket.type) != null, galleryBucket.type)
            }
        }

    }

    inner class NoteDAOBridge : BaseNoteDAO() {

        override suspend fun sendEvent(mediaAction: MediaAction.NoteDataAction) {
            sendNoteAction(mediaAction)
        }

        fun deleteNoteAsync(note: Note) {
            execute {
                deleteNote(note)
            }
        }

        suspend fun insertNoteRs(note: Note): Pair<Boolean, Long> {
            val id = insertNote(note)
            return Pair(getNoteByName(note.title) != null, id)
        }

        suspend fun insertNoteDirRs(noteDir: NoteDir): Pair<Boolean, NoteDir> {
            var count = 0
            val tempName = noteDir.name
            val names = mutableMapOf<String, Int>()
            getALLNoteDir()?.forEach { dir ->
                names[dir.name] = 1
                if (dir.name == noteDir.name) {
                    noteDir.name = "${tempName}(${++count})"
                }
            }
            while (names[noteDir.name] != null) {
                noteDir.name = "${tempName}(${++count})"
            }
            insertNoteDir(noteDir)
            return getNoteDir(noteDir.name).let { Pair(it != null, it ?: noteDir) }
        }

        suspend fun doDeleteNoteDirRs(noteDir: NoteDir): Boolean {
            deleteNoteDir(noteDir)
            return getNoteDir(noteDir.name) != null
        }
    }

}