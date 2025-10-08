package br.com.jonatas.metronomeplus.presenter.ui.adapter

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import br.com.jonatas.metronomeplus.R
import br.com.jonatas.metronomeplus.databinding.ViewFolderFormSongItemBinding
import br.com.jonatas.metronomeplus.presenter.extension.bindNumeratorToViews
import br.com.jonatas.metronomeplus.presenter.extension.vibrateHeavyClick
import br.com.jonatas.metronomeplus.presenter.model.song.SongCallbacks
import br.com.jonatas.metronomeplus.presenter.model.song.SongUiModel
import br.com.jonatas.metronomeplus.presenter.ui.adapter.util.ItemTouchHelperAdapter
import br.com.jonatas.metronomeplus.presenter.ui.adapter.util.ItemTouchHelperViewHolder
import br.com.jonatas.metronomeplus.presenter.ui.adapter.utils.EditableAdapterPayloads.PAYLOAD_EDITING_CHANGED
import br.com.jonatas.metronomeplus.presenter.ui.adapter.utils.EditableAdapterPayloads.PAYLOAD_LIST_EDITING_MODE_CHANGED
import br.com.jonatas.metronomeplus.presenter.ui.adapter.utils.EditableState
import dagger.hilt.android.scopes.FragmentScoped
import javax.inject.Inject

private const val DRAG_LONG_CLICK_DELAY = 200L
private const val DEFAULT_LONG_CLICK_DURATION = 600L

@FragmentScoped
class FolderFormSongsAdapter @Inject constructor() :
    ListAdapter<SongUiModel, FolderFormSongsAdapter.ViewHolder>(DiffCallback),
    ItemTouchHelperAdapter {

    private var itemTouchHelper: ItemTouchHelper? = null
    private var callbacks: SongCallbacks? = null

    fun setCallbacks(callbacks: SongCallbacks) {
        this.callbacks = callbacks
    }

    var editableState: EditableState = EditableState()
        set(value) {
            if (field != value) {
                field = value
                if (value.isEditMode)
                    notifyItemRangeChanged(0, itemCount, PAYLOAD_EDITING_CHANGED)

                if (value.isListEditMode)
                    notifyItemRangeChanged(0, itemCount, PAYLOAD_LIST_EDITING_MODE_CHANGED)
            }
        }

    companion object DiffCallback : DiffUtil.ItemCallback<SongUiModel>() {
        override fun areItemsTheSame(oldItem: SongUiModel, newItem: SongUiModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SongUiModel, newItem: SongUiModel): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ViewFolderFormSongItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val songUi = getItem(position)
        songUi?.let {
            holder.bind(it)
        }
    }

    override fun onRowMove(fromPosition: Int, toPosition: Int) {
        callbacks?.onItemMove(fromPosition, toPosition)
    }

    override fun onAttachHelper(itemTouchHelper: ItemTouchHelper) {
        this.itemTouchHelper = itemTouchHelper
    }

    inner class ViewHolder(private val binding: ViewFolderFormSongItemBinding) :
        RecyclerView.ViewHolder(binding.root), ItemTouchHelperViewHolder {

        private lateinit var songUi: SongUiModel
        private val handler = Handler(Looper.getMainLooper())
        private val vibrator = itemView.context.getSystemService(Vibrator::class.java)
        private var downTime = 0L
        private var eventTimeUp = 0L

        init {
            setAllListeners()
        }

        fun bind(songUi: SongUiModel) {
            this.songUi = songUi

            binding.apply {
                songItemTitle.text = songUi.title
                songItemArtist.text = songUi.artist
                songItemBpm.text = songUi.bpm.toString()

                songUi.timeSignature.bindNumeratorToViews(
                    songItemViewSignature.songSigNumeratorOne,
                    songItemViewSignature.songSigNumeratorTwo,
                )

                songItemViewSignature.songSigDenominator.text =
                    songUi.timeSignature.denominator.toString()

                songItemBeatListview.updateBeats(newBeats = songUi.beatPatterns)

                songItemSelected.isChecked = songUi.selected

                applyListEditingMode(editableState)
            }
        }

        private fun applyListEditingMode(editableState: EditableState) {
            binding.apply {
                songItemSelected.isVisible = editableState.isListEditMode
                songItemDragDrop.isVisible = editableState.isListEditMode
                songItemOptions.isInvisible = editableState.isListEditMode

                if (!editableState.isReorderingMode) {
                    songItemDragDrop.isInvisible = true
                }
            }
        }

        override fun onItemSelected() {
            itemView.alpha = 0.6f
            binding.root.strokeColor = itemView.context.getColor(R.color.beat_highlight_color)
        }

        override fun onItemClear() {
            itemView.alpha = 1.0f
            binding.root.strokeColor = itemView.context.getColor(R.color.white80)
        }

        private fun setAllListeners() {
            binding.apply {
                @SuppressLint("ClickableViewAccessibility")
                root.setOnTouchListener { view, event ->
                    when (event.actionMasked) {
                        MotionEvent.ACTION_DOWN -> {
                            handler.removeCallbacksAndMessages(null)
                            view.isPressed = true
                            downTime = SystemClock.uptimeMillis()

                            if (editableState.isEditMode) {
                                if (::songUi.isInitialized && !editableState.isListEditMode) {
                                    executeCallback(DEFAULT_LONG_CLICK_DURATION) {
                                        callbacks?.onListEditMode(songUi.id, true)
                                    }
                                }

                                if (editableState.isListEditMode && editableState.isReorderingMode) {
                                    executeCallback(DRAG_LONG_CLICK_DELAY) {
                                        itemTouchHelper?.startDrag(this@ViewHolder)
                                    }
                                }
                            }
                            return@setOnTouchListener true
                        }

                        MotionEvent.ACTION_UP -> {
                            handler.removeCallbacksAndMessages(null)
                            eventTimeUp = SystemClock.uptimeMillis()

                            if (view.isPressed) {
                                if (eventTimeUp - downTime < DRAG_LONG_CLICK_DELAY) {
                                    if (::songUi.isInitialized) {
                                        if (!editableState.isEditMode) {
                                            callbacks?.onItemClicked(songUi.id)
                                        } else {
                                            if (editableState.isListEditMode) {
                                                callbacks?.onItemSelectionToggle(songUi.id)
                                            }
                                        }
                                    }
                                }
                                view.isPressed = false
                            }
                            return@setOnTouchListener true
                        }

                        MotionEvent.ACTION_CANCEL -> {
                            handler.removeCallbacksAndMessages(null)
                            view.isPressed = false
                            return@setOnTouchListener true
                        }

                        else -> return@setOnTouchListener false
                    }
                }

                songItemOptions.setOnClickListener {
                    if (::songUi.isInitialized)
                        callbacks?.onItemMenuClicked(songUi.id, it)
                }

                songItemSelected.setOnClickListener {
                    if (::songUi.isInitialized) {
                        if (callbacks != null) {
                            callbacks?.onItemSelectionToggle(songUi.id)
                        } else {
                            songItemSelected.isChecked = false
                        }
                    }
                }
            }
        }

        private fun executeCallback(duration: Long, callback: () -> Unit) {
            val runnable = Runnable {
                vibrator.vibrateHeavyClick()
                callback.invoke()
            }
            handler.postDelayed(runnable, duration)
        }
    }
}