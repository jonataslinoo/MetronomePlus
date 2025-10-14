package br.com.jonatas.metronomeplus.presenter.extension

import java.util.Collections

fun <T> List<T>.swapItems(fromPosition: Int, toPosition: Int): List<T> {
    return toMutableList().apply {
        if (fromPosition in indices && toPosition in indices) {
            Collections.swap(this, fromPosition, toPosition)
        }
    }
}

fun Set<String>.addOrRemove(item: String): Set<String> {
    return toMutableSet().apply {
        if (item.isNotEmpty() && item.isNotBlank()) {
            if (item in this)
                this.remove(item)
            else this.add(item)
        }
    }
}