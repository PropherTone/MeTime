package com.protone.component.view.customView.musicPlayer

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import com.protone.component.R
import com.protone.component.databinding.AutoMusicPlayerLayoutFullBinding
import com.protone.common.context.newLayoutInflater
import com.protone.component.view.customView.ColorfulProgressBar
import com.protone.component.view.customView.SwitchImageView

class MusicPlayerViewFull @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : BaseMusicPlayer(context, attrs) {

    private val binding =
        AutoMusicPlayerLayoutFullBinding.inflate(context.newLayoutInflater, this, true)

    override val next: ImageView = binding.musicNext
    override val control: ImageView = binding.musicControl
    override val previous: ImageView = binding.musicPrevious
    override val progress: ColorfulProgressBar = binding.musicProgress
    override val switcher: SwitchImageView = binding.musicBack
    override val coverSwitcher: SwitchImageView = binding.musicCover
    override var looper: ImageView? = binding.loopMode
    override val root: View = binding.controller

    override var duration: Long? = 0L
        set(value) {
            binding.musicProgress.barDuration = value ?: 0L
            field = value
        }

    override fun onPlay() {
        binding.musicControl.setImageResource(R.drawable.ic_round_paused_white)
    }

    override fun onPause() {
        binding.musicControl.setImageResource(R.drawable.ic_round_on_white)
    }

    override fun setName(name: String) {
        binding.musicName.ellipsize = TextUtils.TruncateAt.MARQUEE
        binding.musicName.text = name
    }

    override fun setDetail(detail: String) {
        binding.musicDetail.text = detail
    }

    init {
        coverSwitcher.enableShapeAppearance = true
    }

}
