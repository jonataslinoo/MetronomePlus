package br.com.jonatas.metronomeplus.presenter.interfaces

import android.view.View
import br.com.jonatas.metronomeplus.presenter.model.FolderUiModel

interface OnFolderClickListener {
    fun onFolderClicked(folderUiModel: FolderUiModel)
    fun onFolderOptionsClicked(folderUiModel: FolderUiModel, anchorView: View)
}