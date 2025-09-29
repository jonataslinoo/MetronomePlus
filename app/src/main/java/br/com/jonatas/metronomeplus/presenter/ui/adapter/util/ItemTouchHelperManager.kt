package br.com.jonatas.metronomeplus.presenter.ui.adapter.util

import androidx.recyclerview.widget.ItemTouchHelper
import dagger.hilt.android.scopes.FragmentScoped
import javax.inject.Inject

@FragmentScoped
class ItemTouchHelperManager @Inject constructor(
    private val adapter: ItemTouchHelperAdapter,
    private val callback: ItemTouchHelperCallback,
) {

    val itemTouchHelper: ItemTouchHelper = ItemTouchHelper(callback)

    init {
        adapter.onAttachHelper(itemTouchHelper = itemTouchHelper)
    }
}