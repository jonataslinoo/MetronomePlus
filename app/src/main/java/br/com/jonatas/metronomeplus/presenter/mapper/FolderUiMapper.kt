package br.com.jonatas.metronomeplus.presenter.mapper

import br.com.jonatas.metronomeplus.domain.model.Folder
import br.com.jonatas.metronomeplus.presenter.model.FolderUiModel

fun List<Folder>.toUiModelList(): List<FolderUiModel> = map { it.toUiModel() }

fun Folder.toUiModel(): FolderUiModel = FolderUiModel(
    name = name,
    musics = musics,
    date = date,
)