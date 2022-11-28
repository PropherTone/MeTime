package com.protone.common.utils

import android.net.Uri
import android.provider.MediaStore
import com.alibaba.android.arouter.facade.Postcard
import com.protone.common.R
import com.protone.common.baseType.getString
import com.protone.common.utils.json.toJson

const val TAG = "MeTimeLogTAG"

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
            ): Postcard = withString(CHOOSE_MODE, chooseMode)
        }

        object GalleryViewWire {
            const val MEDIA = "MEDIA"
            const val IS_VIDEO = "IS_VIDEO"
            const val GALLERY = "GALLERY"

            fun Postcard.galleryViewPostcard(
                mediaJson: String,
                isVideo: Boolean,
                targetGallery: String = "全部"
            ): Postcard = withString(MEDIA, mediaJson)
                .withBoolean(IS_VIDEO, isVideo)
                .withString(GALLERY, targetGallery)
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
                withString(NOTE_NAME, noteName)
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

            fun Postcard.pickMusicPostcard(mode: String): Postcard = withString(MODE, mode)
        }
    }
}
