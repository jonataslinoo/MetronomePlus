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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BeatListView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var beatsUi: List<BeatUiModel> = mutableListOf()
    private var bpm: Int = 0
    private var intervalBeat: Long = 0

    fun updateBeats(newBeats: List<BeatUiModel>) {
        if (newBeats != beatsUi) {
            beatsUi = newBeats
            refreshViews()
        }
    }

    private fun refreshViews() {
        removeAllViews()
        beatsUi.forEach { beatUi ->
            addNewView(beatUi)
        }
    }

    fun updateBpm(newBpm: Int) {
        if (newBpm != bpm) {
            bpm = newBpm

            intervalBeat = (60_000 / bpm / 6).toLong().coerceAtLeast(1)
        }
    }

    fun nextBeat(index: Int, dispatcher: CoroutineDispatcher = Dispatchers.Main) {
        if (index < 0) return
        if (index >= beatsUi.size) return
        val beatUi = beatsUi[index]

        CoroutineScope(dispatcher).launch {
            setImageView(
                index = index,
                getStateUiDrawableColor(
                    context = context,
                    beatUi = beatUi
                )
            )

            postDelayed(
                {
                    setImageView(
                        index = index,
                        getStateUiDrawable(
                            context = context,
                            beatUi = beatUi
                        )
                    )
                }, intervalBeat
            )
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

    private fun setImageView(index: Int, drawable: Drawable?) {
        val imageView = getChildAt(index) as? ImageView

        if (imageView != null)
            drawable?.let(imageView::setImageDrawable)
    }

    private fun getStateUiDrawable(context: Context, beatUi: BeatUiModel): Drawable? {
        return when (beatUi.stateUiModel) {
            BeatStateUiModel.Normal -> getDrawable(context, R.drawable.beat_item_normal)
            BeatStateUiModel.Silence -> getDrawable(context, R.drawable.beat_item_silence)
            BeatStateUiModel.Accent -> getDrawable(context, R.drawable.beat_item_accent)
            BeatStateUiModel.Medium -> getDrawable(context, R.drawable.beat_item_medium)
        }
    }

    private fun getStateUiDrawableColor(context: Context, beatUi: BeatUiModel): Drawable? {
        return when (beatUi.stateUiModel) {
            BeatStateUiModel.Normal -> getDrawable(context, R.drawable.beat_item_normal_color)
            BeatStateUiModel.Silence -> getDrawable(context, R.drawable.beat_item_silence_color)
            BeatStateUiModel.Accent -> getDrawable(context, R.drawable.beat_item_accent_color)
            BeatStateUiModel.Medium -> getDrawable(context, R.drawable.beat_item_medium_color)
        }
    }
}

