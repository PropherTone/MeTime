package com.protone.common.utils.displayUtils

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart

object AnimationHelper {

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

}