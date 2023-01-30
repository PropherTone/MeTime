package com.protone.music.bindingAdapter

import android.animation.ValueAnimator
import android.text.method.ScrollingMovementMethod
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import com.protone.common.utils.displayUtils.AnimationHelper
import com.protone.component.view.customView.BlurTableCardView
import com.protone.component.view.customView.blurView.DefaultBlurController
import com.protone.component.view.customView.blurView.DefaultBlurEngine
import com.protone.music.databinding.MusicActivityLayoutBinding

@BindingAdapter(value = ["ShowDetail"], requireAll = true)
fun ImageView.showDetail(binding: MusicActivityLayoutBinding) {
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

        setOnClickListener {
            if (startAnimator.isRunning || endAnimator.isRunning) {
                return@setOnClickListener
            }
            when (musicModelContainer.progress) {
                0f -> {
                    locate.isVisible = false
                    startAnimator.start()
                }
                1f -> endAnimator.start()
            }
        }
    }
}

@BindingAdapter(value = ["BlurInit"], requireAll = true)
fun BlurTableCardView.initRenderBlur(binding: MusicActivityLayoutBinding) {
    binding.apply {
        initBlurTool(
            DefaultBlurController(
                root as ViewGroup,
                DefaultBlurEngine().also {
                    it.scaleFactor = 16f
                })
        )
        setForeColor(root.context.getColor(com.protone.component.R.color.foreDark))
        root.viewTreeObserver.addOnPreDrawListener {
            renderFrame()
            true
        }
    }
}
