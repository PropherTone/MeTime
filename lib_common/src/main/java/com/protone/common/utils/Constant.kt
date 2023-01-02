package com.protone.common.utils

import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import com.alibaba.android.arouter.facade.Postcard

const val TAG = "MeTimeLogTAG"

const val ALL_GALLERY = "全部"
const val ALL_MUSIC = "全部音乐"
const val MUSIC_BUCKET = "MusicBucket"

val imageContent: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

val videoContent: Uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI

val musicContent: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

val sizeFormatMap = mapOf(Pair(0, "B"), Pair(1, "KB"), Pair(2, "MB"), Pair(3, "GB"))

class RouterPath {

    object GalleryRouterPath {
        private const val Home = "/Gallery"
        const val Main = "$Home/Main"
        const val GalleryView = "$Home/GalleryView"
        const val Search = "$Home/Search"
        const val Box = "$Home/Box"

        object GalleryMainWire {
            const val CHOOSE_MODE = "ChooseData"
            const val URI = "Uri"
            const val GALLERY_DATA = "GalleryData"
            const val CHOOSE_PHOTO = "PHOTO"
            const val CHOOSE_VIDEO = "VIDEO"
            const val CHOOSE_MEDIA = "MEDIA"

            fun Postcard.galleryMainPostcard(
                chooseMode: String
            ): Postcard = with(Bundle().apply {
                putString(CHOOSE_MODE, chooseMode)
            })
        }

        object GalleryViewWire {
            const val MEDIA = "MEDIA"
            const val IS_VIDEO = "IS_VIDEO"
            const val GALLERY = "GALLERY"

            fun Postcard.galleryViewPostcard(
                mediaJson: String,
                isVideo: Boolean,
                targetGallery: String = "全部"
            ): Postcard = with(Bundle().apply {
                putString(MEDIA, mediaJson)
                putBoolean(IS_VIDEO, isVideo)
                putString(GALLERY, targetGallery)
            })
        }
    }

    object NoteRouterPath {
        private const val Home = "/Note"
        const val Main = "$Home/Main"
        const val Edit = "$Home/Edit"
        const val NoteView = "$Home/View"

        object NoteViewWire {
            const val NOTE_NAME = "NOTE_NAME"

            fun Postcard.noteViewPostcard(noteName: String): Postcard =
                with(Bundle().apply {
                    putString(NOTE_NAME, noteName)
                })
        }

        object NoteEditWire {
            const val CONTENT_TITLE = "NoteContentTitle"
            const val NOTE_DIR = "NoteType"
            const val NOTE = "Note"

            fun Postcard.noteEditPostcard(title: String): Postcard =
                with(Bundle().apply {
                    putString(CONTENT_TITLE, title)
                })
        }
    }

    object MusicRouterPath {
        private const val Home = "/Music"
        const val Main = "$Home/Main"
        const val AddBucket = "$Home/AddMusicBucket"
        const val MusicPlayer = "$Home/MusicView"
        const val Pick = "$Home/PickMusic"

        object PickPostcard {
            const val BUCKET_NAME = "BUCKET"
            const val MODE = "MODE"
            const val ADD_BUCKET = "ADD"
            const val PICK_MUSIC = "PICK"
            const val SEARCH_MUSIC = "SEARCH"

            fun Postcard.pickMusicPostcard(mode: String): Postcard =
                with(Bundle().apply {
                    putString(MODE, mode)
                })
        }
    }

    object ConfigRouterPath {
        private const val Home = "/Config"
        const val UserConfig = "$Home/UserConfig"
        const val Log = "$Home/Log"
    }
}
