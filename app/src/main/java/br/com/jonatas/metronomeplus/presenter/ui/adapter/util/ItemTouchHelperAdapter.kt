package br.com.jonatas.metronomeplus.presenter.ui.adapter.util

import androidx.recyclerview.widget.ItemTouchHelper

interface ItemTouchHelperAdapter {
    fun onRowMove(fromPosition: Int, toPosition: Int)
    fun onAttachHelper(itemTouchHelper: ItemTouchHelper)
}