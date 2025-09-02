package br.com.jonatas.metronomeplus.presenter.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import br.com.jonatas.metronomeplus.databinding.ViewFolderFormSongItemBinding
import br.com.jonatas.metronomeplus.presenter.extension.bindNumeratorToViews
import br.com.jonatas.metronomeplus.presenter.model.song.SongUiModel

class FolderFormSongsAdapter() :
    ListAdapter<SongUiModel, FolderFormSongsAdapter.ViewHolder>(DiffCallback) {

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

    class ViewHolder(private val binding: ViewFolderFormSongItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(songUi: SongUiModel) {
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
            }
        }
    }
}