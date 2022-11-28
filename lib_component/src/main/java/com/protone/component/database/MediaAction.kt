package com.protone.component.database

import com.protone.common.entity.*

sealed class MediaAction {

    sealed class MusicDataAction : MediaAction() {
        data class OnNewMusicBucket(val musicBucket: MusicBucket) : MusicDataAction()
        data class OnMusicBucketUpdated(val musicBucket: MusicBucket) : MusicDataAction()
        data class OnMusicBucketDeleted(val musicBucket: MusicBucket) : MusicDataAction()
        data class OnMusicInserted(val music: Music) : MusicDataAction()
        data class OnMusicDeleted(val music: Music) : MusicDataAction()
        data class OnMusicUpdate(val music: Music) : MusicDataAction()
        data class OnMusicWithMusicBucketInserted(val musicWithMusicBucket: MusicWithMusicBucket) : MusicDataAction()
        data class OnMusicWithMusicBucketDeleted(val musicID: Long) : MusicDataAction()
    }

    sealed class NoteDataAction : MediaAction() {
        data class OnNoteUpdated(val note: Note) : NoteDataAction()
        data class OnNoteDeleted(val note: Note) : NoteDataAction()
        data class OnNoteInserted(val note: Note) : NoteDataAction()
        data class OnNoteDirInserted(val noteDir: NoteDir) : NoteDataAction()
        data class OnNoteDirDeleted(val noteDir: NoteDir) : NoteDataAction()
        data class OnNoteDirWithNoteInserted(val noteDirWithNotes: NoteDirWithNotes) : NoteDataAction()
    }

    sealed class GalleryDataAction : MediaAction() {
        data class OnGalleryMediaDeleted(val media: GalleryMedia) : GalleryDataAction()
        data class OnGalleryMediaInserted(val media: GalleryMedia) : GalleryDataAction()
        data class OnGalleryMediaUpdated(val media: GalleryMedia) : GalleryDataAction()
        data class OnGalleryDeleted(val gallery: String) : GalleryDataAction()
        data class OnGalleryBucketInserted(val galleryBucket: GalleryBucket) : GalleryDataAction()
        data class OnGalleryBucketDeleted(val galleryBucket: GalleryBucket) : GalleryDataAction()
        data class OnGalleriesWithNotesInserted(val galleriesWithNotes: GalleriesWithNotes) : GalleryDataAction()
    }

}
