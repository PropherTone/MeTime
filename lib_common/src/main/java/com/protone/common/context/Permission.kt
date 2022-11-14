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

inline fun Activity.checkNeededPermission(onFailed : ()->Unit, onSucceed : ()->Unit){
    val read = this.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    val write = this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    if ((read) == PackageManager.PERMISSION_GRANTED && (write) == PackageManager.PERMISSION_GRANTED){
        onSucceed()
    }else onFailed()
}