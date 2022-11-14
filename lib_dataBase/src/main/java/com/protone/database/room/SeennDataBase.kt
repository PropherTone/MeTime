package com.protone.database.room

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.protone.common.context.SApplication
import com.protone.common.entity.*
import com.protone.database.room.dao.*
import java.lang.ref.WeakReference

@Database(
    entities = [
        GalleryMedia::class,
        Note::class,
        NoteDir::class,
        MusicBucket::class,
        Music::class,
        GalleryBucket::class,
        GalleriesWithNotes::class,
        NoteDirWithNotes::class,
        MusicWithMusicBucket::class
    ],
    version = 1,
    exportSchema = false
)
internal abstract class SeennDataBase : RoomDatabase() {
    internal abstract fun getGalleryDAO(): SignedGalleryDAO
    internal abstract fun getNoteDAO(): NoteDAO
    internal abstract fun getNoteTypeDAO(): NoteTypeDAO
    internal abstract fun getMusicBucketDAO(): MusicBucketDAO
    internal abstract fun getMusicDAO(): MusicDAO
    internal abstract fun getGalleryBucketDAO(): GalleryBucketDAO
    internal abstract fun getGalleriesWithNotesDAO(): GalleriesWithNotesDAO
    internal abstract fun getNoteDirWithNoteDAO(): NoteDirWithNoteDAO
    internal abstract fun getMusicWithMusicBucketDAO(): MusicWithMusicBucketDAO

    companion object {
        @JvmStatic
        val database: SeennDataBase
            @Synchronized get() {
                return databaseImpl?.get() ?: init().apply {
                    databaseImpl = WeakReference(this)
                }
            }

        private var databaseImpl: WeakReference<SeennDataBase>? = null

        private fun init(): SeennDataBase {
            return Room.databaseBuilder(
                SApplication.app,
                SeennDataBase::class.java,
                "SeennDB"
            ).build()
        }
    }

}