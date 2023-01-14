package com.protone.component.notification.music

import android.app.Notification
import android.content.Context
import android.graphics.Bitmap

interface IMusicNotification {
    fun initNotification(context: Context): Notification
    fun doNotify()
    fun setTitle(title: CharSequence)
    fun setMusicCover(cover: Bitmap)
    fun setPlayState(isPlaying: Boolean)
    fun cancelAll()
    fun release()
}

interface IMusicNotificationProvider {
    fun getNotificationIcon(): Int
    fun getNotificationIdName(): String
    fun getNotificationName(): String
    fun getNotificationId(): Int
    fun getPlayIcon(): Int
    fun getPauseIcon(): Int
}