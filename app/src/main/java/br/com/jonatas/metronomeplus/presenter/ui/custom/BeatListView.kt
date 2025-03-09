package br.com.jonatas.metronomeplus.presenter.ui.custom

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat.getDrawable
import br.com.jonatas.metronomeplus.R
import br.com.jonatas.metronomeplus.presenter.model.BeatStateUiModel
import br.com.jonatas.metronomeplus.presenter.model.BeatUiModel

class BeatListView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private lateinit var beatsUi: List<BeatUiModel>

    fun updateBeats(newBeats: List<BeatUiModel>) {
        beatsUi = newBeats
        refreshViews()
    }

    private fun refreshViews() {
        removeAllViews()
        beatsUi.forEach { beatUi ->
            addNewView(beatUi)
        }
    }

    private fun addNewView(beatUiModel: BeatUiModel) {
        val imageView = ImageView(context).apply {
            setImageDrawable(getStateUiDrawable(context, beatUiModel))

            layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f).apply {
                setMargins(4, 4, 4, 4)
            }
        }
        addView(imageView)
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