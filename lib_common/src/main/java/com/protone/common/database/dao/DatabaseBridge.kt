package com.protone.common.database.dao

import com.protone.common.database.MediaAction
import com.protone.common.entity.*

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

        private var helperImpl: DatabaseBridge? = null
    }

    val musicDAOBridge by lazy { MusicDAOBridge() }
    val noteDAOBridge by lazy { NoteDAOBridge() }
    val galleryDAOBridge by lazy { GalleryDAOBridge() }

    inner class MusicDAOBridge : BaseMusicDAO() {

        override fun sendEvent(mediaAction: MediaAction.MusicDataAction) {
            sendMusicAction(mediaAction)
        }

        fun insertMusicMultiAsync(music: List<Music>) {
            execute {
                music.forEach {
                    insertMusic(it)
                }
            }
        }

        suspend fun insertMusicMulti(music: List<Music>) {
            music.forEach {
                insertMusic(it)
            }
        }

        fun deleteMusicMultiAsync(music: List<Music>) {
            if (music.isEmpty()) return
            execute {
                music.forEach {
                    deleteMusic(it)
                }
            }
        }

        suspend fun deleteMusicMulti(music: List<Music>) {
            if (music.isEmpty()) return
            music.forEach {
                deleteMusic(it)
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
                deleteMusicWithMusicBucket(musicID, musicBucketId)
            }
        }

        fun insertMusicMultiAsyncWithBucket(musicBucket: String, music: List<Music>) {
            execute {
                val bucket = musicDAOBridge.getMusicBucketByName(musicBucket)
                bucket?.let { b ->
                    music.forEach {
                        insertMusicWithMusicBucket(
                            MusicWithMusicBucket(
                                b.musicBucketId,
                                it.musicBaseId
                            )
                        )
                    }
                }
            }
        }

        suspend fun insertMusicWithMusicBucket(musicID: Long, bucket: String): Long {
            val musicBucket = musicDAOBridge.getMusicBucketByName(bucket) ?: return -1L
            val entity = MusicWithMusicBucket(
                musicBucket.musicBucketId,
                musicID
            )
            return (insertMusicWithMusicBucket(entity) ?: -1)
        }

    }

    inner class GalleryDAOBridge : BaseGalleryDAO() {

        override fun sendEvent(mediaAction: MediaAction.GalleryDataAction) {
            sendGalleryAction(mediaAction)
        }

        fun deleteSignedMediaMultiAsync(list: MutableList<GalleryMedia>) {
            execute {
                list.forEach {
                    deleteSignedMediaByUri(it.uri)
                }
            }
        }

        fun deleteSignedMediaAsync(media: GalleryMedia) {
            execute {
                deleteSignedMedia(media)
            }
        }

        fun updateMediaMultiAsync(list: MutableList<GalleryMedia>) {
            execute { list.forEach { updateSignedMedia(it) } }
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


        fun deleteGalleryBucketAsync(galleryBucket: GalleryBucket) {
            execute {
                deleteGalleryBucket(galleryBucket)
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
                getAllGalleryBucket(galleryBucket.isImage)?.forEach {
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

        override fun sendEvent(mediaAction: MediaAction.NoteDataAction) {
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

        suspend fun insertNoteDirRs(
            noteDir: NoteDir,
        ): Pair<Boolean, NoteDir> {
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