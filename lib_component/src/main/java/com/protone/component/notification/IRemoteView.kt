package com.protone.component.notification

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

interface IRemoteView {
    fun getContent(context: Context): RemoteViews
    fun getBigContent(context: Context): RemoteViews

    fun getLayout(): Int
    fun getRootId(): Int
    fun getTitleId(): Int
    fun getPlayId(): Int
    fun getPreviousId(): Int
    fun getNextId(): Int
    fun getFinishId(): Int
    fun getCoverId(): Int
    fun getIntent(): Intent
}