package com.protone.ui.view.customView.richText

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import com.protone.ui.databinding.RichPhotoLayoutBinding
import com.protone.common.context.newLayoutInflater

class RichPhotoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes){
    val binding = RichPhotoLayoutBinding.inflate(context.newLayoutInflater,this,true)
}