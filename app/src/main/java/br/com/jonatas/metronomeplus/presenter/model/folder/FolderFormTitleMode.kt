package br.com.jonatas.metronomeplus.presenter.model.folder

sealed class FolderFormTitleMode {
    object NewFolder : FolderFormTitleMode()
    object ViewFolder : FolderFormTitleMode()
    object EditFolder : FolderFormTitleMode()
}