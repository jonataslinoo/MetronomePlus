package br.com.jonatas.metronomeplus.presenter.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import br.com.jonatas.metronomeplus.databinding.ViewLibraryFoldersItemBinding
import br.com.jonatas.metronomeplus.presenter.extension.getDateString
import br.com.jonatas.metronomeplus.presenter.extension.getDrawableResId
import br.com.jonatas.metronomeplus.presenter.extension.getMusicCountString
import br.com.jonatas.metronomeplus.presenter.interfaces.OnFolderClickListener
import br.com.jonatas.metronomeplus.presenter.model.FolderUiModel

class LibraryFoldersAdapter(
    private val onFolderClickListener: OnFolderClickListener? = null
) : ListAdapter<FolderUiModel, LibraryFoldersAdapter.ViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<FolderUiModel>() {
        override fun areItemsTheSame(oldItem: FolderUiModel, newItem: FolderUiModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: FolderUiModel, newItem: FolderUiModel): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ViewLibraryFoldersItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val folderUi = getItem(position)
        folderUi?.let {
            holder.bind(it, onFolderClickListener)
        }
    }

    class ViewHolder(private val binding: ViewLibraryFoldersItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(folderUi: FolderUiModel, listener: OnFolderClickListener?) {
            with(binding) {
                name.text = folderUi.name
                musics.text = folderUi.getMusicCountString(binding.root.context)
                date.text = folderUi.getDateString()
                folderImage.setImageResource(folderUi.getDrawableResId())

                root.setOnClickListener {
                    listener?.onFolderClicked(folderUi)
                }

                menuImage.setOnClickListener {
                    listener?.onFolderOptionsClicked(folderUi, it)
                }
            }
        }
    }
}