package com.protone.component.view.customView

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.constraintlayout.widget.ConstraintLayout
import com.protone.component.view.customView.blurView.SBlurView

class BlurTableCardView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : SBlurView(context, attrs) {

    var topBlock
        set(value) {
            tableTool.topBlock = value
        }
        get() = tableTool.topBlock

    var botBlock
        set(value) {
            tableTool.botBlock = value
        }
        get() = tableTool.botBlock

    private val tableTool by lazy {
        TableAnimationTool(this)
    }

    fun show(
        onStart: Runnable? = null,
        onEnd: Runnable? = null,
        update: ValueAnimator.AnimatorUpdateListener? = null
    ) {
        tableTool.show(onStart, onEnd, update)
    }

    fun hide(
        onStart: Runnable? = null,
        onEnd: Runnable? = null,
        update: ValueAnimator.AnimatorUpdateListener? = null
    ) {
        tableTool.hide(onStart, onEnd, update)
    }

}

class TableCardView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    var topBlock
        set(value) {
            tableTool.topBlock = value
        }
        get() = tableTool.topBlock

    var botBlock
        set(value) {
            tableTool.botBlock = value
        }
        get() = tableTool.botBlock

    private val tableTool by lazy {
        TableAnimationTool(this)
    }

    fun doAfterShow(block: () -> Unit) {
        tableTool.doAfterShow = block
    }

    fun doAfterHide(block: () -> Unit) {
        tableTool.doAfterHide = block
    }

    fun show(
        onStart: Runnable? = null,
        onEnd: Runnable? = null,
        update: ValueAnimator.AnimatorUpdateListener? = null
    ) {
        tableTool.show(onStart, onEnd, update)
    }

    fun hide(
        onStart: Runnable? = null,
        onEnd: Runnable? = null,
        update: ValueAnimator.AnimatorUpdateListener? = null
    ) {
        tableTool.hide(onStart, onEnd, update)
    }

}

class TableAnimationTool(private val view: View) {
    var topBlock = 0f
    var botBlock = 0f

    var interpolator = DecelerateInterpolator()

    var doAfterShow: () -> Unit = {}
    var doAfterHide: () -> Unit = {}

    fun show(
        onStart: Runnable? = null,
        onEnd: Runnable? = null,
        update: ValueAnimator.AnimatorUpdateListener? = null
    ) {
        view.animate().setInterpolator(interpolator)
            .translationY(0f + topBlock)
            .withEndAction {
                onEnd?.run()
            }
            .withStartAction {
                onStart?.run()
            }
            .setUpdateListener(update)
            .start()
    }

    fun hide(
        onStart: Runnable? = null,
        onEnd: Runnable? = null,
        update: ValueAnimator.AnimatorUpdateListener? = null
    ) {
        view.animate().setInterpolator(interpolator)
            .translationY(view.measuredHeight.toFloat() - botBlock)
            .withEndAction(onEnd)
            .withStartAction(onStart)
            .setUpdateListener(update)
            .start()
    }
}