package com.protone.common.entity

import android.net.Uri

data class Gallery(
    val name: String,
    var size: Int,
    var uri: Uri?,
    val custom: Boolean = false
){
    enum class ItemState {
        SIZE_CHANGED,
        URI_CHANGED,
        ALL_CHANGED
    }
}