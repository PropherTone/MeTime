package com.protone.component.notification.music

import android.app.NotificationManager
import android.content.Intent
import com.protone.common.context.MApplication
import com.protone.component.R
import com.protone.component.notification.music.remoteViews.IMusicRemoteViews

class MusicNotification(
    notificationManager: NotificationManager,
    provider: IMusicNotificationProvider = MusicNotificationProvider(),
    mrv: IMusicRemoteViews = MusicRemoteView()
) : BaseMusicNotification(notificationManager, provider, mrv)

internal class MusicNotificationProvider : IMusicNotificationProvider {
    override fun getNotificationIcon(): Int = R.drawable.ic_baseline_music_note_24

    override fun getNotificationIdName(): String = "METIME_MUSIC"

    override fun getNotificationName(): String = "METIME_MUSIC_NOTIFICATION"

    override fun getNotificationId(): Int = 0x01

    override fun getPlayIcon(): Int = R.drawable.ic_baseline_pause_24

    override fun getPauseIcon(): Int = R.drawable.ic_baseline_play_arrow_24
}

internal class MusicRemoteView : IMusicRemoteViews {
    override fun getSmallLayout(): Int = R.layout.music_notification_small_layout
    override fun getSmallRootId(): Int = R.id.notify_music_parent
    override fun getSmallTitleId(): Int = R.id.notify_music_name
    override fun getSmallPlayId(): Int = R.id.notify_music_control
    override fun getSmallPreviousId(): Int = R.id.notify_music_previous
    override fun getSmallNextId(): Int = R.id.notify_music_next
    override fun getSmallFinishId(): Int = R.id.notify_music_close
    override fun getSmallCoverId(): Int = R.id.notify_music_icon
    override fun getSmallIntent(): Intent = Intent(
        MApplication.app,
        Class.forName("com.protone.music.activity.MusicViewActivity")
    )

    override fun getBigLayout(): Int = R.layout.music_notification_layout
    override fun getBigRootId(): Int = R.id.notify_music_parent
    override fun getBigTitleId(): Int = R.id.notify_music_name
    override fun getBigPlayId(): Int = R.id.notify_music_control
    override fun getBigPreviousId(): Int = R.id.notify_music_previous
    override fun getBigNextId(): Int = R.id.notify_music_next
    override fun getBigFinishId(): Int = R.id.notify_music_close
    override fun getBigCoverId(): Int = R.id.notify_music_icon
    override fun getBigIntent(): Intent = Intent(
        MApplication.app,
        Class.forName("com.protone.music.activity.MusicViewActivity")
    )
}
