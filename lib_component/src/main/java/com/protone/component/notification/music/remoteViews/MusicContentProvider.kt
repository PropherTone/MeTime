package com.protone.component.notification.music.remoteViews

import android.content.Intent
import com.protone.common.context.*
import com.protone.component.R

class MusicContentProvider : BaseMusicContentProvider() {

    override fun getSmallLayout(): Int = R.layout.music_notification_layout
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