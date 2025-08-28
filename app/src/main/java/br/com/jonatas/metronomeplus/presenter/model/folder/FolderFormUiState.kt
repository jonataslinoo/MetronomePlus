package br.com.jonatas.metronomeplus.presenter.model.folder

import br.com.jonatas.metronomeplus.presenter.model.FolderUiModel

data class FolderFormUiState(
    val folderUi: FolderUiModel = FolderUiModel(
        id = "",
        name = "",
        musics = 0,
        date = 0L,
    ),
    val isEditMode: Boolean = false,
    val barTitle: FolderFormTitleMode
)