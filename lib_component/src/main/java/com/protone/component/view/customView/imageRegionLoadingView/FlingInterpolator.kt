package com.protone.component.view.customView.imageRegionLoadingView

import android.view.animation.Interpolator

class FlingInterpolator : Interpolator {

    override fun getInterpolation(input: Float): Float {
        var t = input
        t -= 1f
        return t * t * t * t * t + 1f
    }

}
