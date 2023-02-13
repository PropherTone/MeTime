package com.protone.gallery.component

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.animation.doOnEnd
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.protone.common.baseType.getColor
import com.protone.common.context.newLayoutInflater
import com.protone.gallery.R
import com.protone.component.R as ComponentR
import com.protone.gallery.databinding.GalleryItemLayoutBinding
import kotlin.math.hypot

class GalleryItemView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private var mX = 0f
    private var mY = 0f

    @SuppressLint("ClickableViewAccessibility")
    private val binding =
        GalleryItemLayoutBinding.inflate(context.newLayoutInflater, this, false).apply {
            root.setOnTouchListener { _, event ->
                mX = event.x
                mY = event.y
                false
            }
        }

    private val bucketCheck: View

    init {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            resources.getDimensionPixelSize(R.dimen.gallery_item_height)
        )
        setBackgroundResource(ComponentR.drawable.trans_dark_ripple)
        addView(binding.root)
        addView(View(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                resources.getDimensionPixelSize(R.dimen.bucket_check_line_height),
                Gravity.BOTTOM
            )
            isFocusable = false
            isClickable = false
            setBackgroundColor(ComponentR.color.foreDark.getColor())
            isVisible = false
            bucketCheck = this
        })
    }

    val thumb get() = binding.bucketThumb

    val name get() = binding.bucketName

    val size get() = binding.bucketItemNumber

    var check: Boolean = false
        set(value) {
            reveal(!value)
            field = value
        }

    private fun reveal(visible: Boolean) {
        binding.apply {
            val radius = hypot(mX.toDouble(), mY.toDouble()).toFloat()
            if (visible) {
                val reveal = try {
                    ViewAnimationUtils.createCircularReveal(
                        bucket, mX.toInt(),
                        mY.toInt(), 0f, radius
                    )
                } catch (e: Exception) {
                    null
                }
                bucket.isGone = false
                bucketCheck.animate().scaleX(0f).withEndAction {
                    bucketCheck.isVisible = false
                }.start()
                reveal?.start() ?: run { bucketCheck.isVisible = false }
            } else {
                val reveal = try {
                    ViewAnimationUtils.createCircularReveal(
                        bucket, mX.toInt(),
                        mY.toInt(), radius, 0f
                    )
                } catch (e: Exception) {
                    null
                }
                bucketCheck.animate().scaleX(1f).withStartAction {
                    bucketCheck.isVisible = true
                }.start()
                reveal?.let {
                    it.doOnEnd { bucket.isGone = true }
                    it.start()
                } ?: run {
                    bucket.isGone = true
                    bucketCheck.isVisible = true
                }
            }
        }
    }

    fun stopAnimate() {
        binding.bucket.animate().cancel()
        bucketCheck.animate().cancel()
    }

}