package com.protone.component.notification.music.remoteViews

import android.app.NotificationManager
import com.protone.component.R
import com.protone.component.notification.music.BaseMusicNotification

class MusicNotification(notificationManager: NotificationManager) :
    BaseMusicNotification(notificationManager) {

    override fun getNotificationIcon(): Int = R.drawable.ic_baseline_music_note_24

    override fun getNotificationIdName(): String = "METIME_MUSIC"

    override fun getNotificationName(): String = "METIME_MUSIC_NOTIFICATION"

    override fun getNotificationId(): Int = 0x01

    override fun getPlayIcon(): Int = R.drawable.ic_baseline_pause_24

    override fun getPauseIcon(): Int = R.drawable.ic_baseline_play_arrow_24

}