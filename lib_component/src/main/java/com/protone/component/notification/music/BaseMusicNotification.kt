package com.protone.component.notification.music

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Icon
import android.os.Build
import android.widget.RemoteViews
import com.protone.common.context.MApplication
import com.protone.component.R
import com.protone.component.notification.music.remoteViews.IMusicRemoteViews
import com.protone.component.notification.music.remoteViews.MusicContentProvider

abstract class BaseMusicNotification(
    private val notificationManager: NotificationManager,
    private val provider: IMusicNotificationProvider,
    mrv: IMusicRemoteViews
) : IMusicNotification {

    private val remoteViews = MusicContentProvider(mrv)
    private var smallContent: RemoteViews? = null
    private var bigContent: RemoteViews? = null
    private var notification: Notification? = null

    private fun Context.sdkONotification(content: RemoteViews, bigContent: RemoteViews? = null) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                provider.getNotificationIdName(),
                provider.getNotificationName(),
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
            Notification.Builder(this, provider.getNotificationIdName()).apply {
                setOngoing(true)
                setSmallIcon(provider.getNotificationIcon())
                setLargeIcon(Icon.createWithResource(MApplication.app, R.drawable.ic_baseline_pause_24))
                setCustomContentView(content)
                if (bigContent != null) setCustomBigContentView(bigContent)
            }.build()
        } else null

    @Suppress("DEPRECATION")
    private fun legacyNotification(content: RemoteViews, bigContent: RemoteViews? = null) =
        Notification().apply {
            icon = provider.getNotificationIcon()
            contentView = content
            if (bigContent != null) bigContentView = bigContent
        }

    override fun initNotification(context: Context): Notification {
        return remoteViews.let {
            val small = it.getContent(context)
            val big = it.getBigContent(context)
            smallContent = small
            bigContent = big
            context.sdkONotification(small, big) ?: legacyNotification(small, big)
        }.also {
            it.flags = Notification.FLAG_NO_CLEAR
            notification = it
        }
    }

    override fun doNotify() {
        notificationManager.notify(provider.getNotificationId(), notification)
    }

    override fun setTitle(title: CharSequence) {
        smallContent?.setTextViewText(remoteViews.getTitleId(), title)
        bigContent?.setTextViewText(remoteViews.getTitleId(), title)
    }

    override fun setMusicCover(cover: Bitmap) {
        smallContent?.setImageViewBitmap(remoteViews.getCoverId(), cover)
        bigContent?.setImageViewBitmap(remoteViews.getCoverId(), cover)
    }

    override fun setPlayState(isPlaying: Boolean) {
        smallContent?.setImageViewResource(
            remoteViews.getPlayId(),
            if (isPlaying) provider.getPlayIcon() else provider.getPauseIcon()
        )
        bigContent?.setImageViewResource(
            remoteViews.getPlayId(),
            if (isPlaying) provider.getPlayIcon() else provider.getPauseIcon()
        )
    }

    override fun cancelAll() {
        notificationManager.cancelAll()
    }

    override fun release() {
        smallContent?.removeAllViews(remoteViews.getRootId())
        bigContent?.removeAllViews(remoteViews.getRootId())
        smallContent = null
        bigContent = null
        notification = null
    }
}