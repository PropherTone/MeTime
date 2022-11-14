package com.protone.common.utils.spans

import android.graphics.Bitmap
import android.view.View
import com.protone.common.entity.RichMusicStates
import com.protone.common.entity.RichNoteStates
import com.protone.common.entity.RichPhotoStates
import com.protone.common.entity.RichVideoStates

interface ISpanForEditor : ISpan {
    fun setSize(size: Int)
    fun setColor(color: Any)
    fun setBackColor(color: Any)
    fun insertText(note: RichNoteStates)
    fun insertImage(photo: RichPhotoStates, ba: Bitmap?)
    fun insertVideo(video: RichVideoStates)
    fun insertMusic(music: RichMusicStates)
    fun setBullet(
        gapWidth: Int? = null,
        color: Any? = null,
        radius: Int? = null
    )

    fun setQuote(
        color: Any? = null,
        stripeWidth: Int? = null,
        gapWidth: Int? = null
    )

    fun setParagraph(alignment: SpanStates.SpanAlignment)
    fun getChild(index: Int): View?
}