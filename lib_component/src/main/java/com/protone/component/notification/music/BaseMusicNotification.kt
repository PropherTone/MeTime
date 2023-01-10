package com.protone.component.notification.music

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.widget.RemoteViews
import com.protone.component.notification.music.remoteViews.MusicContentProvider

abstract class BaseMusicNotification(private val notificationManager: NotificationManager) :
    IMusicNotification, IMusicNotificationProvider {

    private val remoteViews = MusicContentProvider()
    private var content: RemoteViews? = null
    private var notification: Notification? = null

    private fun Context.sdkONotification(content: RemoteViews, bigContent: RemoteViews? = null) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                getNotificationIdName(),
                getNotificationName(),
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
            Notification.Builder(this, getNotificationName()).apply {
                setCustomContentView(content)
                if (bigContent != null) setCustomBigContentView(bigContent)
                setSmallIcon(getNotificationIcon())
            }.build()
        } else null

    @Suppress("DEPRECATION")
    private fun legacyNotification(content: RemoteViews, bigContent: RemoteViews? = null) =
        Notification().apply {
            contentView = content
            if (bigContent != null) bigContentView = bigContent
            icon = getNotificationIcon()
        }

    override fun initNotification(context: Context): Notification {
        return remoteViews.getBigContent(context).let {
            content = it
            context.sdkONotification(it) ?: legacyNotification(it)
        }.also {
            it.flags = Notification.FLAG_NO_CLEAR
            notification = it
        }
    }

    override fun doNotify() {
        notificationManager.notify(getNotificationId(), notification)
    }

    override fun setTitle(title: CharSequence) {
        content?.setTextViewText(remoteViews.getTitleId(), title)
    }

    override fun setMusicCover(cover: Bitmap) {
        content?.setImageViewBitmap(remoteViews.getCoverId(), cover)
    }

    override fun setPlayState(isPlaying: Boolean) {
        content?.setImageViewResource(
            remoteViews.getPlayId(),
            if (isPlaying) getPlayIcon() else getPauseIcon()
        )
    }

    override fun cancelAll() {
        notificationManager.cancelAll()
    }

    override fun release() {
        content?.removeAllViews(remoteViews.getRootId())
        content = null
        notification = null
    }
}

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