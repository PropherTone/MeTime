package com.protone.database.room

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.protone.common.context.MApplication
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
internal abstract class DataBase : RoomDatabase() {
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
        val database: DataBase
            @Synchronized get() {
                return databaseImpl?.get() ?: init().apply {
                    databaseImpl = WeakReference(this)
                }
            }

        private var databaseImpl: WeakReference<DataBase>? = null

        private fun init(): DataBase {
            return Room.databaseBuilder(
                MApplication.app,
                DataBase::class.java,
                "SeennDB"
            ).build()
        }
    }

}