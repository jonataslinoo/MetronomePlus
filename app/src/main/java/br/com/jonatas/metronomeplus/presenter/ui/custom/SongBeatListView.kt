package br.com.jonatas.metronomeplus.presenter.ui.custom

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getDrawable
import br.com.jonatas.metronomeplus.R
import br.com.jonatas.metronomeplus.presenter.model.BeatStateUiModel
import br.com.jonatas.metronomeplus.presenter.model.BeatUiModel

class SongBeatListView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var beatsUi: List<BeatUiModel> = mutableListOf()

    fun updateBeats(newBeats: List<BeatUiModel>) {
        if (newBeats != beatsUi) {
            beatsUi = newBeats
            refreshViews()
        }
    }

    private fun refreshViews() {
        removeAllViews()
        beatsUi.forEachIndexed { index, beatUi ->
            addNewView(index = index, beatUiModel = beatUi)
        }
    }

    private fun addNewView(index: Int, beatUiModel: BeatUiModel) {
        val imageView = ImageView(context).apply {
            val originalDrawable = getStateUiDrawable(context, beatUiModel)

            val drawable = originalDrawable?.mutate()
            drawable?.setTint(ContextCompat.getColor(context, R.color.white80))
            setImageDrawable(drawable)

            layoutParams = if (index == 0) {
                LayoutParams(39, 39).apply {
                    setMargins(0, 0, 4, 0)
                }
            } else if (index - 1 == beatsUi.size) {
                LayoutParams(39, 39).apply {
                    setMargins(4, 0, 4, 0)
                }
            } else {
                LayoutParams(39, 39).apply {
                    setMargins(4, 0, 4, 0)
                }
            }
        }

        addView(imageView, index)
    }

    private fun getStateUiDrawable(context: Context, beatUi: BeatUiModel): Drawable? {
        return when (beatUi.stateUiModel) {
            BeatStateUiModel.Normal -> getDrawable(context, R.drawable.beat_item_normal)
            BeatStateUiModel.Silence -> getDrawable(context, R.drawable.beat_item_silence)
            BeatStateUiModel.Accent -> getDrawable(context, R.drawable.beat_item_accent)
            BeatStateUiModel.Medium -> getDrawable(context, R.drawable.beat_item_medium)
        }
    }
}

