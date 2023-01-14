package com.protone.component.notification.music.remoteViews

import android.content.Intent

internal abstract class BaseMusicContentProvider(private val mrv: IMusicRemoteViews) : IRemoteView,
    IRemoteViewProvider {

    protected var isBigContent = false

    override fun getLayout(): Int =
        if (isBigContent) mrv.getBigLayout() else mrv.getSmallLayout()

    override fun getRootId(): Int =
        if (isBigContent) mrv.getBigRootId() else mrv.getSmallRootId()

    override fun getTitleId(): Int =
        if (isBigContent) mrv.getBigTitleId() else mrv.getSmallTitleId()

    override fun getPlayId(): Int =
        if (isBigContent) mrv.getBigPlayId() else mrv.getSmallPlayId()

    override fun getPreviousId(): Int =
        if (isBigContent) mrv.getBigPreviousId() else mrv.getSmallPreviousId()

    override fun getNextId(): Int =
        if (isBigContent) mrv.getBigNextId() else mrv.getSmallNextId()

    override fun getFinishId(): Int =
        if (isBigContent) mrv.getBigFinishId() else mrv.getSmallFinishId()

    override fun getCoverId(): Int =
        if (isBigContent) mrv.getBigCoverId() else mrv.getSmallCoverId()

    override fun getIntent(): Intent =
        if (isBigContent) mrv.getBigIntent() else mrv.getSmallIntent()

}
