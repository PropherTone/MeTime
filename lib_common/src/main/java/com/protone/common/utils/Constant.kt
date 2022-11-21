package com.protone.common.utils

import android.net.Uri
import android.provider.MediaStore

const val TAG = "MeTimeLogTAG"

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
    }

    object NoteRouterPath {
        private const val Home = "/Note"
        const val Main = "$Home/Main"
        const val Edit = "$Home/Edit"
        const val NoteView = "$Home/View"
    }

    object MusicRouterPath {
        private const val Home = "/Music"
        const val Main = "$Home/Main"
        const val AddBucket = "$Home/AddMusicBucket"
        const val MusicPlayer = "$Home/MusicView"
        const val Pick = "$Home/PickMusic"
    }
}
