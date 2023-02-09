package com.protone.component.notification.music.remoteViews

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

internal interface IRemoteView {
    fun getContent(context: Context): RemoteViews
    fun getBigContent(context: Context): RemoteViews
}

internal interface IRemoteViewProvider {
    fun getLayout(isBigContent: Boolean): Int
    fun getRootId(): Int
    fun getTitleId(): Int
    fun getPlayId(): Int
    fun getPreviousId(): Int
    fun getNextId(): Int
    fun getFinishId(): Int
    fun getCoverId(): Int
    fun getIntent(): Intent
}

interface IMusicRemoteViews : IMusicBigRemoteViews {
    fun getSmallLayout(): Int
    fun getSmallRootId(): Int
    fun getSmallTitleId(): Int
    fun getSmallPlayId(): Int
    fun getSmallPreviousId(): Int
    fun getSmallNextId(): Int
    fun getSmallFinishId(): Int
    fun getSmallCoverId(): Int
    fun getSmallIntent(): Intent
}

interface IMusicBigRemoteViews {
    fun getBigLayout(): Int
    fun getBigRootId(): Int
    fun getBigTitleId(): Int
    fun getBigPlayId(): Int
    fun getBigPreviousId(): Int
    fun getBigNextId(): Int
    fun getBigFinishId(): Int
    fun getBigCoverId(): Int
    fun getBigIntent(): Intent
}