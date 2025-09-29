package br.com.jonatas.metronomeplus.presenter.ui.adapter

import android.annotation.SuppressLint
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
import br.com.jonatas.metronomeplus.presenter.model.song.SongCallbacks
import br.com.jonatas.metronomeplus.presenter.model.song.SongUiModel
import br.com.jonatas.metronomeplus.presenter.ui.adapter.util.ItemTouchHelperAdapter
import br.com.jonatas.metronomeplus.presenter.ui.adapter.util.ItemTouchHelperViewHolder
import br.com.jonatas.metronomeplus.presenter.ui.adapter.utils.EditableAdapterPayloads.PAYLOAD_EDITING_CHANGED
import br.com.jonatas.metronomeplus.presenter.ui.adapter.utils.EditableAdapterPayloads.PAYLOAD_LIST_EDITING_MODE_CHANGED
import br.com.jonatas.metronomeplus.presenter.ui.adapter.utils.EditableState
import dagger.hilt.android.scopes.FragmentScoped
import javax.inject.Inject

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

        init {
            binding.apply {
                @SuppressLint("ClickableViewAccessibility")
                root.setOnTouchListener { view, event ->
                    view.performClick()
                    if (editableState.isListEditMode && editableState.isReorderingMode) {
                        if (event.actionMasked == MotionEvent.ACTION_MOVE) {
                            itemTouchHelper?.startDrag(this@ViewHolder)
                        }
                    }
                    return@setOnTouchListener false
                }

                root.setOnLongClickListener {
                    if (editableState.isEditMode) {
                        if (::songUi.isInitialized && !editableState.isListEditMode) {
                            callbacks?.onListEditMode(songUi.id, !editableState.isListEditMode)
                        }
                    }
                    return@setOnLongClickListener true
                }

                root.setOnClickListener {
                    if (::songUi.isInitialized && !editableState.isEditMode)
                        callbacks?.onItemClicked(songUi.id)
                }

                songItemOptions.setOnClickListener {
                    if (::songUi.isInitialized)
                        callbacks?.onItemMenuClicked(songUi.id, it)
                }
            }
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

                applyListEditingMode(editableState)
            }
        }

        private fun applyListEditingMode(editableState: EditableState) {
            binding.apply {
                songItemSelected.isVisible =  editableState.isListEditMode
                songItemDragDrop.isVisible =  editableState.isListEditMode
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
            binding.root.strokeColor = itemView.context.getColor(R.color.white)
        }
    }
}