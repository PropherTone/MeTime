package com.protone.base.view.customView.richText

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import com.protone.base.databinding.RichMusicLayoutBinding
import com.protone.common.context.newLayoutInflater

class RichMusicView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes){
    val binding = RichMusicLayoutBinding.inflate(context.newLayoutInflater,this,true)

}