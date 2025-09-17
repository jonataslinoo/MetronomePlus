package br.com.jonatas.metronomeplus.presenter.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import br.com.jonatas.metronomeplus.databinding.ViewFolderFormSongItemBinding
import br.com.jonatas.metronomeplus.presenter.extension.bindNumeratorToViews
import br.com.jonatas.metronomeplus.presenter.model.song.SongCallbacks
import br.com.jonatas.metronomeplus.presenter.model.song.SongUiModel
import br.com.jonatas.metronomeplus.presenter.ui.adapter.utils.EditableAdapterPayloads.PAYLOAD_EDITING_CHANGED
import br.com.jonatas.metronomeplus.presenter.ui.adapter.utils.EditableAdapterPayloads.PAYLOAD_LIST_EDITING_MODE_CHANGED
import br.com.jonatas.metronomeplus.presenter.ui.adapter.utils.EditableAdapterState

class FolderFormSongsAdapter() :
    ListAdapter<SongUiModel, FolderFormSongsAdapter.ViewHolder>(DiffCallback) {

    private var callbacks: SongCallbacks? = null

    fun setCallbacks(callbacks: SongCallbacks) {
        this.callbacks = callbacks
    }

    var editableState: EditableAdapterState = EditableAdapterState()
        set(value) {
            if (field != value) {
                field = value
                if (value.isEditingEnabled)
                    notifyItemRangeChanged(0, itemCount, PAYLOAD_EDITING_CHANGED)

                if (value.isListEditingMode)
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

    inner class ViewHolder(private val binding: ViewFolderFormSongItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private lateinit var songUi: SongUiModel

        init {
            binding.apply {
                root.setOnClickListener {
                    if (::songUi.isInitialized)
                        callbacks?.onItemClicked(songUi.id)
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

                applyEditingState(editableState.isEditingEnabled)
            }
        }

        private fun applyEditingState(isEditing: Boolean) {
            binding.apply {
                songItemOptions.isEnabled = isEditing
                songItemSelected.isEnabled = isEditing
            }
        }
    }
}