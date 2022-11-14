package com.protone.common.entity

import android.net.Uri

class RichPhotoStates(
    val uri: Uri,
    name: String?,
    var path: String?,
    val date: String?
) : RichStates(name ?: "")
