package com.protone.database.room

import android.content.Context
import com.protone.database.room.dao.*
import com.wajahatkarim3.roomexplorer.RoomExplorer

fun getGalleryDAO(): SignedGalleryDAO {
    return SeennDataBase.database.getGalleryDAO()
}

fun getNoteDAO(): NoteDAO {
    return SeennDataBase.database.getNoteDAO()
}

fun getNoteTypeDAO(): NoteTypeDAO {
    return SeennDataBase.database.getNoteTypeDAO()
}

fun getMusicBucketDAO(): MusicBucketDAO {
    return SeennDataBase.database.getMusicBucketDAO()
}

fun getMusicDAO(): MusicDAO {
    return SeennDataBase.database.getMusicDAO()
}

fun getGalleryBucketDAO(): GalleryBucketDAO {
    return SeennDataBase.database.getGalleryBucketDAO()
}

fun getGalleriesWithNotesDAO(): GalleriesWithNotesDAO {
    return SeennDataBase.database.getGalleriesWithNotesDAO()
}

fun getNoteDirWithNoteDAO(): NoteDirWithNoteDAO {
    return SeennDataBase.database.getNoteDirWithNoteDAO()
}

fun getMusicWithMusicBucketDAO(): MusicWithMusicBucketDAO {
    return SeennDataBase.database.getMusicWithMusicBucketDAO()
}

fun shutdownDataBase(){
    SeennDataBase.database.close()
}

fun showRoomDB(context: Context){
    RoomExplorer.show(context,SeennDataBase::class.java,"SeennDB")
}
