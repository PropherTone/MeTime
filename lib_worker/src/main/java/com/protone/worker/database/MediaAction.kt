package com.protone.worker.database

import com.protone.common.entity.*

sealed class MediaAction {

    data class OnNewMusicBucket(val musicBucket: MusicBucket) : MediaAction()
    data class OnMusicBucketUpdated(val musicBucket: MusicBucket) : MediaAction()
    data class OnMusicBucketDeleted(val musicBucket: MusicBucket) : MediaAction()

    data class OnMusicInserted(val music: Music) : MediaAction()
    data class OnMusicDeleted(val music: Music) : MediaAction()
    data class OnMusicUpdate(val music: Music) : MediaAction()

    data class OnMediaDeleted(val media: GalleryMedia) : MediaAction()
    data class OnMediaInserted(val media: GalleryMedia) : MediaAction()
    data class OnMediaUpdated(val media: GalleryMedia) : MediaAction()
    data class OnGalleryDeleted(val gallery: String) : MediaAction()

    data class OnNoteUpdated(val note: Note) : MediaAction()
    data class OnNoteDeleted(val note: Note) : MediaAction()
    data class OnNoteInserted(val note: Note) : MediaAction()

    data class OnNoteDirInserted(val noteDir: NoteDir) : MediaAction()
    data class OnNoteDirDeleted(val noteDir: NoteDir) : MediaAction()

    data class OnGalleryBucketInserted(val galleryBucket: GalleryBucket) : MediaAction()
    data class OnGalleryBucketDeleted(val galleryBucket: GalleryBucket) : MediaAction()

    data class OnGalleriesWithNotesInserted(val galleriesWithNotes: GalleriesWithNotes) :
        MediaAction()

    data class OnNoteDirWithNoteInserted(val noteDirWithNotes: NoteDirWithNotes) :
        MediaAction()

    data class OnMusicWithMusicBucketInserted(val musicWithMusicBucket: MusicWithMusicBucket) :
        MediaAction()

    data class OnMusicWithMusicBucketDeleted(val musicID: Long) : MediaAction()
}

//sealed class MusicBucketAction {
//
//}
//
//sealed class MusicAction {
//
//}
//
//sealed class galleryAction {
//
//}
//
//sealed class NoteAction {
//
//}
//
//sealed class NoteTypeAction {
//
//}
//
//sealed class galleryBucketAction {
//
//}
//
//sealed class GalleriesWithNotesAction {
//
//}
//
//sealed class NoteDirWithNoteAction {
//
//}
//
//sealed class MusicWithMusicBucketAction {
//
//}
