package com.protone.common.utils.displayUtils

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.View
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart

object AnimationHelper {

    enum class AniQueue {
        WITH,
        AFTER,
        BEFORE
    }

    inline fun translationX(
        view: View,
        var1: Float,
        var2: Float,
        duration: Long,
        play: Boolean = false,
        crossinline doOnEnd: () -> Unit = {},
        crossinline doOnStart: () -> Unit = {}
    ): ObjectAnimator = ObjectAnimator.ofFloat(
        view,
        "translationX",
        var1,
        var2
    ).apply {
        this.duration = duration
        doOnEnd { doOnEnd() }
        doOnStart { doOnStart() }
        if (play) start()
    }

    inline fun translationY(
        view: View,
        var1: Float,
        var2: Float,
        duration: Long? = null,
        play: Boolean = false,
        crossinline doOnEnd: () -> Unit = {},
        crossinline doOnStart: () -> Unit = {}
    ): ObjectAnimator = ObjectAnimator.ofFloat(
        view,
        "translationY",
        var1,
        var2
    ).apply {
        if (duration != null) {
            this.duration = duration
        }
        doOnEnd { doOnEnd() }
        doOnStart { doOnStart() }
        if (play) start()
    }

    inline fun translationX(
        view: View,
        var1: Float,
        duration: Long? = null,
        play: Boolean = false,
        crossinline doOnEnd: () -> Unit = {},
        crossinline doOnStart: () -> Unit = {}
    ): ObjectAnimator = ObjectAnimator.ofFloat(
        view,
        "translationX",
        var1
    ).apply {
        if (duration != null) {
            this.duration = duration
        }
        doOnEnd { doOnEnd() }
        doOnStart { doOnStart() }
        if (play) start()
    }

    inline fun translationY(
        view: View,
        var1: Float,
        duration: Long? = null,
        play: Boolean = false,
        crossinline doOnEnd: () -> Unit = {},
        crossinline doOnStart: () -> Unit = {}
    ): ObjectAnimator = ObjectAnimator.ofFloat(
        view,
        "translationY",
        var1
    ).apply {
        if (duration != null) {
            this.duration = duration
        }
        doOnEnd { doOnEnd() }
        doOnStart { doOnStart() }
        if (play) start()
    }

    inline fun rotation(
        view: View,
        var1: Float,
        duration: Long? = null,
        play: Boolean = false,
        crossinline doOnEnd: () -> Unit = {},
        crossinline doOnStart: () -> Unit = {}
    ): ObjectAnimator = ObjectAnimator.ofFloat(
        view,
        "rotation",
        var1
    ).apply {
        if (duration != null) {
            this.duration = duration
        }
        doOnEnd { doOnEnd() }
        doOnStart { doOnStart() }
        if (play) start()
    }

    inline fun alpha(
        view: View,
        var1: Float,
        duration: Long? = null,
        play: Boolean = false,
        crossinline doOnEnd: () -> Unit = {},
        crossinline doOnStart: () -> Unit = {}
    ): ObjectAnimator = ObjectAnimator.ofFloat(
        view,
        "alpha",
        var1
    ).apply {
        if (duration != null) {
            this.duration = duration
        }
        doOnEnd { doOnEnd() }
        doOnStart { doOnStart() }
        if (play) start()
    }

    inline fun scaleX(
        view: View,
        var1: Float,
        duration: Long? = null,
        play: Boolean = false,
        crossinline doOnEnd: () -> Unit = {},
        crossinline doOnStart: () -> Unit = {}
    ): ObjectAnimator = ObjectAnimator.ofFloat(
        view,
        "scaleX",
        var1
    ).apply {
        if (duration != null) {
            this.duration = duration
        }
        doOnEnd { doOnEnd() }
        doOnStart { doOnStart() }
        if (play) start()
    }

    inline fun scaleY(
        view: View,
        var1: Float,
        duration: Long? = null,
        play: Boolean = false,
        crossinline doOnEnd: () -> Unit = {},
        crossinline doOnStart: () -> Unit = {}
    ): ObjectAnimator = ObjectAnimator.ofFloat(
        view,
        "scaleY",
        var1
    ).apply {
        if (duration != null) {
            this.duration = duration
        }
        doOnEnd { doOnEnd() }
        doOnStart { doOnStart() }
        if (play) start()
    }

    inline fun animatorSet(
        vararg items: Animator,
        play: Boolean = false,
        crossinline doOnEnd: () -> Unit = {},
        crossinline doOnStart: () -> Unit = {}
    ): AnimatorSet = AnimatorSet().apply {
        doOnEnd { doOnEnd() }
        doOnStart { doOnStart() }
        playTogether(items.asList())
        if (play) start()
    }

    object AnimationSet {
        inline fun translationX(
            view: View,
            var1: Float,
            var2: Float,
            duration: Long,
            crossinline doOnEnd: () -> Unit = {},
            crossinline doOnStart: () -> Unit = {}
        ): AnimationSetHelper =
            AnimationSetHelper(translationX(view, var1, var2, duration, false, doOnEnd, doOnStart))

        inline fun translationY(
            view: View,
            var1: Float,
            var2: Float,
            duration: Long? = null,
            crossinline doOnEnd: () -> Unit = {},
            crossinline doOnStart: () -> Unit = {}
        ): AnimationSetHelper =
            AnimationSetHelper(translationY(view, var1, var2, duration, false, doOnEnd, doOnStart))

        inline fun translationX(
            view: View,
            var1: Float,
            duration: Long? = null,
            crossinline doOnEnd: () -> Unit = {},
            crossinline doOnStart: () -> Unit = {}
        ): AnimationSetHelper =
            AnimationSetHelper(translationX(view, var1, duration, false, doOnEnd, doOnStart))

        inline fun translationY(
            view: View,
            var1: Float,
            duration: Long? = null,
            crossinline doOnEnd: () -> Unit = {},
            crossinline doOnStart: () -> Unit = {}
        ): AnimationSetHelper =
            AnimationSetHelper(translationY(view, var1, duration, false, doOnEnd, doOnStart))

        inline fun rotation(
            view: View,
            var1: Float,
            duration: Long? = null,
            crossinline doOnEnd: () -> Unit = {},
            crossinline doOnStart: () -> Unit = {}
        ): AnimationSetHelper =
            AnimationSetHelper(rotation(view, var1, duration, false, doOnEnd, doOnStart))

        inline fun alpha(
            view: View,
            var1: Float,
            duration: Long? = null,
            crossinline doOnEnd: () -> Unit = {},
            crossinline doOnStart: () -> Unit = {}
        ): AnimationSetHelper =
            AnimationSetHelper(alpha(view, var1, duration, false, doOnEnd, doOnStart))

        inline fun scaleX(
            view: View,
            var1: Float,
            duration: Long? = null,
            crossinline doOnEnd: () -> Unit = {},
            crossinline doOnStart: () -> Unit = {}
        ): AnimationSetHelper =
            AnimationSetHelper(scaleX(view, var1, duration, false, doOnEnd, doOnStart))

        inline fun scaleY(
            view: View,
            var1: Float,
            duration: Long? = null,
            crossinline doOnEnd: () -> Unit = {},
            crossinline doOnStart: () -> Unit = {}
        ): AnimationSetHelper =
            AnimationSetHelper(scaleY(view, var1, duration, false, doOnEnd, doOnStart))
    }


    class AnimationSetHelper(animator: ValueAnimator) {
        private val animatorSet = AnimatorSet()
        private var aniBuilder: AnimatorSet.Builder? = null

        init {
            aniBuilder = animatorSet.play(animator)
        }

        inline fun translationX(
            view: View,
            var1: Float,
            var2: Float,
            duration: Long,
            crossinline doOnEnd: () -> Unit = {},
            crossinline doOnStart: () -> Unit = {},
            aniQueue: AniQueue?,
            aniDelay: Long? = null
        ): AnimationSetHelper =
            add(
                translationX(view, var1, var2, duration, false, doOnEnd, doOnStart),
                aniQueue,
                aniDelay
            )

        inline fun translationY(
            view: View,
            var1: Float,
            var2: Float,
            duration: Long? = null,
            crossinline doOnEnd: () -> Unit = {},
            crossinline doOnStart: () -> Unit = {},
            aniQueue: AniQueue?,
            aniDelay: Long? = null
        ): AnimationSetHelper =
            add(
                translationY(view, var1, var2, duration, false, doOnEnd, doOnStart),
                aniQueue,
                aniDelay
            )

        inline fun translationX(
            view: View,
            var1: Float,
            duration: Long? = null,
            crossinline doOnEnd: () -> Unit = {},
            crossinline doOnStart: () -> Unit = {},
            aniQueue: AniQueue?,
            aniDelay: Long? = null
        ): AnimationSetHelper =
            add(translationX(view, var1, duration, false, doOnEnd, doOnStart), aniQueue, aniDelay)

        inline fun translationY(
            view: View,
            var1: Float,
            duration: Long? = null,
            crossinline doOnEnd: () -> Unit = {},
            crossinline doOnStart: () -> Unit = {},
            aniQueue: AniQueue?,
            aniDelay: Long? = null
        ): AnimationSetHelper =
            add(translationY(view, var1, duration, false, doOnEnd, doOnStart), aniQueue, aniDelay)

        inline fun rotation(
            view: View,
            var1: Float,
            duration: Long? = null,
            crossinline doOnEnd: () -> Unit = {},
            crossinline doOnStart: () -> Unit = {},
            aniQueue: AniQueue?,
            aniDelay: Long? = null
        ): AnimationSetHelper =
            add(rotation(view, var1, duration, false, doOnEnd, doOnStart), aniQueue, aniDelay)

        inline fun alpha(
            view: View,
            var1: Float,
            duration: Long? = null,
            crossinline doOnEnd: () -> Unit = {},
            crossinline doOnStart: () -> Unit = {},
            aniQueue: AniQueue?,
            aniDelay: Long? = null
        ): AnimationSetHelper =
            add(alpha(view, var1, duration, false, doOnEnd, doOnStart), aniQueue, aniDelay)

        inline fun scaleX(
            view: View,
            var1: Float,
            duration: Long? = null,
            crossinline doOnEnd: () -> Unit = {},
            crossinline doOnStart: () -> Unit = {},
            aniQueue: AniQueue?,
            aniDelay: Long? = null
        ): AnimationSetHelper =
            add(scaleX(view, var1, duration, false, doOnEnd, doOnStart), aniQueue, aniDelay)

        inline fun scaleY(
            view: View,
            var1: Float,
            duration: Long? = null,
            crossinline doOnEnd: () -> Unit = {},
            crossinline doOnStart: () -> Unit = {},
            aniQueue: AniQueue?,
            aniDelay: Long? = null
        ): AnimationSetHelper =
            add(scaleY(view, var1, duration, false, doOnEnd, doOnStart), aniQueue, aniDelay)

        fun build(): AnimatorSet {
            return animatorSet
        }

        fun add(
            animator: ValueAnimator,
            aniQueue: AniQueue?,
            aniDelay: Long? = null
        ): AnimationSetHelper {
            when (aniQueue) {
                AniQueue.WITH -> {
                    aniBuilder?.with(animator)
                }
                AniQueue.AFTER -> {
                    aniBuilder?.after(animator)
                }
                AniQueue.BEFORE -> {
                    aniBuilder?.before(animator)
                }
                else -> aniDelay?.let { aniBuilder?.after(it) }
            }
            return this
        }

    }
}