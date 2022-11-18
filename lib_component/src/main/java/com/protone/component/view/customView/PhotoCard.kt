package com.protone.component.view.customView

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import androidx.annotation.AttrRes
import androidx.cardview.widget.CardView
import com.protone.component.databinding.PhotoCardLayoutBinding
import com.protone.common.context.newLayoutInflater

class PhotoCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {

    private val binding = PhotoCardLayoutBinding.inflate(context.newLayoutInflater, this, true)

    var title = ""
        set(value) {
            binding.photoCardTitle.text = value
            field = value
        }

    val photo : ImageView
        get() = binding.photoCardPhoto

}