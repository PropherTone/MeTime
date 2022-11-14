package com.protone.database.room

import android.content.Context
import com.protone.database.room.dao.*
import com.wajahatkarim3.roomexplorer.RoomExplorer

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

fun shutdownDataBase(){
    DataBase.database.close()
}

fun showRoomDB(context: Context){
    RoomExplorer.show(context,DataBase::class.java,"SeennDB")
}
