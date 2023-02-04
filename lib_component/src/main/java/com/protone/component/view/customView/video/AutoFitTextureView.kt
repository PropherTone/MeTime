package com.protone.component.view.customView.video

import android.content.Context
import android.util.AttributeSet
import android.view.TextureView
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes

class AutoFitTextureView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0
) : TextureView(context, attrs, defStyleAttr, defStyleRes) {

    private var videoHeight = 0
    private var videoWidth = 0

    fun adaptVideoSize(videoWidth: Int, videoHeight: Int) {
        if (this.videoWidth != videoWidth && this.videoHeight != videoHeight) {
            this.videoWidth = videoWidth
            this.videoHeight = videoHeight
            requestLayout()
        }
    }

    override fun setRotation(rotation: Float) {
        if (rotation != getRotation()) {
            super.setRotation(rotation)
            requestLayout()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val viewRotation = rotation
        var widthMS = widthMeasureSpec
        var heightMS = heightMeasureSpec
        if (viewRotation == 90f || viewRotation == 270f) {
            val tempMeasureSpec = widthMS
            widthMS = heightMS
            heightMS = tempMeasureSpec
        }
        var width = getDefaultSize(videoWidth, widthMS)
        var height = getDefaultSize(videoHeight, heightMS)
        if (videoWidth > 0 && videoHeight > 0) {
            val widthSpecMode = MeasureSpec.getMode(widthMS)
            val widthSpecSize = MeasureSpec.getSize(widthMS)
            val heightSpecMode = MeasureSpec.getMode(heightMS)
            val heightSpecSize = MeasureSpec.getSize(heightMS)
            if (widthSpecMode == MeasureSpec.EXACTLY && heightSpecMode == MeasureSpec.EXACTLY) {
                // the size is fixed
                width = widthSpecSize
                height = heightSpecSize
                // for compatibility, we adjust size based on aspect ratio
                if (videoWidth * height < width * videoHeight) {
                    width = height * videoWidth / videoHeight
                } else if (videoWidth * height > width * videoHeight) {
                    height = width * videoHeight / videoWidth
                }
            } else if (widthSpecMode == MeasureSpec.EXACTLY) {
                // only the width is fixed, adjust the height to match aspect ratio if possible
                width = widthSpecSize
                height = width * videoHeight / videoWidth
                if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {
                    // couldn't match aspect ratio within the constraints
                    height = heightSpecSize
                    width = height * videoWidth / videoHeight
                }
            } else if (heightSpecMode == MeasureSpec.EXACTLY) {
                // only the height is fixed, adjust the width to match aspect ratio if possible
                height = heightSpecSize
                width = height * videoWidth / videoHeight
                if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
                    // couldn't match aspect ratio within the constraints
                    width = widthSpecSize
                    height = width * videoHeight / videoWidth
                }
            } else {
                // neither the width nor the height are fixed, try to use actual video size
                width = videoWidth
                height = videoHeight
                if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {
                    // too tall, decrease both width and height
                    height = heightSpecSize
                    width = height * videoWidth / videoHeight
                }
                if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
                    // too wide, decrease both width and height
                    width = widthSpecSize
                    height = width * videoHeight / videoWidth
                }
            }
        }
        setMeasuredDimension(width, height)
    }
}