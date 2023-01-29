package com.protone.music.bindingAdapter

import android.animation.ValueAnimator
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isGone
import androidx.core.view.updateLayoutParams
import androidx.databinding.BindingAdapter
import androidx.databinding.ObservableField
import androidx.transition.TransitionManager
import com.protone.component.view.customView.BlurTableCardView
import com.protone.component.view.customView.StatusImageView
import com.protone.component.view.customView.blurView.DefaultBlurController
import com.protone.component.view.customView.blurView.DefaultBlurEngine
import com.protone.music.activity.MusicActivity
import com.protone.music.databinding.MusicActivityLayoutBinding
import kotlinx.coroutines.launch

@BindingAdapter(value = ["DetailBinding"], requireAll = true)
fun showDetail(imageView: ImageView, binding: MusicActivityLayoutBinding) {
    binding.apply {
        val startAnimator = ValueAnimator.ofFloat(0f, 1f)
        val endAnimator = ValueAnimator.ofFloat(1f, 0f)
        val updateListener = ValueAnimator.AnimatorUpdateListener {
            musicModelContainer.progress = it.animatedValue as Float
        }
        startAnimator.addUpdateListener(updateListener)
        endAnimator.addUpdateListener(updateListener)
        imageView.setOnClickListener {
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

@BindingAdapter(value = ["BlurBinding"], requireAll = true)
fun initRenderBlur(view: BlurTableCardView, binding: MusicActivityLayoutBinding) {
    binding.apply {
        view.initBlurTool(
            DefaultBlurController(
                root as ViewGroup,
                DefaultBlurEngine().also {
                    it.scaleFactor = 16f
                })
        )
        view.setForeColor(root.context.getColor(com.protone.component.R.color.foreDark))
        root.viewTreeObserver.addOnPreDrawListener {
            view.renderFrame()
            true
        }
    }
}

@BindingAdapter(value = ["IsBucketOpen","Activity", "Binding"], requireAll = true)
fun initBucketState(
    view: StatusImageView,
    isBucketOpen: Boolean,
    activity: MusicActivity,
    binding: MusicActivityLayoutBinding
) {

    binding.apply {
        if (isBucketOpen) {
            musicBucketContainer.enableRender()
            var isDone = false
            musicBucketContainer.show(onStart = {
                musicBucketContainer.setWillMove(true)
                musicBucketNamePhanton.isGone = false
                musicFinishPhanton.isGone = false
            }, update = {
                if ((it?.animatedValue as Float) > 0.8f) {
                    if (isDone) return@show
                    isDone = true
                    activity.apply {
                        binding.translatePlayerCoverToFit(true)
                    }
                }
            }, onEnd = {
                musicBucketContainer.setWillMove(false)
            })
        } else {
            musicBucketNamePhanton.isGone = true
            musicFinishPhanton.isGone = true
            musicBucketContainer.hide(onStart = {
                musicBucketContainer.setWillMove(true)
                activity.apply {
                    binding.translatePlayerCoverToFit(false)
                }
            }, onEnd = {
                musicBucketContainer.setWillMove(false)
                musicBucketContainer.disableRender()
            })
        }
    }
}
