package com.protone.ui.view.customView.musicPlayer

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import com.protone.ui.R
import com.protone.ui.databinding.AutoMusicPlayerLayoutLiteBinding
import com.protone.common.context.newLayoutInflater
import com.protone.ui.view.customView.ColorfulProgressBar
import com.protone.ui.view.customView.SwitchImageView

class MusicPlayerViewLite @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : BaseMusicPlayer(context, attrs) {

    private val binding =
        AutoMusicPlayerLayoutLiteBinding.inflate(context.newLayoutInflater, this, true)

    override val next: ImageView? = null
    override val control: ImageView = binding.musicControl
    override val previous: ImageView? = null
    override val progress: ColorfulProgressBar? = null
    override val switcher: SwitchImageView = binding.musicBack
    override val coverSwitcher: SwitchImageView = binding.musicCover
    override var looper: ImageView? = null
    override val root: View = binding.musicBack

    override var duration: Long? = 0L

    override fun onPlay() {
        binding.musicControl.setImageResource(R.drawable.ic_round_paused_white)
    }

    override fun onPause() {
        binding.musicControl.setImageResource(R.drawable.ic_round_on_white)
    }

    override fun setName(name: String) {
        binding.musicName.text = name
    }

    override fun setDetail(detail: String) {}

    init {
        coverSwitcher.enableShapeAppearance = false
    }

}