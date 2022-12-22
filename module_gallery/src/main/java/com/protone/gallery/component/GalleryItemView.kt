package com.protone.gallery.component

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.animation.doOnEnd
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.transition.TransitionManager
import com.protone.common.context.newLayoutInflater
import com.protone.gallery.databinding.GalleryItemLayoutBinding
import kotlin.math.hypot

class GalleryItemView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private var mX = 0f
    private var mY = 0f

    @SuppressLint("ClickableViewAccessibility")
    private val binding =
        GalleryItemLayoutBinding.inflate(context.newLayoutInflater, this, true).apply {
            root.setOnTouchListener { v, event ->
                mX = event.x
                mY = event.y
                false
            }
        }

    val thumb get() = binding.bucketThumb

    val name get() = binding.bucketName

    val size get() = binding.bucketItemNumber

    var check: Boolean = false
        set(value) {
            binding.bucketCheck.isVisible = value
            reveal(!value)
            field = value
        }

    private fun reveal(visible: Boolean) {
        val radius = hypot(mX.toDouble(), mY.toDouble()).toFloat()
        if (visible) {
            val reveal =
                ViewAnimationUtils.createCircularReveal(
                    binding.bucket, mX.toInt(),
                    mY.toInt(), 0f, radius
                )
            binding.bucket.isGone = false
            reveal.start()
        } else {
            val reveal =
                ViewAnimationUtils.createCircularReveal(
                    binding.bucket, mX.toInt(),
                    mY.toInt(), radius, 0f
                )
            reveal.doOnEnd {
                binding.bucket.isGone = true
            }
            reveal.start()
        }
    }
}