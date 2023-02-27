package com.protone.component.view.customView.imageRegionLoadingView

interface OnScrollEvent {
    fun getStartX(): Int
    fun getStartY(): Int

    fun calculateScrollX(scrollValue: Float)
    fun calculateScrollY(scrollValue: Float)

    fun calculateLeftBorder() = Int.MIN_VALUE
    fun calculateRightBorder() = Int.MAX_VALUE
    fun calculateTopBorder() = Int.MIN_VALUE
    fun calculateBottomBorder() = Int.MAX_VALUE

    fun onScrollReady(): Boolean
    fun onFlingReady()
}