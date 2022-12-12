package com.protone.common.context

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager

fun Activity.requestContentPermission() {
    this.requestPermissions(
        arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ), 0
    )
}

inline fun Activity.checkNeededPermission(block: Permission.() -> Unit) {
    val permission = Permission()
    block(permission)
    val read = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    val write = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    if ((read) == PackageManager.PERMISSION_GRANTED && (write) == PackageManager.PERMISSION_GRANTED) {
        permission.onSucceed?.invoke()
    } else permission.onFailed?.invoke()
}

class Permission {

    var onFailed: (() -> Unit)? = null
    var onSucceed: (() -> Unit)? = null

    fun onFailed(block: () -> Unit) {
        onFailed = block
    }

    fun onSucceed(block: () -> Unit) {
        onSucceed = block
    }
}