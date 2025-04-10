package br.com.jonatas.metronomeplus.presenter.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import br.com.jonatas.metronomeplus.R
import br.com.jonatas.metronomeplus.databinding.ViewLibraryItemBinding
import br.com.jonatas.metronomeplus.presenter.model.FolderUiModel
import br.com.jonatas.metronomeplus.presenter.util.convertToDateString

class LibraryFoldersAdapter(
    private val context: Context,
    private val folders: List<FolderUiModel> = listOf()
) : RecyclerView.Adapter<LibraryFoldersAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ViewLibraryItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val folderUi = folders[position]
        holder.bind(folderUi)
    }

    override fun getItemCount(): Int {
        return folders.size
    }

    inner class ViewHolder(private val binding: ViewLibraryItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(folderUiModel: FolderUiModel) {
            binding.apply {
                name.text = folderUiModel.name
                musics.text = context.resources.getQuantityString(
                    R.plurals.music_count,
                    folderUiModel.musics,
                    folderUiModel.musics
                )
                updateDate.text = folderUiModel.date.convertToDateString()
                folderImage.setImageResource(getFolderDrawable(folderUiModel.musics))

                menuImage.setOnClickListener { }
            }
        }

        private fun getFolderDrawable(amountMusics: Int): Int {
            return when (amountMusics) {
                0 -> {
                    R.drawable.ic_folder_empty_white
                }

                in 1..3 -> {
                    R.drawable.ic_folder_file_white
                }

                else -> {
                    R.drawable.ic_folder_files_white
                }
            }
        }
    }

}