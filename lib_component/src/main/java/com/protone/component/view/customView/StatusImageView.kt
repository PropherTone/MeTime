package com.protone.component.view.customView

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import com.protone.component.R
import com.protone.common.baseType.getDrawable

class StatusImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0
) : androidx.appcompat.widget.AppCompatImageView(context, attrs, defStyleAttr) {
    var positive = false
        private set
    private var stateListener: StateListener? = null

    private var activeDrawable = R.drawable.ic_round_arrow_drop_down_24_blue.getDrawable()
    private var negativeDrawable = R.drawable.ic_round_arrow_drop_up_24_blue.getDrawable()

    init {
        setImageDrawable(activeDrawable)
        context.theme.obtainStyledAttributes(
            attrs, R.styleable.StateImageView, defStyleAttr, defStyleRes
        ).let {
            activeDrawable = it.getDrawable(R.styleable.StateImageView_ActiveDrawable)
            negativeDrawable = it.getDrawable(R.styleable.StateImageView_NegativeDrawable)
        }
        this.setOnClickListener {
            if (positive) {
                active()
            } else {
                negative()
            }
        }
    }

    fun setOnStateListener(l: StateListener) {
        this.stateListener = l
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun active() {
        setImageDrawable(negativeDrawable)
        positive = false
        stateListener?.onActive()
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun negative() {
        setImageDrawable(activeDrawable)
        positive = true
        stateListener?.onNegative()
    }

    interface StateListener {
        fun onActive()
        fun onNegative()
    }
}