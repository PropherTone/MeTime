package com.protone.component.view.customView

import android.content.Context
import android.graphics.Outline
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.view.ViewOutlineProvider
import androidx.constraintlayout.widget.ConstraintLayout
import com.protone.common.context.clipOutLine
import com.protone.component.R

class CardConstraintLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    init {
        context.obtainStyledAttributes(R.styleable.CardConstraintLayout).also {
            this.clipOutLine(
                it.getDimensionPixelSize(
                    R.styleable.CardConstraintLayout_cardConstraintRadius,
                    20
                ).toFloat()
            )
        }.recycle()
    }
}