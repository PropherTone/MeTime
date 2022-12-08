package com.protone.database.room

import android.content.Context
import com.protone.common.entity.GalleryMedia
import com.protone.common.entity.Music
import com.protone.common.entity.Note
import com.protone.database.room.dao.*
import com.wajahatkarim3.roomexplorer.RoomExplorer

inline fun <reified T> List<T>.mapToLongList(block: (T) -> Long): List<Long> {
    val list = mutableListOf<Long>()
    this.forEach {
        block(it).apply {
            if (this != -1L) list.add(this)
        }
    }
    return list
}

infix fun List<GalleryMedia>.mediasFilterBy(idList: List<Long>) =
    if (this.size != idList.size)
        this.filter { idList.contains(it.mediaId) }
    else this

infix fun List<Music>.musicsFilterBy(idList: List<Long>) =
    if (this.size != idList.size)
        this.filter { idList.contains(it.musicBaseId) }
    else this

infix fun List<Note>.notesFilterBy(idList: List<Long>) =
    if (this.size != idList.size)
        this.filter { idList.contains(it.noteId) }
    else this

fun getGalleryDAO(): SignedGalleryDAO {
    return DataBase.database.getGalleryDAO()
}

fun getNoteDAO(): NoteDAO {
    return DataBase.database.getNoteDAO()
}

fun getNoteTypeDAO(): NoteTypeDAO {
    return DataBase.database.getNoteTypeDAO()
}

fun getMusicBucketDAO(): MusicBucketDAO {
    return DataBase.database.getMusicBucketDAO()
}

fun getMusicDAO(): MusicDAO {
    return DataBase.database.getMusicDAO()
}

fun getGalleryBucketDAO(): GalleryBucketDAO {
    return DataBase.database.getGalleryBucketDAO()
}

fun getGalleriesWithNotesDAO(): GalleriesWithNotesDAO {
    return DataBase.database.getGalleriesWithNotesDAO()
}

fun getNoteDirWithNoteDAO(): NoteDirWithNoteDAO {
    return DataBase.database.getNoteDirWithNoteDAO()
}

fun getMusicWithMusicBucketDAO(): MusicWithMusicBucketDAO {
    return DataBase.database.getMusicWithMusicBucketDAO()
}

fun getMediaWithGalleryBucketDAO(): MediaWithGalleryBucketDAO {
    return DataBase.database.getMediaWithGalleryBucketDAO()
}

fun shutdownDataBase() {
    DataBase.database.close()
}

fun showRoomDB(context: Context) {
    RoomExplorer.show(context, DataBase::class.java, "SeennDB")
}
