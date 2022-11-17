package com.protone.ui.view.customView

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
import com.protone.ui.R

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
        switcher.setOutAnimation(context,R.anim.image_switch_out)
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

    fun setImageBitmap(bitmap: Bitmap?) {
        switcher.apply {
            (nextView as ImageView).setImageBitmap(bitmap)
            showNext()
        }
    }

    fun setImageResource(@DrawableRes res: Int) {
        switcher.apply {
            (nextView as ImageView).setImageResource(res)
            showNext()
        }
    }

    fun setImageDrawable(drawable: Drawable?) {
        switcher.apply {
            (nextView as ImageView).setImageDrawable(drawable)
            showNext()
            (nextView as ImageView).setImageBitmap(null)
        }
    }

}