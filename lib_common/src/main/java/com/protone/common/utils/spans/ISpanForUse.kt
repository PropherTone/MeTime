package com.protone.common.utils.spans

import android.net.Uri
import com.protone.common.entity.GalleryMedia

interface ISpanForUse : ISpan {
    fun setSize()
    fun setColor(isBackGround: Boolean)
    suspend fun insertImage(media: GalleryMedia)
    fun insertVideo(uri: Uri)
    fun insertMusic(uri: Uri, list: MutableList<String>, title: String)
    fun setBullet()
    fun setQuote()
    fun setParagraph()
}