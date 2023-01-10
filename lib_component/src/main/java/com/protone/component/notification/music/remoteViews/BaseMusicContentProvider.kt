package com.protone.component.notification.music.remoteViews

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.protone.common.context.FINISH
import com.protone.common.context.MUSIC_NEXT
import com.protone.common.context.MUSIC_PLAY
import com.protone.common.context.MUSIC_PREVIOUS
import com.protone.component.notification.IRemoteView

abstract class BaseMusicContentProvider : IMusicRemoteViews {

    var isBigContent = false

    override fun getContent(context: Context): RemoteViews =
        RemoteViews(context.packageName, getLayout()).apply {
            val intentFlags = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT

            PendingIntent.getBroadcast(
                context,
                0,
                Intent(MUSIC_PLAY),
                intentFlags
            ).let { setOnClickPendingIntent(getPlayId(), it) }

            PendingIntent.getBroadcast(
                context,
                0,
                Intent(MUSIC_PREVIOUS),
                intentFlags
            ).let { setOnClickPendingIntent(getPreviousId(), it) }

            PendingIntent.getBroadcast(
                context,
                0,
                Intent(MUSIC_NEXT),
                intentFlags
            ).let { setOnClickPendingIntent(getNextId(), it) }

            PendingIntent.getActivity(
                context,
                0,
                getIntent(),
                intentFlags
            ).let { setOnClickPendingIntent(getRootId(), it) }

            PendingIntent.getBroadcast(
                context,
                0,
                Intent(FINISH),
                intentFlags
            ).let { setOnClickPendingIntent(getFinishId(), it) }
        }

    override fun getBigContent(context: Context): RemoteViews =
        RemoteViews(context.packageName, getLayout()).apply {
            val intentFlags = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT

            PendingIntent.getBroadcast(
                context,
                0,
                Intent(MUSIC_PLAY),
                intentFlags
            ).let { setOnClickPendingIntent(getPlayId(), it) }

            PendingIntent.getBroadcast(
                context,
                0,
                Intent(MUSIC_PREVIOUS),
                intentFlags
            ).let { setOnClickPendingIntent(getPreviousId(), it) }

            PendingIntent.getBroadcast(
                context,
                0,
                Intent(MUSIC_NEXT),
                intentFlags
            ).let { setOnClickPendingIntent(getNextId(), it) }

            PendingIntent.getActivity(
                context,
                0,
                getIntent(),
                intentFlags
            ).let { setOnClickPendingIntent(getRootId(), it) }

            PendingIntent.getBroadcast(
                context,
                0,
                Intent(FINISH),
                intentFlags
            ).let { setOnClickPendingIntent(getFinishId(), it) }
        }

    override fun getLayout(): Int =
        if (isBigContent) getBigLayout() else getSmallLayout()

    override fun getRootId(): Int =
        if (isBigContent) getBigRootId() else getSmallRootId()

    override fun getTitleId(): Int =
        if (isBigContent) getBigTitleId() else getSmallTitleId()

    override fun getPlayId(): Int =
        if (isBigContent) getBigPlayId() else getSmallPlayId()

    override fun getPreviousId(): Int =
        if (isBigContent) getBigPreviousId() else getSmallPreviousId()

    override fun getNextId(): Int =
        if (isBigContent) getBigNextId() else getSmallNextId()

    override fun getFinishId(): Int =
        if (isBigContent) getBigFinishId() else getSmallFinishId()

    override fun getCoverId(): Int =
        if (isBigContent) getBigCoverId() else getSmallCoverId()

    override fun getIntent(): Intent =
        if (isBigContent) getBigIntent() else getSmallIntent()

}

sealed interface IMusicRemoteViews : IMusicBigRemoteViews, IRemoteView {
    fun getSmallLayout(): Int
    fun getSmallRootId(): Int
    fun getSmallTitleId(): Int
    fun getSmallPlayId(): Int
    fun getSmallPreviousId(): Int
    fun getSmallNextId(): Int
    fun getSmallFinishId(): Int
    fun getSmallCoverId() : Int
    fun getSmallIntent(): Intent
}

sealed interface IMusicBigRemoteViews {
    fun getBigLayout(): Int
    fun getBigRootId(): Int
    fun getBigTitleId(): Int
    fun getBigPlayId(): Int
    fun getBigPreviousId(): Int
    fun getBigNextId(): Int
    fun getBigFinishId(): Int
    fun getBigCoverId() : Int
    fun getBigIntent(): Intent
}
