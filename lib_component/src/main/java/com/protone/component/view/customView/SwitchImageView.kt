package com.protone.component.view.customView

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ViewSwitcher
import androidx.annotation.DrawableRes
import androidx.core.view.children
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.ShapeAppearanceModel
import com.protone.common.context.setBlurBitmap
import com.protone.component.R

class SwitchImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val switcher: ViewSwitcher = ViewSwitcher(context, attrs)

    var enableShapeAppearance: Boolean = false
        set(value) {
            if (value) {
                switcher.children.forEach {
                    (it as ShapeableImageView).shapeAppearanceModel =
                        ShapeAppearanceModel
                            .builder(context, R.style.ovalImage, R.style.ovalImage)
                            .build()
                }
            }
            field = value
        }

    init {
        addView(switcher)
        switcher.setInAnimation(context, R.anim.image_switch_in)
        switcher.setOutAnimation(context, R.anim.image_switch_out)
        repeat(2) {
            switcher.addView(ShapeableImageView(context).apply {
                layoutParams = LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT,
                    Gravity.CENTER
                )
                scaleType = ImageView.ScaleType.CENTER_CROP
            })
        }
    }

    fun setBlurBitmap(bitmap: Bitmap?, radius: Int, sample: Int) {
        switcher.switch {
            it.setBlurBitmap(bitmap, radius, sample)
        }
    }

    fun setImageBitmap(bitmap: Bitmap?) {
        switcher.switch {
            it.setImageBitmap(bitmap)
        }
    }

    fun setImageResource(@DrawableRes res: Int) {
        switcher.switch {
            it.setImageResource(res)
        }
    }

    fun setImageDrawable(drawable: Drawable?) {
        switcher.switch {
            it.setImageDrawable(drawable)
        }
        (switcher.nextView as ImageView).setImageBitmap(null)
    }

    private inline fun ViewSwitcher.switch(block: (ImageView) -> Unit) {
        block(nextView as ImageView)
        showNext()
    }

}