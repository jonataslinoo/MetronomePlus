package br.com.jonatas.metronomeplus.presenter.model.folder

import br.com.jonatas.metronomeplus.presenter.model.FolderUiModel
import br.com.jonatas.metronomeplus.presenter.model.song.SongUiModel
import br.com.jonatas.metronomeplus.presenter.ui.adapter.utils.EditableState

data class FolderFormUiState(
    val folderUi: FolderUiModel,
    val barTitle: FolderFormTitleMode,
    val songsUi: List<SongUiModel> = listOf(),
    val editableState: EditableState,
)