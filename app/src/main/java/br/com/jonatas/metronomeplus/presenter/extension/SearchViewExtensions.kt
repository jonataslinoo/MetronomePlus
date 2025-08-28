package br.com.jonatas.metronomeplus.presenter.extension

import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.core.view.size

fun SearchView.enabledAllChildren(enable: Boolean) {
    enableAllChildrenOfSearchView(this, enable)
}

private fun enableAllChildrenOfSearchView(view: View, enable: Boolean) {
    view.isEnabled = enable
    if (view is ViewGroup) {
        val viewGroup = view
        for (i in 0..<viewGroup.size) {
            val child = viewGroup.getChildAt(i)
            enableAllChildrenOfSearchView(child, enable)
        }
    }
}