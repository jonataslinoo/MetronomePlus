package br.com.jonatas.metronomeplus.presenter.model.song

import android.view.View

data class SongCallbacks(
    val onItemClicked: (songId: String) -> Unit = {},
    val onItemMenuClicked: (songId: String, anchorView: View) -> Unit = { _, _ -> },
    val onItemSelectionToggle: (songId: String) -> Unit = {},
    val onItemMove: (fromPosition: Int, toPosition: Int) -> Unit = { _, _ -> },
    val onListEditMode: (enable: Boolean) -> Unit = {},
)