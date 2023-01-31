package com.protone.music.bindingAdapter

import android.animation.ValueAnimator
import android.text.method.ScrollingMovementMethod
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.core.view.marginStart
import androidx.databinding.BindingAdapter
import androidx.transition.TransitionManager
import com.protone.common.context.marginStart
import com.protone.common.utils.displayUtils.AnimationHelper
import com.protone.component.view.customView.BlurTableCardView
import com.protone.component.view.customView.blurView.DefaultBlurController
import com.protone.component.view.customView.blurView.DefaultBlurEngine
import com.protone.component.view.customView.musicPlayer.MusicPlayerViewLite
import com.protone.music.databinding.MusicActivityLayoutBinding

@BindingAdapter(value = ["State", "Binding"], requireAll = true)
internal fun MusicPlayerViewLite.onStateChange(
    isOpen: Boolean,
    binding: MusicActivityLayoutBinding
) {
    if (binding.musicFinish.measuredWidth <= 0) return
    TransitionManager.beginDelayedTransition(binding.musicBucketContainer)
    if (isOpen) {
        marginStart(binding.musicFinish.measuredWidth - binding.musicFinish.paddingStart)
    } else {
        marginStart(0)
    }
}

@BindingAdapter(value = ["ShowDetail"], requireAll = true)
internal fun ImageView.showDetail(binding: MusicActivityLayoutBinding) {
    binding.apply {
        val updateListener = ValueAnimator.AnimatorUpdateListener {
            musicModelContainer.progress = it.animatedValue as Float
        }

        val animatorStartSet = AnimationHelper.rotation(this@showDetail, 180f)
        val start = ValueAnimator.ofFloat(0f, 1f)
        start.addUpdateListener(updateListener)
        val startAnimator = AnimationHelper.animatorSet(start, animatorStartSet)

        val animatorEndSet = AnimationHelper.rotation(this@showDetail, 0f)
        val end = ValueAnimator.ofFloat(1f, 0f)
        end.addUpdateListener(updateListener)
        val endAnimator = AnimationHelper.animatorSet(end, animatorEndSet)

        DecelerateInterpolator().also {
            startAnimator.interpolator = it
            endAnimator.interpolator = it
        }

        setOnClickListener {
            if (startAnimator.isRunning || endAnimator.isRunning) {
                return@setOnClickListener
            }
            when (musicModelContainer.progress) {
                0f -> startAnimator.start()
                1f -> endAnimator.start()
            }
        }
    }
}

@BindingAdapter(value = ["BlurInit"], requireAll = true)
internal fun BlurTableCardView.initRenderBlur(binding: MusicActivityLayoutBinding) {
    binding.apply {
        initBlurTool(
            DefaultBlurController(
                root as ViewGroup,
                DefaultBlurEngine().also {
                    it.scaleFactor = 16f
                })
        )
        root.viewTreeObserver.addOnPreDrawListener {
            renderFrame()
            true
        }
    }
}
