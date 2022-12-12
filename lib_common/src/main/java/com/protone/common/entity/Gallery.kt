package com.protone.common.entity

import android.net.Uri

data class Gallery(
    val name: String,
    var size: Int,
    var uri: Uri?,
    var itemState: ItemState = ItemState.ALL_CHANGED,
    val custom: Boolean = false
) {
    enum class ItemState {
        SIZE_CHANGED,
        URI_CHANGED,
        ALL_CHANGED
    }
}