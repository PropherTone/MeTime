package com.protone.common.utils

import android.net.Uri
import android.provider.MediaStore

const val TAG = "TAG"

val imageContent: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

val videoContent: Uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI

val musicContent: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

val sizeFormatMap = mapOf(Pair(0, "B"), Pair(1, "KB"), Pair(2, "MB"), Pair(3, "GB"))
