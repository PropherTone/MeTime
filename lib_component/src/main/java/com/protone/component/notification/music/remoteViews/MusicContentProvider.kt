package com.protone.component.notification.music.remoteViews

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.protone.common.context.FINISH
import com.protone.common.context.MUSIC_NEXT
import com.protone.common.context.MUSIC_PLAY
import com.protone.common.context.MUSIC_PREVIOUS

internal class MusicContentProvider(mrv: IMusicRemoteViews) : BaseMusicContentProvider(mrv) {

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

}